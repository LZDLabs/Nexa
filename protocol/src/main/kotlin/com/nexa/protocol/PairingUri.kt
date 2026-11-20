package com.nexa.protocol

import java.net.URLDecoder
import java.net.URLEncoder

/** What the TV QR code carries. Contains only the temporary token, never a password. */
data class PairingInfo(val host: String, val port: Int, val token: String, val tvName: String)

object PairingUri {
    private const val PREFIX = "nexa://pair?"

    fun build(info: PairingInfo): String = PREFIX + listOf(
        "v" to NexaProtocol.VERSION.toString(),
        "h" to info.host,
        "p" to info.port.toString(),
        "t" to info.token,
        "n" to info.tvName,
    ).joinToString("&") { (k, v) -> "$k=${URLEncoder.encode(v, "UTF-8")}" }

    fun parse(raw: String): PairingInfo? {
        val text = raw.trim()
        if (!text.startsWith(PREFIX, ignoreCase = true)) return null
        val params = text.substring(PREFIX.length).split('&').mapNotNull { part ->
            val i = part.indexOf('=')
            if (i <= 0) null else part.substring(0, i) to runCatching { URLDecoder.decode(part.substring(i + 1), "UTF-8") }.getOrNull()
        }.toMap()
        val host = params["h"]?.takeIf { it.isNotBlank() } ?: return null
        val port = params["p"]?.toIntOrNull()?.takeIf { it in 1..65535 } ?: return null
        val token = params["t"]?.takeIf { it.length >= 16 } ?: return null
        return PairingInfo(host, port, token, params["n"].orEmpty())
    }
}
