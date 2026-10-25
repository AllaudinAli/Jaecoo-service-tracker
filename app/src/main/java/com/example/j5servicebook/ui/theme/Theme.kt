package com.example.j5servicebook.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.j5servicebook.R

// One accent (yellow) on a white canvas with soft grey cards, plus dark "inverse"
// panels for the most important block on each screen.
val Accent = Color(0xFFFFCC00)
val OnAccent = Color(0xFF111111)

private val LightColors = lightColorScheme(
    primary = Accent,
    onPrimary = OnAccent,
    primaryContainer = Color(0xFFFFF4CC),
    onPrimaryContainer = OnAccent,
    secondary = Color(0xFF111111),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFF2F2F2),
    onSecondaryContainer = Color(0xFF111111),
    tertiary = Color(0xFF111111),
    background = Color.White,
    onBackground = Color(0xFF111111),
    surface = Color.White,
    onSurface = Color(0xFF111111),
    surfaceVariant = Color(0xFFF4F4F4),
    onSurfaceVariant = Color(0xFF8A8A8A),
    surfaceContainerLowest = Color.White,
    surfaceContainerLow = Color.White,
    surfaceContainer = Color(0xFFF5F5F5),
    surfaceContainerHigh = Color(0xFFEFEFEF),
    surfaceContainerHighest = Color(0xFFE9E9E9),
    outline = Color(0xFFD9D9D9),
    outlineVariant = Color(0xFFEAEAEA),
    inverseSurface = Color(0xFF1E1E1E),
    inverseOnSurface = Color.White,
    inversePrimary = Accent,
    error = Color(0xFFE5484D),
    onError = Color.White,
    scrim = Color.Black,
)

private val DarkColors = darkColorScheme(
    primary = Accent,
    onPrimary = OnAccent,
    primaryContainer = Color(0xFF3A3000),
    onPrimaryContainer = Accent,
    secondary = Color(0xFFF2F2F2),
    onSecondary = Color(0xFF111111),
    secondaryContainer = Color(0xFF222222),
    onSecondaryContainer = Color(0xFFF2F2F2),
    tertiary = Color(0xFFF2F2F2),
    background = Color(0xFF0A0A0A),
    onBackground = Color(0xFFF2F2F2),
    surface = Color(0xFF0A0A0A),
    onSurface = Color(0xFFF2F2F2),
    surfaceVariant = Color(0xFF171717),
    onSurfaceVariant = Color(0xFF8C8C8C),
    surfaceContainerLowest = Color(0xFF0A0A0A),
    surfaceContainerLow = Color(0xFF141414),
    surfaceContainer = Color(0xFF161616),
    surfaceContainerHigh = Color(0xFF1E1E1E),
    surfaceContainerHighest = Color(0xFF262626),
    outline = Color(0xFF3A3A3A),
    outlineVariant = Color(0xFF242424),
    inverseSurface = Color(0xFF1C1C1C),
    inverseOnSurface = Color.White,
    inversePrimary = Accent,
    error = Color(0xFFFF6B6B),
    onError = Color(0xFF111111),
    scrim = Color.Black,
)

/** Status colours. Brand yellow is kept for actions; "due soon" uses orange so the two never blur. */
@Immutable
data class StatusColors(val ok: Color, val soon: Color, val overdue: Color)

private val LightStatus = StatusColors(ok = Color(0xFF16A34A), soon = Color(0xFFEA7A00), overdue = Color(0xFFE5484D))
private val DarkStatus = StatusColors(ok = Color(0xFF4ADE80), soon = Color(0xFFFFA23E), overdue = Color(0xFFFF6B6B))

val LocalStatusColors = staticCompositionLocalOf { LightStatus }

@OptIn(ExperimentalTextApi::class)
private fun geist(weight: Int) = Font(
    R.font.geist,
    weight = FontWeight(weight),
    variationSettings = FontVariation.Settings(FontVariation.weight(weight)),
)

/** Geist, bundled so the app stays offline. Swap the font file to change the whole app. */
val Geist = FontFamily(geist(300), geist(400), geist(500), geist(600))

private const val Tabular = "tnum"

private fun style(size: Int, line: Int, weight: Int, tracking: Double = 0.0, tabular: Boolean = false) = TextStyle(
    fontFamily = Geist,
    fontSize = size.sp,
    lineHeight = line.sp,
    fontWeight = FontWeight(weight),
    letterSpacing = tracking.sp,
    fontFeatureSettings = if (tabular) Tabular else null,
)

private val AppTypography = Typography(
    displayLarge = style(44, 50, 500, -1.2, tabular = true),
    displayMedium = style(38, 44, 500, -1.0, tabular = true),
    displaySmall = style(32, 38, 500, -0.8, tabular = true),
    headlineLarge = style(32, 38, 500, -0.8),
    headlineMedium = style(26, 32, 500, -0.5),
    headlineSmall = style(22, 28, 500, -0.3, tabular = true),
    titleLarge = style(20, 26, 500, -0.2),
    titleMedium = style(16, 22, 500, -0.1, tabular = true),
    titleSmall = style(14, 20, 500),
    bodyLarge = style(16, 24, 400, tabular = true),
    bodyMedium = style(14, 20, 400, tabular = true),
    bodySmall = style(13, 18, 400, tabular = true),
    labelLarge = style(15, 20, 500),
    labelMedium = style(13, 16, 500, tabular = true),
    labelSmall = style(11, 14, 500, 0.2),
)

private val AppShapes = Shapes(
    extraSmall = RoundedCornerShape(10.dp),
    small = RoundedCornerShape(14.dp),
    medium = RoundedCornerShape(18.dp),
    large = RoundedCornerShape(26.dp),
    extraLarge = RoundedCornerShape(32.dp),
)

@Composable
fun J5Theme(wallpaperColors: Boolean = false, content: @Composable () -> Unit) {
    val dark = isSystemInDarkTheme()
    val context = LocalContext.current
    val scheme: ColorScheme = when {
        wallpaperColors && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S ->
            if (dark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        dark -> DarkColors
        else -> LightColors
    }
    CompositionLocalProvider(LocalStatusColors provides if (dark) DarkStatus else LightStatus) {
        MaterialTheme(colorScheme = scheme, typography = AppTypography, shapes = AppShapes, content = content)
    }
}
