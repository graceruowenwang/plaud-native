package com.openplaud.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val PlaudColorScheme = darkColorScheme(
    primary = Color(0xFF6366F1),        // Indigo
    onPrimary = Color.White,
    primaryContainer = Color(0xFF4F46E5),
    secondary = Color(0xFF22D3EE),       // Cyan
    onSecondary = Color.Black,
    tertiary = Color(0xFFA78BFA),        // Violet
    background = Color(0xFF0F172A),      // Slate-900
    onBackground = Color(0xFFF1F5F9),    // Slate-100
    surface = Color(0xFF1E293B),         // Slate-800
    onSurface = Color(0xFFE2E8F0),       // Slate-200
    surfaceVariant = Color(0xFF334155),  // Slate-700
    onSurfaceVariant = Color(0xFF94A3B8), // Slate-400
    outline = Color(0xFF475569),          // Slate-600
    error = Color(0xFFEF4444),            // Red-500
    onError = Color.White,
)

object PlaudTheme {
    val colors @Composable get() = PlaudColors
}

object PlaudColors {
    val background = Color(0xFF0F172A)
    val surface = Color(0xFF1E293B)
    val surfaceVariant = Color(0xFF334155)
    val surfaceLight = Color(0xFF334155)
    val primary = Color(0xFF6366F1)
    val accent = Color(0xFF22D3EE)
    val textPrimary = Color(0xFFF1F5F9)
    val textSecondary = Color(0xFF94A3B8)
    val success = Color(0xFF22C55E)
    val warning = Color(0xFFF59E0B)
    val error = Color(0xFFEF4444)
}

@Composable
fun PlaudTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = PlaudColorScheme,
        content = content
    )
}
