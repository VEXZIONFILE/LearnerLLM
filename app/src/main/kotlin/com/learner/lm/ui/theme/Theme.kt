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

// NotebookLM-inspired Google palette
object NotebookColors {
    val GoogleBlue = Color(0xFF1A73E8)
    val GoogleBlueDark = Color(0xFF8AB4F8)
    val NotebookBackground = Color(0xFFF0F4F9)
    val NotebookSurface = Color(0xFFFFFFFF)
    val NotebookSurfaceVariant = Color(0xFFE8F0FE)
    val NotebookOutline = Color(0xFFDADCE0)
    val NotebookTextPrimary = Color(0xFF202124)
    val NotebookTextSecondary = Color(0xFF5F6368)
    val NotebookDarkBackground = Color(0xFF131314)
    val NotebookDarkSurface = Color(0xFF1E1F20)
    val NotebookDarkSurfaceVariant = Color(0xFF2D2E30)
    val SuccessGreen = Color(0xFF1E8E3E)
    val ProGold = Color(0xFFF9AB00)
}

private val LightColorScheme = lightColorScheme(
    primary = NotebookColors.GoogleBlue,
    onPrimary = Color.White,
    primaryContainer = NotebookColors.NotebookSurfaceVariant,
    onPrimaryContainer = NotebookColors.GoogleBlue,
    secondary = Color(0xFF1967D2),
    background = NotebookColors.NotebookBackground,
    onBackground = NotebookColors.NotebookTextPrimary,
    surface = NotebookColors.NotebookSurface,
    onSurface = NotebookColors.NotebookTextPrimary,
    surfaceVariant = Color(0xFFF1F3F4),
    onSurfaceVariant = NotebookColors.NotebookTextSecondary,
    outline = NotebookColors.NotebookOutline,
    outlineVariant = Color(0xFFE8EAED)
)

private val DarkColorScheme = darkColorScheme(
    primary = NotebookColors.GoogleBlueDark,
    onPrimary = Color(0xFF062E6F),
    primaryContainer = Color(0xFF174EA6),
    onPrimaryContainer = NotebookColors.GoogleBlueDark,
    secondary = NotebookColors.GoogleBlueDark,
    background = NotebookColors.NotebookDarkBackground,
    onBackground = Color(0xFFE8EAED),
    surface = NotebookColors.NotebookDarkSurface,
    onSurface = Color(0xFFE8EAED),
    surfaceVariant = NotebookColors.NotebookDarkSurfaceVariant,
    onSurfaceVariant = Color(0xFF9AA0A6),
    outline = Color(0xFF3C4043),
    outlineVariant = Color(0xFF2D2E30)
)

private val NotebookTypography = Typography(
    headlineLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 32.sp,
        lineHeight = 40.sp,
        letterSpacing = (-0.25).sp
    ),
    headlineMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 24.sp,
        lineHeight = 32.sp
    ),
    titleLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Medium,
        fontSize = 22.sp,
        lineHeight = 28.sp
    ),
    titleMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.15.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.25.sp
    ),
    labelLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    ),
    labelMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
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
