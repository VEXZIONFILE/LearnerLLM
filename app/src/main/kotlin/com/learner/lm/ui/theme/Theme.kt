package com.learner.lm.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

object NotebookColors {
    val GoogleBlue = Color(0xFF1A73E8)
    val GoogleBlueDark = Color(0xFF8AB4F8)
    val AccentTeal = Color(0xFF12B5CB)
    val AccentPurple = Color(0xFF7C4DFF)
    val NotebookBackground = Color(0xFFF5F7FB)
    val NotebookSurface = Color(0xFFFFFFFF)
    val NotebookSurfaceVariant = Color(0xFFE8F0FE)
    val NotebookOutline = Color(0xFFDADCE0)
    val NotebookTextPrimary = Color(0xFF1A1C1E)
    val NotebookTextSecondary = Color(0xFF5F6368)
    val NotebookDarkBackground = Color(0xFF0F1113)
    val NotebookDarkSurface = Color(0xFF1A1C1E)
    val NotebookDarkSurfaceVariant = Color(0xFF2A2D31)
    val SuccessGreen = Color(0xFF1E8E3E)
    val ProGold = Color(0xFFF9AB00)
    val ErrorRed = Color(0xFFD93025)

    val heroGradientLight = listOf(Color(0xFF1A73E8), Color(0xFF4285F4), Color(0xFF7BAAF7))
    val heroGradientDark = listOf(Color(0xFF174EA6), Color(0xFF1A73E8), Color(0xFF2D5FA8))
}

fun learnerHeroBrush(darkTheme: Boolean): Brush = Brush.linearGradient(
    if (darkTheme) NotebookColors.heroGradientDark else NotebookColors.heroGradientLight
)

private val LightColorScheme = lightColorScheme(
    primary = NotebookColors.GoogleBlue,
    onPrimary = Color.White,
    primaryContainer = NotebookColors.NotebookSurfaceVariant,
    onPrimaryContainer = NotebookColors.GoogleBlue,
    secondary = NotebookColors.AccentTeal,
    onSecondary = Color.White,
    tertiary = NotebookColors.AccentPurple,
    background = NotebookColors.NotebookBackground,
    onBackground = NotebookColors.NotebookTextPrimary,
    surface = NotebookColors.NotebookSurface,
    onSurface = NotebookColors.NotebookTextPrimary,
    surfaceVariant = Color(0xFFEEF1F6),
    onSurfaceVariant = NotebookColors.NotebookTextSecondary,
    outline = NotebookColors.NotebookOutline,
    outlineVariant = Color(0xFFE3E6EB),
    error = NotebookColors.ErrorRed
)

private val DarkColorScheme = darkColorScheme(
    primary = NotebookColors.GoogleBlueDark,
    onPrimary = Color(0xFF062E6F),
    primaryContainer = Color(0xFF174EA6),
    onPrimaryContainer = NotebookColors.GoogleBlueDark,
    secondary = NotebookColors.AccentTeal,
    tertiary = NotebookColors.AccentPurple,
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
    displaySmall = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 36.sp,
        lineHeight = 44.sp,
        letterSpacing = (-0.25).sp
    ),
    headlineLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 32.sp,
        lineHeight = 40.sp,
        letterSpacing = (-0.25).sp
    ),
    headlineMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 24.sp,
        lineHeight = 32.sp
    ),
    headlineSmall = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Medium,
        fontSize = 20.sp,
        lineHeight = 28.sp
    ),
    titleLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
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
    bodySmall = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.4.sp
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
    ),
    labelSmall = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
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
