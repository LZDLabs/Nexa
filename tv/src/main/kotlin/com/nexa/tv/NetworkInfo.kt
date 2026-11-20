package com.nexa.tv

import android.content.Context
import android.net.ConnectivityManager
import java.net.Inet4Address

object NetworkInfo {
    /** IPv4 of the active local network (Wi-Fi or Ethernet), or null when offline. */
    fun localIpv4(context: Context): String? {
        val cm = context.getSystemService(ConnectivityManager::class.java) ?: return null
        val props = cm.getLinkProperties(cm.activeNetwork) ?: return null
        return props.linkAddresses
            .map { it.address }
            .firstOrNull { it is Inet4Address && !it.isLoopbackAddress }
            ?.hostAddress
    }
}
