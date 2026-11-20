package com.nexa.mobile.net

import android.content.Context
import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import com.nexa.protocol.NexaProtocol
import kotlinx.coroutines.delay
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.Collections
import kotlin.coroutines.resume

/**
 * Finds NEXA TVs on the local network via DNS-SD (used for the manual code path).
 * The TV only advertises "NEXA TV" + port; the code itself is never broadcast.
 */
class TvDiscovery(context: Context) {
    private val nsd = requireNotNull(context.getSystemService(NsdManager::class.java))

    data class Candidate(val name: String, val host: String, val port: Int)

    suspend fun find(scanMs: Long = 3_000): List<Candidate> {
        val found = Collections.synchronizedList(mutableListOf<NsdServiceInfo>())
        val listener = object : NsdManager.DiscoveryListener {
            override fun onServiceFound(info: NsdServiceInfo) { found.add(info) }
            override fun onServiceLost(info: NsdServiceInfo) {}
            override fun onDiscoveryStarted(serviceType: String) {}
            override fun onDiscoveryStopped(serviceType: String) {}
            override fun onStartDiscoveryFailed(serviceType: String, errorCode: Int) {}
            override fun onStopDiscoveryFailed(serviceType: String, errorCode: Int) {}
        }
        nsd.discoverServices(NexaProtocol.NSD_SERVICE_TYPE, NsdManager.PROTOCOL_DNS_SD, listener)
        try {
            delay(scanMs)
        } finally {
            runCatching { nsd.stopServiceDiscovery(listener) }
        }
        // Resolve one at a time (concurrent resolves fail before Android 14).
        return found.toList().distinctBy { it.serviceName }.mapNotNull { resolve(it) }
    }

    @Suppress("DEPRECATION")
    private suspend fun resolve(info: NsdServiceInfo): Candidate? = suspendCancellableCoroutine { cont ->
        nsd.resolveService(info, object : NsdManager.ResolveListener {
            override fun onResolveFailed(serviceInfo: NsdServiceInfo, errorCode: Int) {
                if (cont.isActive) cont.resume(null)
            }

            override fun onServiceResolved(serviceInfo: NsdServiceInfo) {
                val host = serviceInfo.host?.hostAddress
                if (cont.isActive) cont.resume(host?.let { Candidate(serviceInfo.serviceName, it, serviceInfo.port) })
            }
        })
    }
}
