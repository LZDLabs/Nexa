package com.nexa.protocol

import java.net.URLDecoder
import java.net.URLEncoder
import java.nio.ByteBuffer
import java.nio.charset.StandardCharsets.UTF_8

/** Tiny `key=value` line format for control messages (no JSON dependency needed). */
object KeyValue {
    fun encode(values: Map<String, String?>): ByteArray =
        values.entries
            .filter { it.value != null }
            .joinToString("\n") { "${enc(it.key)}=${enc(it.value!!)}" }
            .toByteArray(UTF_8)

    fun decode(bytes: ByteArray): Map<String, String> =
        String(bytes, UTF_8).split('\n')
            .filter { it.isNotBlank() && it.contains('=') }
            .associate { line ->
                val i = line.indexOf('=')
                dec(line.substring(0, i)) to dec(line.substring(i + 1))
            }

    private fun enc(s: String) = URLEncoder.encode(s, "UTF-8")
    private fun dec(s: String) = URLDecoder.decode(s, "UTF-8")
}

/** Phone -> TV. Exactly one of [token] (from QR) or [code] (typed) is expected. */
data class Hello(
    val version: Int,
    val deviceName: String,
    val token: String? = null,
    val code: String? = null,
) {
    fun encode() = KeyValue.encode(mapOf("v" to version.toString(), "n" to deviceName, "t" to token, "c" to code))

    companion object {
        fun decode(bytes: ByteArray): Hello {
            val m = KeyValue.decode(bytes)
            return Hello(
                version = m["v"]?.toIntOrNull() ?: 0,
                deviceName = m["n"].orEmpty().take(64),
                token = m["t"],
                code = m["c"],
            )
        }
    }
}

data class HelloOk(val tvName: String) {
    fun encode() = KeyValue.encode(mapOf("n" to tvName))
    companion object {
        fun decode(bytes: ByteArray) = HelloOk(KeyValue.decode(bytes)["n"].orEmpty())
    }
}

enum class RejectReason { INVALID_CREDENTIAL, EXPIRED, BUSY, VERSION_MISMATCH, TOO_MANY_ATTEMPTS }

data class HelloReject(val reason: RejectReason) {
    fun encode() = KeyValue.encode(mapOf("r" to reason.name))
    companion object {
        fun decode(bytes: ByteArray): HelloReject {
            val r = KeyValue.decode(bytes)["r"]
            return HelloReject(RejectReason.entries.firstOrNull { it.name == r } ?: RejectReason.INVALID_CREDENTIAL)
        }
    }
}

data class SessionStart(val appLabel: String) {
    fun encode() = KeyValue.encode(mapOf("a" to appLabel))
    companion object {
        fun decode(bytes: ByteArray) = SessionStart(KeyValue.decode(bytes)["a"].orEmpty().take(80))
    }
}

data class VideoConfig(val mime: String, val width: Int, val height: Int) {
    fun encode() = KeyValue.encode(mapOf("m" to mime, "w" to width.toString(), "h" to height.toString()))
    companion object {
        fun decode(bytes: ByteArray): VideoConfig {
            val m = KeyValue.decode(bytes)
            return VideoConfig(m["m"].orEmpty(), m["w"]?.toIntOrNull() ?: 0, m["h"]?.toIntOrNull() ?: 0)
        }
    }
}

/** PCM 16-bit little endian. */
data class AudioConfig(val sampleRate: Int, val channelCount: Int) {
    fun encode() = KeyValue.encode(mapOf("r" to sampleRate.toString(), "c" to channelCount.toString()))
    companion object {
        fun decode(bytes: ByteArray): AudioConfig {
            val m = KeyValue.decode(bytes)
            return AudioConfig(m["r"]?.toIntOrNull() ?: 48_000, m["c"]?.toIntOrNull() ?: 2)
        }
    }
}

/** One encoded access unit: `[flags: u8][ptsUs: i64][data]`. */
class VideoPacket(val flags: Int, val ptsUs: Long, val data: ByteArray) {
    val isKeyFrame get() = flags and FLAG_KEY_FRAME != 0
    val isConfig get() = flags and FLAG_CONFIG != 0

    companion object {
        const val FLAG_KEY_FRAME = 1
        const val FLAG_CONFIG = 2
        private const val HEADER = 9

        fun encode(flags: Int, ptsUs: Long, data: ByteArray, offset: Int = 0, length: Int = data.size): ByteArray {
            val out = ByteArray(HEADER + length)
            ByteBuffer.wrap(out).put(flags.toByte()).putLong(ptsUs)
            System.arraycopy(data, offset, out, HEADER, length)
            return out
        }

        fun decode(bytes: ByteArray): VideoPacket {
            if (bytes.size < HEADER) throw ProtocolException("Video packet too short")
            val bb = ByteBuffer.wrap(bytes)
            val flags = bb.get().toInt() and 0xFF
            val pts = bb.getLong()
            return VideoPacket(flags, pts, bytes.copyOfRange(HEADER, bytes.size))
        }
    }
}

object Ping {
    fun encode(nanos: Long): ByteArray = ByteBuffer.allocate(8).putLong(nanos).array()
    fun decode(bytes: ByteArray): Long = if (bytes.size >= 8) ByteBuffer.wrap(bytes).getLong() else 0L
}
