package com.nexa.tv

import android.util.Log
import com.nexa.protocol.AudioConfig
import com.nexa.protocol.FrameCodec
import com.nexa.protocol.Hello
import com.nexa.protocol.HelloOk
import com.nexa.protocol.HelloReject
import com.nexa.protocol.MessageType
import com.nexa.protocol.NexaProtocol
import com.nexa.protocol.PairingGate
import com.nexa.protocol.PairingVerdict
import com.nexa.protocol.RejectReason
import com.nexa.protocol.SessionStart
import com.nexa.protocol.VideoConfig
import com.nexa.protocol.VideoPacket
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.IOException
import java.net.InetSocketAddress
import java.net.ServerSocket
import java.net.Socket
import java.util.concurrent.Executors

/**
 * TV-side receiver. Accepts one phone at a time, authenticated with the
 * temporary pairing token/code from [PairingGate].
 */
class TvServer(
    private val gate: PairingGate,
    private val tvName: String,
    private val events: Events,
) {
    interface Events {
        fun onPairingChanged()
        fun onPhoneConnected(name: String)
        fun onPhoneDisconnected()
        fun onSessionStart(appLabel: String)
        fun onSessionEnd()
        fun onPaused(paused: Boolean)
        fun onVideoConfig(config: VideoConfig)
        fun onVideoPacket(packet: VideoPacket)
        fun onAudioConfig(config: AudioConfig)
        fun onAudioPacket(data: ByteArray)
    }

    private var server: ServerSocket? = null
    @Volatile private var running = false
    @Volatile private var client: Socket? = null
    @Volatile private var out: DataOutputStream? = null
    private val writeLock = Any()
    /** UI-originated writes go through here: network I/O is not allowed on the main thread. */
    private val writer = Executors.newSingleThreadExecutor()

    val port: Int get() = server?.localPort ?: NexaProtocol.DEFAULT_PORT

    fun start() {
        val s = ServerSocket().apply { reuseAddress = true }
        try {
            s.bind(InetSocketAddress(NexaProtocol.DEFAULT_PORT))
        } catch (e: IOException) {
            s.bind(InetSocketAddress(0)) // port busy: any free port (the QR carries it)
        }
        server = s
        running = true
        Thread({ acceptLoop(s) }, "nexa-accept").apply { isDaemon = true }.start()
    }

    fun stop() {
        running = false
        val s = server
        server = null
        disconnectClient()
        writer.execute { runCatching { s?.close() } }
        writer.shutdown()
    }

    /** Safe from any thread, including the main thread. */
    fun sendAsync(type: MessageType, payload: ByteArray = FrameCodec.EMPTY) {
        runCatching { writer.execute { send(type, payload) } }
    }

    /** Blocking write: only call from a background thread. */
    private fun send(type: MessageType, payload: ByteArray = FrameCodec.EMPTY): Boolean = synchronized(writeLock) {
        val o = out ?: return@synchronized false
        try {
            FrameCodec.write(o, type, payload)
            o.flush()
            true
        } catch (e: IOException) {
            false
        }
    }

    fun disconnectClient() {
        runCatching {
            writer.execute {
                send(MessageType.BYE)
                runCatching { client?.close() }
            }
        }
    }

    private fun acceptLoop(s: ServerSocket) {
        while (running) {
            val socket = try { s.accept() } catch (e: IOException) { break }
            Thread({ handle(socket) }, "nexa-client").apply { isDaemon = true }.start()
        }
    }

    private fun handle(socket: Socket) {
        try {
            socket.tcpNoDelay = true
            socket.soTimeout = 8_000
            val input = DataInputStream(BufferedInputStream(socket.getInputStream(), 256 * 1024))
            val output = DataOutputStream(BufferedOutputStream(socket.getOutputStream(), 16 * 1024))

            val first = FrameCodec.read(input)
            if (first.type != MessageType.HELLO) return
            val hello = Hello.decode(first.payload)

            val reject: RejectReason? = when {
                hello.version != NexaProtocol.VERSION -> RejectReason.VERSION_MISMATCH
                client != null -> RejectReason.BUSY
                else -> when (gate.verify(hello.token, hello.code)) {
                    PairingVerdict.ACCEPTED -> null
                    PairingVerdict.INVALID -> RejectReason.INVALID_CREDENTIAL
                    PairingVerdict.EXPIRED -> RejectReason.EXPIRED.also { events.onPairingChanged() }
                    PairingVerdict.TOO_MANY_ATTEMPTS -> RejectReason.TOO_MANY_ATTEMPTS.also { events.onPairingChanged() }
                }
            }
            if (reject != null) {
                FrameCodec.write(output, MessageType.HELLO_REJECT, HelloReject(reject).encode())
                output.flush()
                return
            }

            synchronized(writeLock) {
                client = socket
                out = output
                FrameCodec.write(output, MessageType.HELLO_OK, HelloOk(tvName).encode())
                output.flush()
            }
            // Credentials are single-use: rotate as soon as a phone is paired.
            gate.regenerate()
            events.onPairingChanged()
            events.onPhoneConnected(hello.deviceName.ifBlank { "Celular Android" })
            socket.soTimeout = 15_000 // the phone pings every 2 s

            loop@ while (true) {
                val frame = FrameCodec.read(input)
                when (frame.type) {
                    MessageType.PING -> send(MessageType.PONG, frame.payload)
                    MessageType.SESSION_START -> events.onSessionStart(SessionStart.decode(frame.payload).appLabel)
                    MessageType.SESSION_END -> events.onSessionEnd()
                    MessageType.SESSION_PAUSE -> events.onPaused(true)
                    MessageType.SESSION_RESUME -> events.onPaused(false)
                    MessageType.VIDEO_CONFIG -> events.onVideoConfig(VideoConfig.decode(frame.payload))
                    MessageType.VIDEO_PACKET -> events.onVideoPacket(VideoPacket.decode(frame.payload))
                    MessageType.AUDIO_CONFIG -> events.onAudioConfig(AudioConfig.decode(frame.payload))
                    MessageType.AUDIO_PACKET -> events.onAudioPacket(frame.payload)
                    MessageType.BYE -> break@loop
                    else -> Unit
                }
            }
        } catch (e: IOException) {
            Log.d(TAG, "Client closed: ${e.message}")
        } finally {
            runCatching { socket.close() }
            val wasClient = synchronized(writeLock) {
                if (client === socket) {
                    client = null
                    out = null
                    true
                } else false
            }
            if (wasClient) {
                gate.regenerate()
                events.onPairingChanged()
                events.onPhoneDisconnected()
            }
        }
    }

    private companion object { const val TAG = "NexaTvServer" }
}
