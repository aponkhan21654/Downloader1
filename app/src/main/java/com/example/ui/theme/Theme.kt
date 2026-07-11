package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = GlowTeal,
    onPrimary = Color.Black,
    primaryContainer = Color(0xFF003D39),
    onPrimaryContainer = GlowTeal,
    secondary = ElectricIndigo,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFF1E1E5C),
    onSecondaryContainer = Color(0xFFD6D6FF),
    tertiary = HotPink,
    onTertiary = Color.White,
    background = DeepBackground,
    onBackground = LightText,
    surface = DarkSurface,
    onSurface = LightText,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = GrayText,
    error = RedError,
    onError = Color.White,
    outline = MutedGray
)

private val LightColorScheme = lightColorScheme(
    primary = GlowTeal,
    onPrimary = Color.Black,
    secondary = ElectricIndigo,
    onSecondary = Color.White,
    background = Color(0xFFF9FAFB),
    onBackground = Color(0xFF111827),
    surface = Color.White,
    onSurface = Color(0xFF111827),
    surfaceVariant = Color(0xFFE5E7EB),
    onSurfaceVariant = Color(0xFF4B5563),
    error = RedError,
    onError = Color.White,
    outline = Color(0xFF9CA3AF)
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // Force Dark mode as the default for the video downloader theme!
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
