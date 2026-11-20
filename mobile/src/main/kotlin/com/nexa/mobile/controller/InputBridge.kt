package com.nexa.mobile.controller

/** Something the virtual controller produced. */
sealed interface ControllerEvent {
    data class Button(val id: String, val pressed: Boolean) : ControllerEvent
    data class Stick(val x: Float, val y: Float) : ControllerEvent
}

/**
 * Delivers controller events to the game running on the phone.
 *
 * Android does not let a normal app inject input into another app. The candidate
 * permitted route (an AccessibilityService using dispatchGesture, enabled explicitly
 * by the user) has an unresolved conflict with the full-screen black overlay that
 * receives the touches, so it is NOT implemented yet. See README → "Controles".
 */
interface InputBridge {
    val isAvailable: Boolean
    val statusText: String
    fun send(event: ControllerEvent)
}

/** Honest default: events are shown on the pad but not delivered to the game. */
object UnavailableInputBridge : InputBridge {
    override val isAvailable = false
    override val statusText = "Entrada em validação: os comandos ainda não chegam ao jogo"
    override fun send(event: ControllerEvent) = Unit
}
