package com.nexa.mobile.stream

import android.annotation.SuppressLint
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioPlaybackCaptureConfiguration
import android.media.AudioRecord
import android.media.projection.MediaProjection
import android.util.Log
import com.nexa.mobile.net.TvLink
import com.nexa.protocol.AudioConfig
import com.nexa.protocol.MessageType

/**
 * Captures what the game plays (AudioPlaybackCapture, Android 10+). This is not the
 * microphone. Apps can opt out of capture (allowAudioPlaybackCapture=false); those
 * stay silent on the TV.
 */
class AudioCapturer(private val projection: MediaProjection, private val link: TvLink) {
    private var record: AudioRecord? = null
    private var thread: Thread? = null
    @Volatile private var running = false
    @Volatile var paused = false

    /** Caller must hold RECORD_AUDIO. Returns false if capture could not start. */
    @SuppressLint("MissingPermission")
    fun start(): Boolean {
        val config = AudioPlaybackCaptureConfiguration.Builder(projection)
            .addMatchingUsage(AudioAttributes.USAGE_GAME)
            .addMatchingUsage(AudioAttributes.USAGE_MEDIA)
            .addMatchingUsage(AudioAttributes.USAGE_UNKNOWN)
            .build()
        val format = AudioFormat.Builder()
            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
            .setSampleRate(SAMPLE_RATE)
            .setChannelMask(AudioFormat.CHANNEL_IN_STEREO)
            .build()
        val minBuf = AudioRecord.getMinBufferSize(SAMPLE_RATE, AudioFormat.CHANNEL_IN_STEREO, AudioFormat.ENCODING_PCM_16BIT)
        val r = try {
            AudioRecord.Builder()
                .setAudioFormat(format)
                .setBufferSizeInBytes(maxOf(minBuf, CHUNK_BYTES * 4))
                .setAudioPlaybackCaptureConfig(config)
                .build()
        } catch (e: Exception) {
            Log.w(TAG, "Audio capture unavailable", e)
            return false
        }
        if (r.state != AudioRecord.STATE_INITIALIZED) {
            r.release()
            return false
        }
        r.startRecording()
        record = r
        link.send(MessageType.AUDIO_CONFIG, AudioConfig(SAMPLE_RATE, 2).encode())
        running = true
        thread = Thread({
            val buf = ByteArray(CHUNK_BYTES)
            while (running) {
                val n = r.read(buf, 0, buf.size)
                if (n > 0 && !paused) link.send(MessageType.AUDIO_PACKET, buf.copyOf(n))
                else if (n < 0) break
            }
        }, "nexa-audio-capture").also { it.start() }
        return true
    }

    fun stop() {
        running = false
        record?.let { runCatching { it.stop() } }
        thread?.join(300)
        thread = null
        record?.release()
        record = null
    }

    companion object {
        private const val TAG = "NexaAudio"
        const val SAMPLE_RATE = 48_000
        /** 10 ms of 48 kHz stereo PCM16. */
        const val CHUNK_BYTES = 480 * 2 * 2
    }
}
