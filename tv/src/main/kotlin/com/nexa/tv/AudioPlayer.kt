package com.nexa.tv

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import com.nexa.protocol.AudioConfig

/** Plays the phone's PCM stream. Non-blocking writes: late audio is dropped, not queued. */
class AudioPlayer {
    @Volatile private var track: AudioTrack? = null

    @Synchronized
    fun configure(config: AudioConfig) {
        release()
        val mask = if (config.channelCount == 1) AudioFormat.CHANNEL_OUT_MONO else AudioFormat.CHANNEL_OUT_STEREO
        val min = AudioTrack.getMinBufferSize(config.sampleRate, mask, AudioFormat.ENCODING_PCM_16BIT)
        val target = config.sampleRate * config.channelCount * 2 / 10 // ~100 ms
        track = AudioTrack.Builder()
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_GAME)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build()
            )
            .setAudioFormat(
                AudioFormat.Builder()
                    .setSampleRate(config.sampleRate)
                    .setChannelMask(mask)
                    .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                    .build()
            )
            .setBufferSizeInBytes(maxOf(min, target))
            .setTransferMode(AudioTrack.MODE_STREAM)
            .setPerformanceMode(AudioTrack.PERFORMANCE_MODE_LOW_LATENCY)
            .build()
            .also { it.play() }
    }

    fun write(data: ByteArray) {
        track?.write(data, 0, data.size, AudioTrack.WRITE_NON_BLOCKING)
    }

    fun setPaused(paused: Boolean) {
        val t = track ?: return
        runCatching { if (paused) { t.pause(); t.flush() } else t.play() }
    }

    @Synchronized
    fun release() {
        track?.let { runCatching { it.stop() }; it.release() }
        track = null
    }
}
