package com.nexa.mobile

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

enum class StreamQuality(val label: String, val longEdge: Int, val bitrate: Int) {
    HD("720p • 6 Mbps", 1280, 6_000_000),
    FULL_HD("1080p • 10 Mbps", 1920, 10_000_000),
}

data class NexaSettings(val quality: StreamQuality, val audioEnabled: Boolean)

/** Local-only settings (V1 has no accounts and no cloud). */
class SettingsStore(context: Context) {
    private val prefs = context.getSharedPreferences("nexa_settings", Context.MODE_PRIVATE)
    private val _state = MutableStateFlow(read())
    val state: StateFlow<NexaSettings> = _state

    private fun read() = NexaSettings(
        quality = StreamQuality.entries.firstOrNull { it.name == prefs.getString("quality", null) } ?: StreamQuality.HD,
        audioEnabled = prefs.getBoolean("audio", true),
    )

    fun update(settings: NexaSettings) {
        prefs.edit().putString("quality", settings.quality.name).putBoolean("audio", settings.audioEnabled).apply()
        _state.value = settings
    }
}
