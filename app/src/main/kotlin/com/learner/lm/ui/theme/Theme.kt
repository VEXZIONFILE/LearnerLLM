package com.learner.lm.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/** NotebookLM-inspired palette — soft canvas, crisp surfaces, Google blue accent. */
object NotebookColors {
    val GoogleBlue = Color(0xFF1A73E8)
    val GoogleBlueDark = Color(0xFF8AB4F8)
    val AccentPurple = Color(0xFF7C4DFF)
    val NotebookBackground = Color(0xFFF0F4F9)
    val NotebookSurface = Color(0xFFFFFFFF)
    val NotebookSurfaceVariant = Color(0xFFE8F0FE)
    val NotebookOutline = Color(0xFFDADCE0)
    val NotebookTextPrimary = Color(0xFF202124)
    val NotebookTextSecondary = Color(0xFF5F6368)
    val NotebookChipSelected = Color(0xFFC2E7FF)
    val NotebookDarkBackground = Color(0xFF131314)
    val NotebookDarkSurface = Color(0xFF1E1F20)
    val NotebookDarkSurfaceVariant = Color(0xFF2D2E30)
    val ProGold = Color(0xFFF9AB00)
    val ErrorRed = Color(0xFFD93025)
}

private val LightColorScheme = lightColorScheme(
    primary = NotebookColors.GoogleBlue,
    onPrimary = Color.White,
    primaryContainer = NotebookColors.NotebookChipSelected,
    onPrimaryContainer = Color(0xFF001D35),
    secondary = NotebookColors.AccentPurple,
    background = NotebookColors.NotebookBackground,
    onBackground = NotebookColors.NotebookTextPrimary,
    surface = NotebookColors.NotebookSurface,
    onSurface = NotebookColors.NotebookTextPrimary,
    surfaceVariant = Color(0xFFF1F3F4),
    onSurfaceVariant = NotebookColors.NotebookTextSecondary,
    outline = NotebookColors.NotebookOutline,
    outlineVariant = Color(0xFFE8EAED),
    error = NotebookColors.ErrorRed
)

private val DarkColorScheme = darkColorScheme(
    primary = NotebookColors.GoogleBlueDark,
    onPrimary = Color(0xFF062E6F),
    primaryContainer = Color(0xFF174EA6),
    onPrimaryContainer = NotebookColors.GoogleBlueDark,
    secondary = NotebookColors.AccentPurple,
    background = NotebookColors.NotebookDarkBackground,
    onBackground = Color(0xFFE8EAED),
    surface = NotebookColors.NotebookDarkSurface,
    onSurface = Color(0xFFE8EAED),
    surfaceVariant = NotebookColors.NotebookDarkSurfaceVariant,
    onSurfaceVariant = Color(0xFF9AA0A6),
    outline = Color(0xFF3C4043),
    outlineVariant = Color(0xFF2D2E30),
    error = Color(0xFFF28B82)
)

private val NotebookTypography = Typography(
    headlineLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 28.sp,
        lineHeight = 36.sp,
        letterSpacing = (-0.25).sp
    ),
    headlineMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 22.sp,
        lineHeight = 28.sp
    ),
    headlineSmall = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Medium,
        fontSize = 18.sp,
        lineHeight = 24.sp
    ),
    titleLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Medium,
        fontSize = 20.sp,
        lineHeight = 26.sp
    ),
    titleMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Medium,
        fontSize = 15.sp,
        lineHeight = 22.sp,
        letterSpacing = 0.1.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 15.sp,
        lineHeight = 22.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp
    ),
    bodySmall = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp
    ),
    labelLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Medium,
        fontSize = 13.sp,
        lineHeight = 18.sp
    ),
    labelMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.4.sp
    ),
    labelSmall = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Medium,
        fontSize = 10.sp,
        lineHeight = 14.sp,
        letterSpacing = 0.4.sp
    )
)

@Composable
fun LearnerLMTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme,
        typography = NotebookTypography,
        content = content
    )
}
