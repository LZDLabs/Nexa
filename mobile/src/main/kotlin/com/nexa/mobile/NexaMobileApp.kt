package com.nexa.mobile

import android.app.Application
import android.content.Context
import com.nexa.mobile.controller.LayoutStore
import com.nexa.mobile.net.TvLink
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow

/** App-wide singletons shared by the UI (MainActivity) and the StreamingService. */
class NexaMobileApp : Application() {
    val appScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    lateinit var link: TvLink
        private set
    lateinit var layouts: LayoutStore
        private set
    lateinit var settings: SettingsStore
        private set

    /** Non-null while a streaming session is running. */
    val session = MutableStateFlow<RunningSession?>(null)

    override fun onCreate() {
        super.onCreate()
        link = TvLink(appScope, deviceName())
        layouts = LayoutStore(this)
        settings = SettingsStore(this)
    }

    private fun deviceName(): String {
        val model = android.os.Build.MODEL.orEmpty()
        val maker = android.os.Build.MANUFACTURER.orEmpty().replaceFirstChar { it.uppercase() }
        return if (model.startsWith(maker, ignoreCase = true)) model else "$maker $model".trim()
    }
}

data class RunningSession(val packageName: String, val appLabel: String)

val Context.nexa: NexaMobileApp get() = applicationContext as NexaMobileApp
