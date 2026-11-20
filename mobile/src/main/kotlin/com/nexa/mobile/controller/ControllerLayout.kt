package com.nexa.mobile.controller

import android.content.Context
import org.json.JSONObject

/** Movable controls (L1/R1 stay fixed in the top row, as in the Gemini design). */
enum class ControlId(val label: String, val description: String) {
    JOYSTICK("Analógico", "Movimento"),
    A("A", "Botão A"),
    B("B", "Botão B"),
    X("X", "Botão X"),
    Y("Y", "Botão Y"),
}

/** Position in percent of the pad (0–100) and a scale factor. */
data class ControlPlacement(val x: Float, val y: Float, val size: Float = 1f)

data class ControllerLayout(val controls: Map<ControlId, ControlPlacement>) {
    operator fun get(id: ControlId): ControlPlacement = controls[id] ?: DEFAULT.controls.getValue(id)
    fun with(id: ControlId, placement: ControlPlacement) = ControllerLayout(controls + (id to placement))

    companion object {
        const val MIN_SIZE = 0.8f
        const val MAX_SIZE = 1.35f

        /** Default positions from the Gemini prototype. */
        val DEFAULT = ControllerLayout(
            mapOf(
                ControlId.JOYSTICK to ControlPlacement(18f, 58f),
                ControlId.A to ControlPlacement(82f, 72f),
                ControlId.B to ControlPlacement(91f, 56f),
                ControlId.X to ControlPlacement(73f, 56f),
                ControlId.Y to ControlPlacement(82f, 40f),
            )
        )
    }
}

/** Saves layouts locally, one per game (package name) with a global fallback. */
class LayoutStore(context: Context) {
    private val prefs = context.getSharedPreferences("nexa_layouts", Context.MODE_PRIVATE)

    fun load(packageName: String?): ControllerLayout =
        (packageName?.let { read(key(it)) } ?: read(DEFAULT_KEY)) ?: ControllerLayout.DEFAULT

    fun save(packageName: String?, layout: ControllerLayout) {
        val json = JSONObject()
        layout.controls.forEach { (id, p) ->
            json.put(id.name, JSONObject().put("x", p.x.toDouble()).put("y", p.y.toDouble()).put("s", p.size.toDouble()))
        }
        prefs.edit().putString(packageName?.let(::key) ?: DEFAULT_KEY, json.toString()).apply()
    }

    fun hasCustomLayout(packageName: String) = prefs.contains(key(packageName))

    private fun read(key: String): ControllerLayout? {
        val raw = prefs.getString(key, null) ?: return null
        return runCatching {
            val json = JSONObject(raw)
            ControllerLayout(ControlId.entries.associateWith { id ->
                val o = json.optJSONObject(id.name)
                if (o == null) ControllerLayout.DEFAULT[id]
                else ControlPlacement(
                    o.getDouble("x").toFloat(),
                    o.getDouble("y").toFloat(),
                    o.optDouble("s", 1.0).toFloat().coerceIn(ControllerLayout.MIN_SIZE, ControllerLayout.MAX_SIZE),
                )
            })
        }.getOrNull()
    }

    private fun key(pkg: String) = "layout:$pkg"

    private companion object { const val DEFAULT_KEY = "layout:__default__" }
}
