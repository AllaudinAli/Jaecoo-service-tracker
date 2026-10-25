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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Monochrome palette: a soft grey canvas with white panels in light mode,
// true black with charcoal panels in dark mode (AMOLED friendly).
private val Ink = Color(0xFF141416)
private val Paper = Color(0xFFF1F1F3)

private val LightColors = lightColorScheme(
    primary = Ink,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE6E6EA),
    onPrimaryContainer = Ink,
    secondary = Color(0xFF5E5E66),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFE4E4E8),
    onSecondaryContainer = Ink,
    tertiary = Color(0xFF5E5E66),
    background = Color(0xFFF3F3F5),
    onBackground = Ink,
    surface = Color(0xFFF3F3F5),
    onSurface = Ink,
    surfaceVariant = Color(0xFFE9E9EC),
    onSurfaceVariant = Color(0xFF6B6B73),
    surfaceContainerLowest = Color.White,
    surfaceContainerLow = Color.White,
    surfaceContainer = Color.White,
    surfaceContainerHigh = Color(0xFFF8F8FA),
    surfaceContainerHighest = Color(0xFFECECEF),
    surfaceBright = Color.White,
    surfaceDim = Color(0xFFE4E4E8),
    outline = Color(0xFFC6C6CC),
    outlineVariant = Color(0xFFE6E6EA),
    inverseSurface = Ink,
    inverseOnSurface = Paper,
    inversePrimary = Paper,
    error = Color(0xFFC4302B),
    onError = Color.White,
    scrim = Color.Black,
)

private val DarkColors = darkColorScheme(
    primary = Paper,
    onPrimary = Ink,
    primaryContainer = Color(0xFF2A2A2E),
    onPrimaryContainer = Paper,
    secondary = Color(0xFFA0A0A8),
    onSecondary = Ink,
    secondaryContainer = Color(0xFF2A2A2E),
    onSecondaryContainer = Paper,
    tertiary = Color(0xFFA0A0A8),
    background = Color.Black,
    onBackground = Paper,
    surface = Color.Black,
    onSurface = Paper,
    surfaceVariant = Color(0xFF1E1E21),
    onSurfaceVariant = Color(0xFF9C9CA4),
    surfaceContainerLowest = Color.Black,
    surfaceContainerLow = Color(0xFF161618),
    surfaceContainer = Color(0xFF161618),
    surfaceContainerHigh = Color(0xFF1E1E21),
    surfaceContainerHighest = Color(0xFF28282C),
    surfaceBright = Color(0xFF2C2C30),
    surfaceDim = Color.Black,
    outline = Color(0xFF45454B),
    outlineVariant = Color(0xFF26262A),
    inverseSurface = Paper,
    inverseOnSurface = Ink,
    inversePrimary = Ink,
    error = Color(0xFFFF6B61),
    onError = Ink,
    scrim = Color.Black,
)

/** Muted status colours, the only colour in the default theme. */
@Immutable
data class StatusColors(val ok: Color, val soon: Color, val overdue: Color)

private val LightStatus = StatusColors(ok = Color(0xFF1F8A47), soon = Color(0xFFB06A00), overdue = Color(0xFFC4302B))
private val DarkStatus = StatusColors(ok = Color(0xFF5CCB80), soon = Color(0xFFF0B44C), overdue = Color(0xFFFF6B61))

val LocalStatusColors = staticCompositionLocalOf { LightStatus }

private val Tabular = "tnum"

private val AppTypography = Typography().run {
    copy(
        displaySmall = displaySmall.copy(fontSize = 34.sp, lineHeight = 40.sp, fontWeight = FontWeight.SemiBold, letterSpacing = (-0.5).sp, fontFeatureSettings = Tabular),
        headlineLarge = headlineLarge.copy(fontSize = 30.sp, lineHeight = 36.sp, fontWeight = FontWeight.SemiBold, letterSpacing = (-0.4).sp),
        headlineSmall = headlineSmall.copy(fontWeight = FontWeight.SemiBold, letterSpacing = (-0.2).sp, fontFeatureSettings = Tabular),
        titleLarge = titleLarge.copy(fontSize = 20.sp, fontWeight = FontWeight.SemiBold),
        titleMedium = titleMedium.copy(fontWeight = FontWeight.Medium, fontFeatureSettings = Tabular),
        bodyLarge = bodyLarge.copy(fontFeatureSettings = Tabular),
        bodyMedium = bodyMedium.copy(fontFeatureSettings = Tabular),
        bodySmall = bodySmall.copy(fontSize = 13.sp, lineHeight = 18.sp, fontFeatureSettings = Tabular),
        labelLarge = labelLarge.copy(fontWeight = FontWeight.Medium),
        labelMedium = labelMedium.copy(fontWeight = FontWeight.Medium, fontFeatureSettings = Tabular),
    )
}

/** Small caps-style section label. */
val SectionLabelStyle = TextStyle(fontSize = 13.sp, lineHeight = 18.sp, fontWeight = FontWeight.Medium, letterSpacing = 0.2.sp)

private val AppShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(28.dp),
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
