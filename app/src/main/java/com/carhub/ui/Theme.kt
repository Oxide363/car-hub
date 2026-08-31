package com.carhub.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Fixed dark palette — an in-car console reads best dark, matching the product design.
object CH {
    val Bg = Color(0xFF0F0F12)
    val Rail = Color(0xFF17171B)
    val Card = Color(0xFF1E1E24)
    val CardAlt = Color(0xFF26262E)
    val Selected = Color(0xFF2E2E38)
    val TextPrimary = Color(0xFFF5F5F7)
    val TextSecondary = Color(0xFF9A9AA5)
    val Accent = Color(0xFF3B82F6)
    val Divider = Color(0xFF2A2A31)

    // Brand gradient endpoints for the fancy surfaces.
    val GradA = Color(0xFF2E74B5)
    val GradB = Color(0xFF7A4FE0)
    val Glow = Color(0xFF1B2A4A)
}

private val CarHubColors = darkColorScheme(
    primary = CH.Accent,
    onPrimary = Color.White,
    background = CH.Bg,
    onBackground = CH.TextPrimary,
    surface = CH.Card,
    onSurface = CH.TextPrimary,
    surfaceVariant = CH.CardAlt,
    onSurfaceVariant = CH.TextSecondary
)

@Composable
fun CarHubTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = CarHubColors, content = content)
}
