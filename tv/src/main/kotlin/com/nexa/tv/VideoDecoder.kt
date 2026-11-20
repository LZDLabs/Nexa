package com.nexa.tv

import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaFormat
import android.os.Build
import android.util.Log
import android.view.Surface
import com.nexa.protocol.VideoConfig
import com.nexa.protocol.VideoPacket
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.TimeUnit

/**
 * Hardware H.264 decoder rendering straight to the full-screen SurfaceView.
 * When it falls behind it drops frames and asks the phone for a new key frame,
 * favouring low latency over completeness.
 */
class VideoDecoder(private val requestKeyFrame: () -> Unit) {
    private val queue = LinkedBlockingQueue<VideoPacket>(MAX_QUEUE)
    private val lock = Any()
    private var config: VideoConfig? = null
    private var surface: Surface? = null
    @Volatile private var codec: MediaCodec? = null
    private var thread: Thread? = null
    @Volatile private var running = false
    @Volatile private var waitingForKeyFrame = true
    @Volatile var framesRendered = 0L
        private set

    fun setConfig(c: VideoConfig) = synchronized(lock) {
        if (c == config && codec != null) return@synchronized
        config = c
        restart()
    }

    fun setSurface(s: Surface?) = synchronized(lock) {
        surface = s
        restart()
    }

    fun submit(packet: VideoPacket) {
        if (codec == null) return
        if (!queue.offer(packet)) {
            queue.clear()
            waitingForKeyFrame = true
            requestKeyFrame()
        }
    }

    fun release() = synchronized(lock) {
        config = null
        surface = null
        stopCodec()
    }

    private fun restart() {
        stopCodec()
        val c = config ?: return
        val s = surface ?: return
        if (!s.isValid || c.width <= 0 || c.height <= 0) return
        try {
            val codec = MediaCodec.createDecoderByType(c.mime)
            val format = MediaFormat.createVideoFormat(c.mime, c.width, c.height)
            val caps = codec.codecInfo.getCapabilitiesForType(c.mime)
            if (Build.VERSION.SDK_INT >= 30 &&
                caps.isFeatureSupported(MediaCodecInfo.CodecCapabilities.FEATURE_LowLatency)
            ) {
                format.setInteger(MediaFormat.KEY_LOW_LATENCY, 1)
            }
            codec.configure(format, s, null, 0)
            codec.start()
            this.codec = codec
            framesRendered = 0
            waitingForKeyFrame = true
            queue.clear()
            running = true
            thread = Thread({ loop(codec) }, "nexa-video-decoder").also { it.start() }
            requestKeyFrame()
        } catch (e: Exception) {
            Log.e(TAG, "Decoder start failed", e)
            stopCodec()
        }
    }

    private fun stopCodec() {
        running = false
        thread?.join(500)
        thread = null
        codec?.let { c ->
            runCatching { c.stop() }
            runCatching { c.release() }
        }
        codec = null
        queue.clear()
    }

    private fun loop(c: MediaCodec) {
        val info = MediaCodec.BufferInfo()
        try {
            while (running) {
                val p = queue.poll(5, TimeUnit.MILLISECONDS)
                if (p != null) feed(c, p)
                while (true) {
                    val out = c.dequeueOutputBuffer(info, 0)
                    if (out >= 0) {
                        c.releaseOutputBuffer(out, true)
                        framesRendered++
                    } else if (out == MediaCodec.INFO_TRY_AGAIN_LATER) {
                        break
                    }
                }
            }
        } catch (e: IllegalStateException) {
            Log.w(TAG, "Decoder loop ended", e)
        }
    }

    private fun feed(c: MediaCodec, p: VideoPacket) {
        if (waitingForKeyFrame) {
            if (!p.isKeyFrame && !p.isConfig) return
            if (p.isKeyFrame) waitingForKeyFrame = false
        }
        val index = c.dequeueInputBuffer(10_000)
        if (index < 0) {
            waitingForKeyFrame = true
            requestKeyFrame()
            return
        }
        val buffer = c.getInputBuffer(index) ?: return
        buffer.clear()
        if (p.data.size > buffer.capacity()) {
            c.queueInputBuffer(index, 0, 0, p.ptsUs, 0)
            waitingForKeyFrame = true
            requestKeyFrame()
            return
        }
        buffer.put(p.data)
        val flags = if (p.isConfig) MediaCodec.BUFFER_FLAG_CODEC_CONFIG else 0
        c.queueInputBuffer(index, 0, p.data.size, p.ptsUs, flags)
    }

    private companion object {
        const val TAG = "NexaDecoder"
        const val MAX_QUEUE = 90
    }
}
