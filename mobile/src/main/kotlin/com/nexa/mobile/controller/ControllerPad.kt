package com.nexa.mobile.controller

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.outlined.OpenInFull
import androidx.compose.material.icons.outlined.OpenWith
import androidx.compose.material.icons.outlined.PowerSettingsNew
import androidx.compose.material.icons.outlined.RestartAlt
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nexa.ui.NexaColors
import kotlin.math.roundToInt

private val JoystickBase = 120.dp
private val ButtonBase = 60.dp

/**
 * Pure-black (#000000) virtual controller from the Gemini prototype.
 * It never renders game imagery: the video exists only on the TV.
 */
@Composable
fun ControllerPad(
    layout: ControllerLayout,
    onLayoutChange: (ControllerLayout) -> Unit,
    customizing: Boolean,
    onToggleCustomize: () -> Unit,
    statusText: String,
    footerText: String,
    endLabel: String,
    onEnd: () -> Unit,
    onEvent: (ControllerEvent) -> Unit,
    modifier: Modifier = Modifier,
    paused: Boolean = false,
    onTogglePause: (() -> Unit)? = null,
    notice: String? = null,
) {
    var selected by remember { mutableStateOf(ControlId.A) }
    var pressed by remember { mutableStateOf(emptySet<String>()) }
    var stick by remember { mutableStateOf(Offset.Zero) }
    val currentLayout by rememberUpdatedState(layout)
    val layoutChanged by rememberUpdatedState(onLayoutChange)
    val emit by rememberUpdatedState(onEvent)

    fun setPressed(id: String, down: Boolean) {
        pressed = if (down) pressed + id else pressed - id
        emit(ControllerEvent.Button(id, down))
    }

    Box(modifier.fillMaxSize().background(NexaColors.PureBlack)) {
        // ---------- Movable controls ----------
        BoxWithConstraints(Modifier.fillMaxSize()) {
            val wPx = constraints.maxWidth.toFloat()
            val hPx = constraints.maxHeight.toFloat()
            val density = LocalDensity.current

            ControlId.entries.forEach { id ->
                val p = layout[id]
                val sizeDp = (if (id == ControlId.JOYSTICK) JoystickBase else ButtonBase) * p.size
                val sizePx = with(density) { sizeDp.toPx() }
                val isSelected = customizing && selected == id

                val interaction = if (customizing) {
                    Modifier
                        .pointerInput(id, wPx, hPx) { detectTapGestures { selected = id } }
                        .pointerInput(id, wPx, hPx) {
                            detectDragGestures(onDragStart = { selected = id }) { change, drag ->
                                change.consume()
                                val cur = currentLayout[id]
                                layoutChanged(
                                    currentLayout.with(
                                        id,
                                        cur.copy(
                                            x = (cur.x + drag.x / wPx * 100f).coerceIn(10f, 92f),
                                            y = (cur.y + drag.y / hPx * 100f).coerceIn(22f, 86f),
                                        ),
                                    )
                                )
                            }
                        }
                } else if (id == ControlId.JOYSTICK) {
                    Modifier.pointerInput(id) {
                        awaitEachGesture {
                            val down = awaitFirstDown()
                            val center = Offset(size.width / 2f, size.height / 2f)
                            val maxR = size.width * 0.3f
                            fun update(pos: Offset) {
                                val d = pos - center
                                val dist = d.getDistance()
                                val v = if (dist > maxR) d * (maxR / dist) else d
                                stick = Offset(v.x / maxR, v.y / maxR)
                                emit(ControllerEvent.Stick(stick.x, stick.y))
                            }
                            update(down.position)
                            while (true) {
                                val event = awaitPointerEvent()
                                val change = event.changes.firstOrNull { it.id == down.id } ?: break
                                if (!change.pressed) break
                                update(change.position)
                                change.consume()
                            }
                            stick = Offset.Zero
                            emit(ControllerEvent.Stick(0f, 0f))
                        }
                    }
                } else {
                    Modifier.pointerInput(id) {
                        awaitEachGesture {
                            awaitFirstDown(requireUnconsumed = false)
                            setPressed(id.label, true)
                            waitForUpOrCancellation()
                            setPressed(id.label, false)
                        }
                    }
                }

                Box(
                    Modifier
                        .offset {
                            IntOffset(
                                (p.x / 100f * wPx - sizePx / 2f).roundToInt(),
                                (p.y / 100f * hPx - sizePx / 2f).roundToInt(),
                            )
                        }
                        .size(sizeDp)
                        .then(interaction),
                    contentAlignment = Alignment.Center,
                ) {
                    if (id == ControlId.JOYSTICK) {
                        Joystick(stick, isSelected, sizeDp)
                    } else {
                        FaceButton(id.label, pressed = id.label in pressed, selected = isSelected)
                    }
                }
            }
        }

        // ---------- Fixed chrome ----------
        Column(Modifier.fillMaxSize()) {
            // Top utility bar
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(Modifier.size(8.dp).clip(CircleShape).background(NexaColors.VioletGlow))
                Spacer(Modifier.width(8.dp))
                Text(statusText, color = NexaColors.TextMuted, fontSize = 12.sp, maxLines = 1)
                Spacer(Modifier.width(8.dp))
                Text(
                    "MODO OLED PRETO",
                    color = NexaColors.Violet,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(NexaColors.Plum)
                        .padding(horizontal = 8.dp, vertical = 2.dp),
                )
                Spacer(Modifier.weight(1f))
                PadChip(
                    if (customizing) "Concluir" else "Personalizar", Icons.Outlined.Tune,
                    active = customizing, onClick = onToggleCustomize,
                )
                if (onTogglePause != null) {
                    Spacer(Modifier.width(6.dp))
                    PadChip(
                        if (paused) "Retomar" else "Pausar",
                        if (paused) Icons.Filled.PlayArrow else Icons.Filled.Pause,
                        active = paused, onClick = onTogglePause,
                    )
                }
                Spacer(Modifier.width(6.dp))
                PadChip(endLabel, Icons.Outlined.PowerSettingsNew, active = false, onClick = onEnd, iconTint = NexaColors.Lavender)
            }
            Box(Modifier.fillMaxWidth().height(1.dp).background(NexaColors.Plum))

            if (notice != null) {
                Text(
                    notice,
                    color = NexaColors.Lavender,
                    fontSize = 11.sp,
                    modifier = Modifier.align(Alignment.CenterHorizontally).padding(top = 6.dp),
                )
            }

            // Shoulder triggers (L1 / R1)
            Row(Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 8.dp)) {
                Shoulder("L1", "L1" in pressed) { down -> if (!customizing) setPressed("L1", down) }
                Spacer(Modifier.weight(1f))
                Shoulder("R1", "R1" in pressed) { down -> if (!customizing) setPressed("R1", down) }
            }

            Spacer(Modifier.weight(1f))

            if (customizing) {
                CustomizeToolbar(
                    size = layout[selected].size,
                    onSize = { s -> onLayoutChange(layout.with(selected, layout[selected].copy(size = s))) },
                    onReset = { onLayoutChange(ControllerLayout.DEFAULT) },
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                )
                Spacer(Modifier.weight(1f))
            }

            Text(
                if (customizing) "EDITANDO: ${selected.label} (${selected.description})".uppercase() else footerText.uppercase(),
                color = NexaColors.TextMuted.copy(alpha = 0.6f),
                fontSize = 10.sp,
                letterSpacing = 2.sp,
                modifier = Modifier.align(Alignment.CenterHorizontally).padding(bottom = 8.dp),
            )
        }
    }
}

@Composable
private fun Joystick(stick: Offset, selected: Boolean, size: Dp) {
    val travel = with(LocalDensity.current) { (size * 0.25f).toPx() }
    Box(
        Modifier
            .fillMaxSize()
            .then(if (selected) Modifier.border(6.dp, NexaColors.Violet.copy(alpha = 0.3f), CircleShape) else Modifier)
            .clip(CircleShape)
            .background(NexaColors.Obsidian)
            .border(2.dp, if (selected) NexaColors.Lavender else NexaColors.Border, CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            Modifier
                .offset { IntOffset((stick.x * travel).roundToInt(), (stick.y * travel).roundToInt()) }
                .size(size * 0.46f)
                .shadow(12.dp, CircleShape, ambientColor = NexaColors.Violet, spotColor = NexaColors.Violet)
                .clip(CircleShape)
                .background(NexaColors.Plum)
                .border(2.dp, NexaColors.Violet, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Box(Modifier.size(size * 0.125f).clip(CircleShape).background(NexaColors.VioletGlow))
        }
    }
}

@Composable
private fun FaceButton(label: String, pressed: Boolean, selected: Boolean) {
    val (bg, border) = when {
        selected -> NexaColors.Border to NexaColors.Lavender
        pressed -> NexaColors.Violet to NexaColors.Text
        else -> NexaColors.Obsidian to NexaColors.Border
    }
    Box(
        Modifier
            .fillMaxSize()
            .then(if (pressed) Modifier.shadow(16.dp, CircleShape, ambientColor = NexaColors.VioletGlow, spotColor = NexaColors.VioletGlow) else Modifier)
            .then(if (selected) Modifier.border(6.dp, NexaColors.Violet.copy(alpha = 0.35f), CircleShape) else Modifier)
            .clip(CircleShape)
            .background(bg)
            .border(2.dp, border, CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Text(label, color = NexaColors.Text, fontWeight = FontWeight.Bold, fontSize = 18.sp)
    }
}

@Composable
private fun Shoulder(label: String, pressed: Boolean, onPress: (Boolean) -> Unit) {
    val press by rememberUpdatedState(onPress)
    Text(
        label,
        color = if (pressed) NexaColors.Text else NexaColors.TextMuted,
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.Bold,
        fontSize = 13.sp,
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (pressed) NexaColors.Violet else NexaColors.Obsidian)
            .border(1.dp, if (pressed) NexaColors.Lavender else NexaColors.Border, RoundedCornerShape(12.dp))
            .pointerInput(Unit) {
                awaitEachGesture {
                    awaitFirstDown(requireUnconsumed = false)
                    press(true)
                    waitForUpOrCancellation()
                    press(false)
                }
            }
            .padding(horizontal = 32.dp, vertical = 8.dp),
    )
}

@Composable
private fun PadChip(text: String, icon: ImageVector, active: Boolean, onClick: () -> Unit, iconTint: Color? = null) {
    Row(
        Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (active) NexaColors.Violet else NexaColors.Obsidian)
            .border(1.dp, if (active) NexaColors.VioletGlow else NexaColors.Border, RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        val fg = if (active) NexaColors.Text else NexaColors.TextMuted
        Icon(icon, null, tint = iconTint ?: fg, modifier = Modifier.size(14.dp))
        Text(text, color = fg, fontSize = 12.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun CustomizeToolbar(size: Float, onSize: (Float) -> Unit, onReset: () -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier
            .clip(RoundedCornerShape(12.dp))
            .background(NexaColors.Plum.copy(alpha = 0.95f))
            .border(1.dp, NexaColors.Violet, RoundedCornerShape(12.dp))
            .padding(horizontal = 14.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Icon(Icons.Outlined.OpenWith, null, tint = NexaColors.Lavender, modifier = Modifier.size(14.dp))
        Text("Arraste os botões", color = NexaColors.Lavender, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
        Icon(Icons.Outlined.OpenInFull, null, tint = NexaColors.TextMuted, modifier = Modifier.size(14.dp))
        Slider(
            value = size,
            onValueChange = onSize,
            valueRange = ControllerLayout.MIN_SIZE..ControllerLayout.MAX_SIZE,
            modifier = Modifier.width(140.dp),
            colors = SliderDefaults.colors(
                thumbColor = NexaColors.Violet,
                activeTrackColor = NexaColors.Violet,
                inactiveTrackColor = NexaColors.Border,
            ),
        )
        Row(
            Modifier.clickable(onClick = onReset).padding(4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Icon(Icons.Outlined.RestartAlt, null, tint = NexaColors.TextMuted, modifier = Modifier.size(14.dp))
            Text("Reset", color = NexaColors.TextMuted, fontSize = 11.sp)
        }
    }
}
