package com.nexa.tv

import android.app.Application
import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import android.os.Build
import android.provider.Settings
import android.view.Surface
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.nexa.protocol.AudioConfig
import com.nexa.protocol.MessageType
import com.nexa.protocol.NexaProtocol
import com.nexa.protocol.PairingGate
import com.nexa.protocol.PairingInfo
import com.nexa.protocol.PairingUri
import com.nexa.protocol.VideoConfig
import com.nexa.protocol.VideoPacket
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

enum class Phase { WAITING, CONNECTED, STREAMING }

data class TvUiState(
    val code: String,
    val qrPayload: String?,
    val expiresAtMs: Long,
    val host: String?,
    val port: Int,
    val tvName: String,
    val phase: Phase = Phase.WAITING,
    val phoneName: String? = null,
    val appLabel: String? = null,
    val video: VideoConfig? = null,
    val paused: Boolean = false,
)

class TvViewModel(app: Application) : AndroidViewModel(app), TvServer.Events {

    private val gate = PairingGate()
    private val tvName = deviceName(app)
    private val server = TvServer(gate, tvName, this)
    private val decoder = VideoDecoder { server.sendAsync(MessageType.REQUEST_KEYFRAME) }
    private val audio = AudioPlayer()
    private val nsd = requireNotNull(app.getSystemService(NsdManager::class.java))
    private var nsdListener: NsdManager.RegistrationListener? = null

    private val _state = MutableStateFlow(buildPairingState(null))
    val state: StateFlow<TvUiState> = _state

    init {
        server.start()
        refreshPairing()
        registerNsd()
        // Rotate expired codes and pick up network changes while waiting for a phone.
        viewModelScope.launch {
            while (isActive) {
                delay(15_000)
                if (_state.value.phase == Phase.WAITING) {
                    gate.refreshIfExpired()
                    refreshPairing()
                }
            }
        }
    }

    fun newCode() {
        gate.regenerate()
        refreshPairing()
    }

    /** Ends the game session but keeps the phone paired. */
    fun endSession() {
        server.sendAsync(MessageType.SESSION_END)
        onSessionEnd()
    }

    /** Fully disconnects the phone; a new code is generated. */
    fun disconnect() {
        server.disconnectClient()
    }

    fun attachSurface(surface: Surface?) = decoder.setSurface(surface)

    // ---- TvServer.Events (network threads) ----
    override fun onPairingChanged() = refreshPairing()

    override fun onPhoneConnected(name: String) =
        _state.update { it.copy(phase = Phase.CONNECTED, phoneName = name) }

    override fun onPhoneDisconnected() {
        stopMedia()
        _state.update { buildPairingState(null) }
    }

    override fun onSessionStart(appLabel: String) =
        _state.update { it.copy(appLabel = appLabel, paused = false) }

    override fun onSessionEnd() {
        stopMedia()
        _state.update { it.copy(phase = if (it.phoneName != null) Phase.CONNECTED else Phase.WAITING, video = null, appLabel = null, paused = false) }
    }

    override fun onPaused(paused: Boolean) {
        audio.setPaused(paused)
        _state.update { it.copy(paused = paused) }
    }

    override fun onVideoConfig(config: VideoConfig) {
        decoder.setConfig(config)
        _state.update { it.copy(phase = Phase.STREAMING, video = config) }
    }

    override fun onVideoPacket(packet: VideoPacket) = decoder.submit(packet)
    override fun onAudioConfig(config: AudioConfig) = audio.configure(config)
    override fun onAudioPacket(data: ByteArray) = audio.write(data)

    private fun stopMedia() {
        decoder.release()
        audio.release()
    }

    private fun refreshPairing() {
        _state.update { current ->
            val fresh = buildPairingState(current)
            current.copy(code = fresh.code, qrPayload = fresh.qrPayload, expiresAtMs = fresh.expiresAtMs, host = fresh.host, port = fresh.port)
        }
    }

    private fun buildPairingState(previous: TvUiState?): TvUiState {
        val session = gate.current
        val host = NetworkInfo.localIpv4(getApplication<Application>())
        val port = server.port
        return TvUiState(
            code = session.code,
            qrPayload = host?.let { PairingUri.build(PairingInfo(it, port, session.token, tvName)) },
            expiresAtMs = session.createdAtMs + session.ttlMs,
            host = host,
            port = port,
            tvName = tvName,
            phase = previous?.phase ?: Phase.WAITING,
            phoneName = previous?.phoneName,
        )
    }

    private fun registerNsd() {
        val info = NsdServiceInfo().apply {
            serviceName = "NEXA TV"
            serviceType = NexaProtocol.NSD_SERVICE_TYPE
            port = server.port
        }
        val listener = object : NsdManager.RegistrationListener {
            override fun onServiceRegistered(serviceInfo: NsdServiceInfo) {}
            override fun onRegistrationFailed(serviceInfo: NsdServiceInfo, errorCode: Int) {}
            override fun onServiceUnregistered(serviceInfo: NsdServiceInfo) {}
            override fun onUnregistrationFailed(serviceInfo: NsdServiceInfo, errorCode: Int) {}
        }
        runCatching { nsd.registerService(info, NsdManager.PROTOCOL_DNS_SD, listener) }
            .onSuccess { nsdListener = listener }
    }

    override fun onCleared() {
        nsdListener?.let { runCatching { nsd.unregisterService(it) } }
        server.stop()
        stopMedia()
    }

    private companion object {
        fun deviceName(app: Application): String =
            runCatching { Settings.Global.getString(app.contentResolver, Settings.Global.DEVICE_NAME) }.getOrNull()
                ?.takeIf { it.isNotBlank() }
                ?: "${Build.MANUFACTURER} ${Build.MODEL}".trim()
    }
}
