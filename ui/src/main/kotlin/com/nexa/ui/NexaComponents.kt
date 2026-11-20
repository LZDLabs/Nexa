package com.nexa.ui

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

enum class LogoSize(val mark: Dp, val textSp: Int, val tracking: Float) {
    Small(24.dp, 16, 0.18f), Medium(32.dp, 20, 0.20f), Large(44.dp, 30, 0.22f)
}

/** Port of the Gemini `NexaLogo` SVG brand mark. */
@Composable
fun NexaLogo(size: LogoSize = LogoSize.Medium, showTagline: Boolean = false, modifier: Modifier = Modifier) {
    Row(modifier, verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        NexaMark(size.mark)
        Column {
            Text(
                "NEXA",
                color = NexaColors.Text,
                fontWeight = FontWeight.Bold,
                fontSize = size.textSp.sp,
                letterSpacing = (size.textSp * size.tracking).sp,
            )
            if (showTagline) {
                Spacer(Modifier.height(4.dp))
                Text(
                    "CONSOLE BRIDGE",
                    color = NexaColors.TextMuted,
                    fontSize = 11.sp,
                    letterSpacing = 2.6.sp,
                )
            }
        }
    }
}

@Composable
fun NexaMark(size: Dp, modifier: Modifier = Modifier) {
    Canvas(modifier.size(size)) {
        val s = this.size.width / 48f
        drawRoundRect(NexaColors.Plum, Offset(2 * s, 2 * s), Size(44 * s, 44 * s), CornerRadius(11 * s))
        drawRoundRect(
            NexaColors.Violet, Offset(2 * s, 2 * s), Size(44 * s, 44 * s), CornerRadius(11 * s),
            style = Stroke(width = 2 * s),
        )
        val n = Path().apply {
            moveTo(14 * s, 34 * s); lineTo(14 * s, 14 * s); lineTo(27 * s, 28.5f * s); lineTo(27 * s, 14 * s)
        }
        drawPath(n, NexaColors.Text, style = Stroke(3.2f * s, cap = StrokeCap.Round, join = StrokeJoin.Round))
        drawLine(NexaColors.VioletGlow, Offset(34 * s, 14 * s), Offset(34 * s, 34 * s), 3.2f * s, StrokeCap.Round)
        drawCircle(NexaColors.Lavender, 2.2f * s, Offset(34 * s, 14 * s))
    }
}

/** Card surface used across both apps (`bg-[#161124] border-[#23193B] rounded-2xl`). */
fun Modifier.nexaCard(radius: Dp = 16.dp, borderColor: Color = NexaColors.Border, background: Color = NexaColors.Plum) =
    this.clip(RoundedCornerShape(radius))
        .background(background)
        .border(1.dp, borderColor, RoundedCornerShape(radius))

/** Small status pill with a dot (`rounded-full bg-[#161124] border`). */
@Composable
fun StatusPill(text: String, active: Boolean, modifier: Modifier = Modifier, pulsing: Boolean = false, fontSize: Int = 12) {
    Row(
        modifier
            .clip(CircleShape)
            .background(NexaColors.Plum)
            .border(1.dp, if (active || pulsing) NexaColors.Violet.copy(alpha = 0.4f) else NexaColors.Border, CircleShape)
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        StatusDot(if (active) NexaColors.VioletGlow else if (pulsing) NexaColors.Lavender else NexaColors.TextMuted, pulsing)
        Text(text, color = NexaColors.Text, fontSize = fontSize.sp)
    }
}

@Composable
fun StatusDot(color: Color, pulsing: Boolean = false, size: Dp = 8.dp) {
    val alpha = if (pulsing) {
        val t = rememberInfiniteTransition(label = "dot")
        val a by t.animateFloat(1f, 0.25f, infiniteRepeatable(tween(900, easing = LinearEasing), RepeatMode.Reverse), label = "a")
        a
    } else 1f
    Box(Modifier.size(size).alpha(alpha).clip(CircleShape).background(color))
}

/** Uppercase eyebrow label (`text-[11px] uppercase tracking-[0.2em] text-[#A78BFA]`). */
@Composable
fun Eyebrow(text: String, modifier: Modifier = Modifier, color: Color = NexaColors.Lavender, fontSize: Int = 11) {
    Text(
        text.uppercase(),
        modifier = modifier,
        color = color,
        fontSize = fontSize.sp,
        fontWeight = FontWeight.SemiBold,
        letterSpacing = (fontSize * 0.2f).sp,
    )
}

/** Labels any feature that is not yet functional, so placeholders are never mistaken for real features. */
@Composable
fun PlaceholderBadge(text: String, modifier: Modifier = Modifier) {
    Text(
        text,
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(NexaColors.Obsidian)
            .border(1.dp, NexaColors.Lavender.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 3.dp),
        color = NexaColors.Lavender,
        fontSize = 10.sp,
        fontWeight = FontWeight.SemiBold,
    )
}
