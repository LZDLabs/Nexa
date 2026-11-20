package com.nexa.mobile.net

import com.nexa.protocol.FrameCodec
import com.nexa.protocol.Hello
import com.nexa.protocol.HelloOk
import com.nexa.protocol.HelloReject
import com.nexa.protocol.MessageType
import com.nexa.protocol.NexaProtocol
import com.nexa.protocol.Ping
import com.nexa.protocol.RejectReason
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.IOException
import java.net.InetSocketAddress
import java.net.Socket

/** Phone-side TCP link to NEXA TV: handshake, keep-alive/RTT, and serialized frame writes. */
class TvLink(private val scope: CoroutineScope, private val deviceName: String) {

    sealed interface State {
        data object Disconnected : State
        data class Connecting(val message: String) : State
        data class Connected(val tvName: String, val host: String) : State
        data class Failed(val message: String) : State
    }

    /** Events coming from the TV that the streaming service must react to. */
    interface Listener {
        fun onKeyframeRequested()
        fun onSessionEndedByTv()
        fun onLinkLost()
    }

    private val _state = MutableStateFlow<State>(State.Disconnected)
    val state: StateFlow<State> = _state
    private val _rttMs = MutableStateFlow<Long?>(null)
    val rttMs: StateFlow<Long?> = _rttMs

    @Volatile var listener: Listener? = null

    private val writeLock = Any()
    @Volatile private var socket: Socket? = null
    @Volatile private var out: DataOutputStream? = null
    private var pingJob: Job? = null

    val isConnected get() = _state.value is State.Connected

    fun setStatus(state: State) { _state.value = state }

    /** Connects and authenticates with either the QR [token] or the typed [code]. */
    suspend fun connect(host: String, port: Int, token: String? = null, code: String? = null): Result<String> =
        withContext(Dispatchers.IO) {
            close(sendBye = true, newState = State.Connecting("Conectando a $host…"))
            val s = Socket()
            try {
                s.tcpNoDelay = true
                s.connect(InetSocketAddress(host, port), 4_000)
                s.soTimeout = 6_000
                val o = DataOutputStream(BufferedOutputStream(s.getOutputStream(), 64 * 1024))
                val i = DataInputStream(BufferedInputStream(s.getInputStream(), 64 * 1024))
                FrameCodec.write(o, MessageType.HELLO, Hello(NexaProtocol.VERSION, deviceName, token, code).encode())
                o.flush()
                val reply = FrameCodec.read(i)
                when (reply.type) {
                    MessageType.HELLO_OK -> {
                        val tvName = HelloOk.decode(reply.payload).tvName.ifBlank { "NEXA TV" }
                        s.soTimeout = 15_000 // TV answers our pings every 2 s
                        socket = s
                        out = o
                        _state.value = State.Connected(tvName, host)
                        startReader(s, i)
                        startPings()
                        Result.success(tvName)
                    }
                    MessageType.HELLO_REJECT -> {
                        s.close()
                        val msg = rejectMessage(HelloReject.decode(reply.payload).reason)
                        _state.value = State.Failed(msg)
                        Result.failure(IOException(msg))
                    }
                    else -> throw IOException("Resposta inesperada da TV")
                }
            } catch (e: IOException) {
                runCatching { s.close() }
                val msg = "Não foi possível conectar: ${e.message ?: "erro de rede"}"
                _state.value = State.Failed(msg)
                Result.failure(e)
            }
        }

    /** Thread-safe blocking write. Returns false if the link is down. */
    fun send(type: MessageType, payload: ByteArray = FrameCodec.EMPTY): Boolean {
        val ok = synchronized(writeLock) {
            val o = out
            if (o == null) {
                false
            } else {
                try {
                    FrameCodec.write(o, type, payload)
                    o.flush()
                    true
                } catch (e: IOException) {
                    false
                }
            }
        }
        if (!ok && socket != null) onLost()
        return ok
    }

    fun sendAsync(type: MessageType, payload: ByteArray = FrameCodec.EMPTY) {
        scope.launch(Dispatchers.IO) { send(type, payload) }
    }

    fun disconnect() {
        scope.launch(Dispatchers.IO) { close(sendBye = true, newState = State.Disconnected) }
    }

    private fun startReader(s: Socket, input: DataInputStream) {
        Thread({
            var bye = false
            try {
                while (!s.isClosed) {
                    val frame = FrameCodec.read(input)
                    when (frame.type) {
                        MessageType.PING -> send(MessageType.PONG, frame.payload)
                        MessageType.PONG -> _rttMs.value = (System.nanoTime() - Ping.decode(frame.payload)) / 1_000_000
                        MessageType.REQUEST_KEYFRAME -> listener?.onKeyframeRequested()
                        MessageType.SESSION_END -> listener?.onSessionEndedByTv()
                        MessageType.BYE -> { bye = true; break }
                        else -> Unit
                    }
                }
            } catch (_: IOException) {
            }
            if (socket === s) onLost(if (bye) "A TV encerrou a conexão" else "Conexão com a TV perdida")
        }, "nexa-link-reader").apply { isDaemon = true }.start()
    }

    private fun startPings() {
        pingJob?.cancel()
        pingJob = scope.launch(Dispatchers.IO) {
            while (isActive && socket != null) {
                send(MessageType.PING, Ping.encode(System.nanoTime()))
                delay(2_000)
            }
        }
    }

    private fun onLost(message: String = "Conexão com a TV perdida") {
        if (socket == null) return
        close(sendBye = false, newState = State.Failed(message))
        listener?.onLinkLost()
    }

    private fun close(sendBye: Boolean, newState: State) {
        pingJob?.cancel()
        pingJob = null
        val s = socket
        synchronized(writeLock) {
            if (sendBye) out?.let { o -> runCatching { FrameCodec.write(o, MessageType.BYE); o.flush() } }
            out = null
            socket = null
        }
        runCatching { s?.close() }
        _rttMs.value = null
        _state.value = newState
    }

    private fun rejectMessage(reason: RejectReason) = when (reason) {
        RejectReason.INVALID_CREDENTIAL -> "Código inválido. Confira o código exibido na TV."
        RejectReason.EXPIRED -> "Código expirado. A TV gerou um novo código."
        RejectReason.BUSY -> "A TV já está conectada a outro celular."
        RejectReason.VERSION_MISMATCH -> "Versões diferentes do NEXA no celular e na TV."
        RejectReason.TOO_MANY_ATTEMPTS -> "Muitas tentativas. A TV gerou um novo código."
    }
}
