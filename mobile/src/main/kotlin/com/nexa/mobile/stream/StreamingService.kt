package com.nexa.mobile.stream

import android.Manifest
import android.app.Activity
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ServiceInfo
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import android.view.WindowManager
import android.widget.Toast
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import androidx.core.content.IntentCompat
import com.nexa.mobile.MainActivity
import com.nexa.mobile.R
import com.nexa.mobile.RunningSession
import com.nexa.mobile.controller.ControllerPad
import com.nexa.mobile.controller.UnavailableInputBridge
import com.nexa.mobile.net.TvLink
import com.nexa.mobile.nexa
import com.nexa.protocol.MessageType
import com.nexa.protocol.SessionStart
import com.nexa.ui.NexaTheme
import java.util.concurrent.Executors
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

/**
 * Foreground service that owns a streaming session:
 * screen capture (video + game audio) -> TV, and the black controller overlay on the phone.
 */
class StreamingService : Service() {

    private val main = Handler(Looper.getMainLooper())
    /** All socket writes and codec setup/teardown run here: Android forbids network I/O on the main thread. */
    private val worker = Executors.newSingleThreadExecutor()
    private var projection: MediaProjection? = null
    @Volatile private var video: VideoEncoder? = null
    @Volatile private var audio: AudioCapturer? = null
    private var overlay: ControllerOverlay? = null
    private var stopping = false

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP) {
            stopSession(notifyTv = true)
            return START_NOT_STICKY
        }
        // Android 14+: the typed foreground service must be running before getMediaProjection().
        startInForeground()

        val resultCode = intent?.getIntExtra(EXTRA_RESULT_CODE, Activity.RESULT_CANCELED) ?: Activity.RESULT_CANCELED
        val data = intent?.let { IntentCompat.getParcelableExtra(it, EXTRA_DATA, Intent::class.java) }
        val pkg = intent?.getStringExtra(EXTRA_PACKAGE)
        val label = intent?.getStringExtra(EXTRA_LABEL).orEmpty()
        val app = nexa
        if (projection != null || data == null || resultCode != Activity.RESULT_OK || pkg == null || !app.link.isConnected) {
            if (projection == null) stopSelf()
            return START_NOT_STICKY
        }

        try {
            startSession(resultCode, data, pkg, label)
        } catch (e: Exception) {
            Log.e(TAG, "Could not start session", e)
            stopSession(notifyTv = true)
            Toast.makeText(this, "Falha ao iniciar a captura: ${e.message}", Toast.LENGTH_LONG).show()
        }
        return START_NOT_STICKY
    }

    private fun startSession(resultCode: Int, data: Intent, pkg: String, label: String) {
        stopping = false
        val app = nexa
        val mpm = requireNotNull(getSystemService(MediaProjectionManager::class.java))
        val mp = mpm.getMediaProjection(resultCode, data) ?: error("MediaProjection recusada")
        // Android 14+ requires the callback before createVirtualDisplay().
        mp.registerCallback(object : MediaProjection.Callback() {
            override fun onStop() { main.post { stopSession(notifyTv = true) } }
        }, main)
        projection = mp

        val settings = app.settings.state.value
        val (w, h) = captureSize(settings.quality.longEdge)
        val dpi = resources.displayMetrics.densityDpi
        val canRecord = ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
        worker.execute {
            try {
                app.link.send(MessageType.SESSION_START, SessionStart(label).encode())
                video = VideoEncoder(mp, w, h, dpi, settings.quality.bitrate, app.link).also { it.start() }
                if (settings.audioEnabled && canRecord) {
                    audio = AudioCapturer(mp, app.link).takeIf { it.start() }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Could not start capture", e)
                main.post {
                    stopSession(notifyTv = true)
                    Toast.makeText(this, "Falha ao iniciar a captura: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }

        app.link.listener = object : TvLink.Listener {
            override fun onKeyframeRequested() { video?.requestKeyframe() }
            override fun onSessionEndedByTv() { main.post { stopSession(notifyTv = false) } }
            override fun onLinkLost() { main.post { stopSession(notifyTv = false) } }
        }

        overlay = ControllerOverlay(this).also { ov ->
            ov.show {
                NexaTheme {
                    var layout by remember { mutableStateOf(app.layouts.load(pkg)) }
                    var customizing by remember { mutableStateOf(false) }
                    var paused by remember { mutableStateOf(false) }
                    val rtt by app.link.rttMs.collectAsState()
                    val bridge = UnavailableInputBridge
                    ControllerPad(
                        layout = layout,
                        onLayoutChange = { layout = it },
                        customizing = customizing,
                        onToggleCustomize = {
                            if (customizing) app.layouts.save(pkg, layout)
                            customizing = !customizing
                        },
                        statusText = "TV Conectada • ${rtt?.let { "$it ms" } ?: "— ms"}",
                        footerText = "$label • Vídeo ativo exclusivamente na TV",
                        endLabel = "Encerrar",
                        onEnd = { stopSession(notifyTv = true) },
                        onEvent = bridge::send,
                        paused = paused,
                        onTogglePause = {
                            paused = !paused
                            video?.setPaused(paused)
                            audio?.paused = paused
                            app.link.sendAsync(if (paused) MessageType.SESSION_PAUSE else MessageType.SESSION_RESUME)
                        },
                        notice = if (bridge.isAvailable) null else bridge.statusText,
                    )
                }
            }
        }
        app.session.value = RunningSession(pkg, label)
    }

    /** Landscape capture size, aligned to 16 px, long edge capped by the quality setting. */
    private fun captureSize(maxLongEdge: Int): Pair<Int, Int> {
        val wm = requireNotNull(getSystemService(WindowManager::class.java))
        val (sw, sh) = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            wm.maximumWindowMetrics.bounds.let { it.width() to it.height() }
        } else {
            val dm = android.util.DisplayMetrics()
            @Suppress("DEPRECATION") wm.defaultDisplay.getRealMetrics(dm)
            dm.widthPixels to dm.heightPixels
        }
        val long = max(sw, sh)
        val short = min(sw, sh)
        val scale = min(1f, maxLongEdge.toFloat() / long)
        fun align(v: Float) = max(16, (v / 16f).roundToInt() * 16)
        return align(long * scale) to align(short * scale)
    }

    private fun stopSession(notifyTv: Boolean) {
        if (stopping) return
        stopping = true
        val app = nexa
        app.link.listener = null
        overlay?.hide(); overlay = null
        val mp = projection
        projection = null
        // Runs after any pending start on the same worker, so nothing is left half-started.
        worker.execute {
            audio?.stop(); audio = null
            video?.stop(); video = null
            mp?.let { runCatching { it.stop() } }
            if (notifyTv) app.link.send(MessageType.SESSION_END)
        }
        val wasRunning = app.session.value != null
        app.session.value = null
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
        if (wasRunning) {
            // Allowed from the background because the user granted "display over other apps".
            runCatching {
                startActivity(Intent(this, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_REORDER_TO_FRONT))
            }
        }
    }

    override fun onDestroy() {
        stopSession(notifyTv = true)
        worker.shutdown() // already-queued teardown still runs
        super.onDestroy()
    }

    private fun startInForeground() {
        val nm = requireNotNull(getSystemService(NotificationManager::class.java))
        nm.createNotificationChannel(NotificationChannel(CHANNEL, "Transmissão NEXA", NotificationManager.IMPORTANCE_LOW))
        val stopIntent = PendingIntent.getService(
            this, 1, Intent(this, StreamingService::class.java).setAction(ACTION_STOP),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )
        val notification = Notification.Builder(this, CHANNEL)
            .setSmallIcon(R.drawable.ic_nexa_notification)
            .setContentTitle("NEXA transmitindo para a TV")
            .setContentText("A tela e o áudio do jogo estão sendo enviados pela rede local.")
            .setOngoing(true)
            .addAction(Notification.Action.Builder(null, "Encerrar", stopIntent).build())
            .build()
        startForeground(NOTIFICATION_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PROJECTION)
    }

    companion object {
        private const val TAG = "NexaStream"
        private const val CHANNEL = "nexa_stream"
        private const val NOTIFICATION_ID = 42
        private const val ACTION_STOP = "com.nexa.mobile.STOP"
        private const val EXTRA_RESULT_CODE = "result_code"
        private const val EXTRA_DATA = "data"
        private const val EXTRA_PACKAGE = "package"
        private const val EXTRA_LABEL = "label"

        fun start(context: Context, resultCode: Int, data: Intent, packageName: String, label: String) {
            val intent = Intent(context, StreamingService::class.java)
                .putExtra(EXTRA_RESULT_CODE, resultCode)
                .putExtra(EXTRA_DATA, data)
                .putExtra(EXTRA_PACKAGE, packageName)
                .putExtra(EXTRA_LABEL, label)
            ContextCompat.startForegroundService(context, intent)
        }

        fun stop(context: Context) {
            context.startService(Intent(context, StreamingService::class.java).setAction(ACTION_STOP))
        }
    }
}
