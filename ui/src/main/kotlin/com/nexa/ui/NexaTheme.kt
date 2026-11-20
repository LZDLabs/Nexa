package com.nexa.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/** NEXA palette, taken 1:1 from the Gemini design system ("sem ciano"). */
object NexaColors {
    val PureBlack = Color(0xFF000000)      // OLED controller background
    val Shell = Color(0xFF050408)
    val Obsidian = Color(0xFF09080D)       // base surface (TV + mobile)
    val Plum = Color(0xFF161124)           // cards, inputs
    val Border = Color(0xFF23193B)
    val Violet = Color(0xFF7C3AED)         // primary actions, D-pad focus ring
    val VioletGlow = Color(0xFF8B5CF6)     // active indicators
    val Lavender = Color(0xFFA78BFA)
    val LavenderLight = Color(0xFFC4B5FD)
    val Text = Color(0xFFF8FAFC)
    val TextMuted = Color(0xFF94A3B8)
}

@Composable
fun NexaTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = darkColorScheme(
            primary = NexaColors.Violet,
            onPrimary = NexaColors.Text,
            secondary = NexaColors.VioletGlow,
            onSecondary = NexaColors.Text,
            background = NexaColors.Obsidian,
            onBackground = NexaColors.Text,
            surface = NexaColors.Plum,
            onSurface = NexaColors.Text,
            surfaceVariant = NexaColors.Border,
            onSurfaceVariant = NexaColors.TextMuted,
            outline = NexaColors.Border,
        ),
        content = content,
    )
}
