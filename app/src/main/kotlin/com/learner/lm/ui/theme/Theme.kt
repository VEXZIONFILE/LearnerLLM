package com.learner.lm.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** Professional SaaS / LLM product palette. */
object AppColors {
    val Accent = Color(0xFF4F46E5)
    val AccentHover = Color(0xFF4338CA)
    val AccentLight = Color(0xFFEEF2FF)
    val AccentMuted = Color(0xFFC7D2FE)

    val Background = Color(0xFFF8F9FC)
    val Surface = Color(0xFFFFFFFF)
    val SurfaceElevated = Color(0xFFFFFFFF)
    val SurfaceMuted = Color(0xFFF3F4F6)
    val Border = Color(0xFFE5E7EB)
    val BorderSubtle = Color(0xFFF0F1F3)

    val TextPrimary = Color(0xFF111827)
    val TextSecondary = Color(0xFF6B7280)
    val TextTertiary = Color(0xFF9CA3AF)

    val UserBubble = Color(0xFFF3F4F6)
    val AssistantBubble = Color(0xFFFFFFFF)

    val DarkBackground = Color(0xFF09090B)
    val DarkSurface = Color(0xFF18181B)
    val DarkSurfaceElevated = Color(0xFF27272A)
    val DarkBorder = Color(0xFF3F3F46)
    val DarkTextPrimary = Color(0xFFFAFAFA)
    val DarkTextSecondary = Color(0xFFA1A1AA)

    val AccentDark = Color(0xFF818CF8)
    val AccentDarkContainer = Color(0xFF312E81)
    val ProGold = Color(0xFFF59E0B)
    val Error = Color(0xFFDC2626)
    val Success = Color(0xFF059669)
}

/** @deprecated Use [AppColors] — kept for gradual migration. */
object NotebookColors {
    val GoogleBlue = AppColors.Accent
    val GoogleBlueDark = AppColors.AccentDark
    val AccentPurple = AppColors.Accent
    val NotebookBackground = AppColors.Background
    val NotebookSurface = AppColors.Surface
    val NotebookSurfaceVariant = AppColors.SurfaceMuted
    val NotebookOutline = AppColors.Border
    val NotebookTextPrimary = AppColors.TextPrimary
    val NotebookTextSecondary = AppColors.TextSecondary
    val NotebookChipSelected = AppColors.AccentLight
    val NotebookDarkBackground = AppColors.DarkBackground
    val NotebookDarkSurface = AppColors.DarkSurface
    val NotebookDarkSurfaceVariant = AppColors.DarkSurfaceElevated
    val ProGold = AppColors.ProGold
    val ErrorRed = AppColors.Error
}

object AppSpacing {
    val xs = 4.dp
    val sm = 8.dp
    val md = 16.dp
    val lg = 24.dp
    val xl = 32.dp
}

object AppRadii {
    val sm = 8.dp
    val md = 12.dp
    val lg = 16.dp
    val xl = 20.dp
    val pill = 999.dp
}

private val LightColorScheme = lightColorScheme(
    primary = AppColors.Accent,
    onPrimary = Color.White,
    primaryContainer = AppColors.AccentLight,
    onPrimaryContainer = AppColors.AccentHover,
    secondary = AppColors.Accent,
    onSecondary = Color.White,
    secondaryContainer = AppColors.SurfaceMuted,
    onSecondaryContainer = AppColors.TextPrimary,
    tertiary = AppColors.ProGold,
    background = AppColors.Background,
    onBackground = AppColors.TextPrimary,
    surface = AppColors.Surface,
    onSurface = AppColors.TextPrimary,
    surfaceVariant = AppColors.SurfaceMuted,
    onSurfaceVariant = AppColors.TextSecondary,
    outline = AppColors.Border,
    outlineVariant = AppColors.BorderSubtle,
    error = AppColors.Error
)

private val DarkColorScheme = darkColorScheme(
    primary = AppColors.AccentDark,
    onPrimary = AppColors.DarkBackground,
    primaryContainer = AppColors.AccentDarkContainer,
    onPrimaryContainer = AppColors.AccentLight,
    secondary = AppColors.AccentDark,
    background = AppColors.DarkBackground,
    onBackground = AppColors.DarkTextPrimary,
    surface = AppColors.DarkSurface,
    onSurface = AppColors.DarkTextPrimary,
    surfaceVariant = AppColors.DarkSurfaceElevated,
    onSurfaceVariant = AppColors.DarkTextSecondary,
    outline = AppColors.DarkBorder,
    outlineVariant = AppColors.DarkSurfaceElevated,
    error = Color(0xFFF87171)
)

private val AppTypography = Typography(
    headlineLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 30.sp,
        lineHeight = 36.sp,
        letterSpacing = (-0.5).sp
    ),
    headlineMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 24.sp,
        lineHeight = 30.sp,
        letterSpacing = (-0.25).sp
    ),
    headlineSmall = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 20.sp,
        lineHeight = 26.sp
    ),
    titleLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 18.sp,
        lineHeight = 24.sp
    ),
    titleMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
        lineHeight = 22.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 21.sp
    ),
    bodySmall = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 13.sp,
        lineHeight = 18.sp
    ),
    labelLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 18.sp
    ),
    labelMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.2.sp
    ),
    labelSmall = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 14.sp,
        letterSpacing = 0.3.sp
    )
)

private val AppShapes = Shapes(
    extraSmall = RoundedCornerShape(AppRadii.sm),
    small = RoundedCornerShape(AppRadii.md),
    medium = RoundedCornerShape(AppRadii.lg),
    large = RoundedCornerShape(AppRadii.xl),
    extraLarge = RoundedCornerShape(28.dp)
)

@Composable
fun LearnerLMTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme,
        typography = AppTypography,
        shapes = AppShapes,
        content = content
    )
}
