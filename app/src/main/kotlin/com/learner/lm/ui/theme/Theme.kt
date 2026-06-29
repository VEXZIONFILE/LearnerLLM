package com.learner.lm.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val PurplePrimary = Color(0xFF6C63FF)
private val BlueSecondary = Color(0xFF4FACFE)
private val DarkBackground = Color(0xFF0F1020)
private val DarkSurface = Color(0xFF1A1B2E)
private val LightBackground = Color(0xFFF8F9FF)

private val DarkColorScheme = darkColorScheme(
    primary = PurplePrimary,
    secondary = BlueSecondary,
    background = DarkBackground,
    surface = DarkSurface,
    onPrimary = Color.White,
    onBackground = Color(0xFFE8E8F0),
    onSurface = Color(0xFFE8E8F0)
)

private val LightColorScheme = lightColorScheme(
    primary = PurplePrimary,
    secondary = BlueSecondary,
    background = LightBackground,
    surface = Color.White,
    onPrimary = Color.White,
    onBackground = Color(0xFF1A1B2E),
    onSurface = Color(0xFF1A1B2E)
)

@Composable
fun LearnerLMTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme,
        content = content
    )
}
