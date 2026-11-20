package com.nexa.protocol

import java.security.MessageDigest
import java.security.SecureRandom
import java.util.Base64

/**
 * Temporary pairing credentials shown by the TV. Never a permanent password:
 * a new session is generated on every TV start, on expiry, after too many
 * failed attempts and after every phone disconnects.
 */
class PairingSession(
    val token: String,
    val code: String,
    val createdAtMs: Long,
    val ttlMs: Long,
) {
    fun isExpired(nowMs: Long) = nowMs - createdAtMs >= ttlMs
    fun remainingMs(nowMs: Long) = (ttlMs - (nowMs - createdAtMs)).coerceAtLeast(0)

    companion object {
        const val DEFAULT_TTL_MS = 10 * 60_000L

        fun generate(nowMs: Long, random: SecureRandom = SecureRandom(), ttlMs: Long = DEFAULT_TTL_MS): PairingSession {
            val tokenBytes = ByteArray(16).also(random::nextBytes)
            val token = Base64.getUrlEncoder().withoutPadding().encodeToString(tokenBytes)
            val code = PairingCode.PREFIX + (1..PairingCode.LENGTH)
                .map { PairingCode.ALPHABET[random.nextInt(PairingCode.ALPHABET.length)] }
                .joinToString("")
            return PairingSession(token, code, nowMs, ttlMs)
        }
    }
}

/** Short human-typable code, formatted like the Gemini UI: `NX-8K4Q`. */
object PairingCode {
    const val PREFIX = "NX-"
    const val LENGTH = 4
    /** No 0/O/1/I to avoid confusion when reading from across the room. */
    const val ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"

    /** Accepts `nx8k4q`, `NX-8K4Q`, `8k4q`… Returns the canonical form or null if invalid. */
    fun normalize(input: String): String? {
        var s = input.uppercase().filter { it.isLetterOrDigit() }
        if (s.length == LENGTH + 2 && s.startsWith("NX")) s = s.substring(2)
        if (s.length != LENGTH || s.any { it !in ALPHABET }) return null
        return PREFIX + s
    }
}

enum class PairingVerdict { ACCEPTED, INVALID, EXPIRED, TOO_MANY_ATTEMPTS }

/** TV-side gate. Thread-safe. */
class PairingGate(
    private val clock: () -> Long = System::currentTimeMillis,
    private val maxFailures: Int = 5,
    private val random: SecureRandom = SecureRandom(),
) {
    @Volatile
    var current: PairingSession = PairingSession.generate(clock(), random)
        private set
    private var failures = 0

    @Synchronized
    fun regenerate(): PairingSession {
        failures = 0
        current = PairingSession.generate(clock(), random)
        return current
    }

    /** Regenerates if expired. Returns true when a new session was created. */
    @Synchronized
    fun refreshIfExpired(): Boolean {
        if (!current.isExpired(clock())) return false
        regenerate()
        return true
    }

    @Synchronized
    fun verify(token: String?, code: String?): PairingVerdict {
        val session = current
        if (session.isExpired(clock())) {
            regenerate()
            return PairingVerdict.EXPIRED
        }
        val tokenOk = token != null && constantTimeEquals(token, session.token)
        val codeOk = code != null && PairingCode.normalize(code)?.let { constantTimeEquals(it, session.code) } == true
        if (tokenOk || codeOk) {
            failures = 0
            return PairingVerdict.ACCEPTED
        }
        failures++
        if (failures >= maxFailures) {
            regenerate()
            return PairingVerdict.TOO_MANY_ATTEMPTS
        }
        return PairingVerdict.INVALID
    }

    private fun constantTimeEquals(a: String, b: String) =
        MessageDigest.isEqual(a.toByteArray(), b.toByteArray())
}
