package com.audil.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColors = lightColorScheme(
    primary = WarmTerracotta,
    onPrimary = Color.White,
    primaryContainer = WarmTerracottaMuted,
    onPrimaryContainer = WarmTerracotta,
    secondary = SageGreen,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFEDF5EC),
    onSecondaryContainer = SageGreen,
    tertiary = StatusAmber,
    onTertiary = Color.White,
    background = PaperCream,
    onBackground = PaperInk,
    surface = PaperWhite,
    onSurface = PaperInk,
    surfaceVariant = PaperWarmGray,
    onSurfaceVariant = PaperInkSecondary,
    outline = PaperBorder,
    outlineVariant = Color(0x08_2D2824),
    error = StatusRed,
    onError = Color.White,
    errorContainer = Color(0xFFFDECEC),
    onErrorContainer = StatusRed,
    inverseSurface = WarmDarkBg,
    inverseOnSurface = WarmDarkInk,
    inversePrimary = WarmTerracottaLight
)

private val DarkColors = darkColorScheme(
    primary = WarmTerracottaLight,
    onPrimary = Color(0xFF2D1810),
    primaryContainer = Color(0xFF4A2820),
    onPrimaryContainer = WarmTerracottaMuted,
    secondary = SageGreenLight,
    onSecondary = Color(0xFF0A1F0A),
    secondaryContainer = Color(0xFF1E3A1E),
    onSecondaryContainer = Color(0xFFD4ECD4),
    tertiary = StatusAmber,
    onTertiary = Color(0xFF1A1000),
    background = WarmDarkBg,
    onBackground = WarmDarkInk,
    surface = WarmDarkSurface,
    onSurface = WarmDarkInk,
    surfaceVariant = WarmDarkSurfaceAlt,
    onSurfaceVariant = WarmDarkInkSecondary,
    outline = WarmDarkBorder,
    outlineVariant = Color(0x08_EBE4DC),
    error = StatusRed,
    onError = Color.White,
    errorContainer = Color(0xFF3D1A1A),
    onErrorContainer = Color(0xFFFFD4D4),
    inverseSurface = PaperCream,
    inverseOnSurface = PaperInk,
    inversePrimary = WarmTerracotta
)

@Composable
fun AudilTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColors else LightColors

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = JournalTypography,
        content = content
    )
}
