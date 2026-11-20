package com.nexa.mobile.stream

import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaFormat
import android.media.projection.MediaProjection
import android.os.Bundle
import android.util.Log
import android.view.Surface
import com.nexa.mobile.net.TvLink
import com.nexa.protocol.MessageType
import com.nexa.protocol.VideoConfig
import com.nexa.protocol.VideoPacket

/** MediaProjection -> VirtualDisplay -> H.264 (MediaCodec, hardware) -> TV. */
class VideoEncoder(
    private val projection: MediaProjection,
    private val width: Int,
    private val height: Int,
    private val dpi: Int,
    private val bitrate: Int,
    private val link: TvLink,
) {
    private var codec: MediaCodec? = null
    private var inputSurface: Surface? = null
    private var display: VirtualDisplay? = null
    private var thread: Thread? = null
    @Volatile private var running = false
    @Volatile private var paused = false
    /** After a pause, drop delta frames until the next key frame so the TV never sees a broken reference chain. */
    @Volatile private var awaitingKeyFrame = false

    fun setPaused(value: Boolean) {
        paused = value
        if (!value) {
            awaitingKeyFrame = true
            requestKeyframe()
        }
    }

    fun start() {
        val format = MediaFormat.createVideoFormat(MIME, width, height).apply {
            setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface)
            setInteger(MediaFormat.KEY_BIT_RATE, bitrate)
            setInteger(MediaFormat.KEY_FRAME_RATE, 60)
            setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 2)
            // Re-send the last frame when the screen is static so the TV keeps updating.
            setLong(MediaFormat.KEY_REPEAT_PREVIOUS_FRAME_AFTER, 100_000L)
            // SPS/PPS on every key frame: the TV can (re)start decoding from any key frame.
            setInteger(MediaFormat.KEY_PREPEND_HEADER_TO_SYNC_FRAMES, 1)
            setInteger(MediaFormat.KEY_PRIORITY, 0) // real-time
        }
        val c = MediaCodec.createEncoderByType(MIME)
        c.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
        val surface = c.createInputSurface()
        c.start()
        codec = c
        inputSurface = surface

        link.send(MessageType.VIDEO_CONFIG, VideoConfig(MIME, width, height).encode())
        display = projection.createVirtualDisplay(
            "NEXA", width, height, dpi,
            DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR, surface, null, null,
        )
        running = true
        thread = Thread({ drain(c) }, "nexa-video-encoder").also { it.start() }
    }

    private fun drain(c: MediaCodec) {
        val info = MediaCodec.BufferInfo()
        try {
            while (running) {
                val index = c.dequeueOutputBuffer(info, 10_000)
                if (index < 0) continue
                val buffer = c.getOutputBuffer(index)
                var flags = 0
                if (info.flags and MediaCodec.BUFFER_FLAG_KEY_FRAME != 0) flags = flags or VideoPacket.FLAG_KEY_FRAME
                if (info.flags and MediaCodec.BUFFER_FLAG_CODEC_CONFIG != 0) flags = flags or VideoPacket.FLAG_CONFIG
                if (awaitingKeyFrame && flags != 0) awaitingKeyFrame = false
                if (buffer != null && info.size > 0 && !paused && !awaitingKeyFrame) {
                    buffer.position(info.offset)
                    buffer.limit(info.offset + info.size)
                    val bytes = ByteArray(info.size)
                    buffer.get(bytes)
                    link.send(MessageType.VIDEO_PACKET, VideoPacket.encode(flags, info.presentationTimeUs, bytes))
                }
                c.releaseOutputBuffer(index, false)
                if (info.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM != 0) break
            }
        } catch (e: IllegalStateException) {
            Log.w(TAG, "Encoder stopped", e)
        }
    }

    fun requestKeyframe() {
        runCatching {
            codec?.setParameters(Bundle().apply { putInt(MediaCodec.PARAMETER_KEY_REQUEST_SYNC_FRAME, 0) })
        }
    }

    fun stop() {
        running = false
        thread?.join(500)
        thread = null
        display?.release()
        display = null
        codec?.let { c -> runCatching { c.stop() }; c.release() }
        codec = null
        inputSurface?.release()
        inputSurface = null
    }

    companion object {
        private const val TAG = "NexaVideo"
        const val MIME = MediaFormat.MIMETYPE_VIDEO_AVC
    }
}
