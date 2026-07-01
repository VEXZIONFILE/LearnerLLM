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

/** ChatGPT-inspired palette */
object AppColors {
    val Accent = Color(0xFF10A37F)
    val AccentHover = Color(0xFF0D8C6D)
    val AccentLight = Color(0xFFE8F5F1)
    val AccentMuted = Color(0xFFB8E8DA)

    val Background = Color(0xFFFFFFFF)
    val Surface = Color(0xFFFFFFFF)
    val SurfaceElevated = Color(0xFFF7F7F8)
    val SurfaceMuted = Color(0xFFF4F4F4)
    val Border = Color(0xFFE5E5E5)
    val BorderSubtle = Color(0xFFEFEFEF)

    val TextPrimary = Color(0xFF0D0D0D)
    val TextSecondary = Color(0xFF6E6E80)
    val TextTertiary = Color(0xFF8E8EA0)

    val UserBubble = Color(0xFFF4F4F4)
    val AssistantBubble = Color(0xFFFFFFFF)
    val MessageStripe = Color(0xFFF7F7F8)

    val DarkBackground = Color(0xFF212121)
    val DarkSurface = Color(0xFF212121)
    val DarkSurfaceElevated = Color(0xFF2F2F2F)
    val DarkBorder = Color(0xFF424242)
    val DarkTextPrimary = Color(0xFFECECF1)
    val DarkTextSecondary = Color(0xFFC5C5D2)
    val DarkUserBubble = Color(0xFF2F2F2F)
    val DarkMessageStripe = Color(0xFF2A2A2A)

    val AccentDark = Color(0xFF19C37D)
    val AccentDarkContainer = Color(0xFF1A4D3E)
    val ProGold = Color(0xFFF59E0B)
    val Error = Color(0xFFEF4146)
    val Success = Color(0xFF10A37F)

    val ComposerBackground = Color(0xFFFFFFFF)
    val ComposerBorder = Color(0xFFD9D9E3)
    val DarkComposerBackground = Color(0xFF2F2F2F)
    val DarkComposerBorder = Color(0xFF565869)
}

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
    val NotebookChipSelected = AppColors.SurfaceMuted
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
    val chatMaxWidth = 768.dp
}

object AppRadii {
    val sm = 8.dp
    val md = 12.dp
    val lg = 18.dp
    val xl = 24.dp
    val pill = 999.dp
    val composer = 26.dp
}

private val LightColorScheme = lightColorScheme(
    primary = AppColors.Accent,
    onPrimary = Color.White,
    primaryContainer = AppColors.AccentLight,
    onPrimaryContainer = AppColors.AccentHover,
    secondary = AppColors.TextSecondary,
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
    secondary = AppColors.DarkTextSecondary,
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
        fontSize = 28.sp,
        lineHeight = 34.sp,
        letterSpacing = (-0.5).sp
    ),
    headlineMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 22.sp,
        lineHeight = 28.sp
    ),
    headlineSmall = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 18.sp,
        lineHeight = 24.sp
    ),
    titleLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 17.sp,
        lineHeight = 22.sp
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
        lineHeight = 26.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 15.sp,
        lineHeight = 24.sp
    ),
    bodySmall = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 13.sp,
        lineHeight = 20.sp
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
        lineHeight = 16.sp
    ),
    labelSmall = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 11.sp,
        lineHeight = 14.sp
    )
)

private val AppShapes = Shapes(
    extraSmall = RoundedCornerShape(AppRadii.sm),
    small = RoundedCornerShape(AppRadii.md),
    medium = RoundedCornerShape(AppRadii.lg),
    large = RoundedCornerShape(AppRadii.xl),
    extraLarge = RoundedCornerShape(AppRadii.composer)
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
