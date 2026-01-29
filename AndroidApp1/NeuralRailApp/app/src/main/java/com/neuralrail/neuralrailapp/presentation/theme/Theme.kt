package com.neuralrail.neuralrailapp.presentation.theme

import android.app.Activity
import android.os.Build
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.neuralrail.neuralrailapp.data.repository.SettingsRepository

// Local composition for theme-aware colors
val LocalAppColors = compositionLocalOf { AppColors() }

data class AppColors(
    val background: Color = BackgroundDark,
    val backgroundCard: Color = BackgroundCard,
    val textPrimary: Color = TextPrimary,
    val textSecondary: Color = TextSecondary,
    val textMuted: Color = TextMuted,
    val divider: Color = DividerColor,
    val isDark: Boolean = true
)

private val DarkAppColors = AppColors(
    background = BackgroundDark,
    backgroundCard = BackgroundCard,
    textPrimary = TextPrimary,
    textSecondary = TextSecondary,
    textMuted = TextMuted,
    divider = DividerColor,
    isDark = true
)

private val LightAppColors = AppColors(
    background = BackgroundLight,
    backgroundCard = BackgroundCardLight,
    textPrimary = TextPrimaryLight,
    textSecondary = TextSecondaryLight,
    textMuted = TextMutedLight,
    divider = DividerColorLight,
    isDark = false
)

// Red/Orange Dark Color Scheme
private val DarkColorScheme = darkColorScheme(
    primary = BluePrimary,              // Red
    secondary = BlueSecondary,          // Deep Orange
    tertiary = AccentOrange,            // Orange accent
    background = BackgroundDark,
    surface = BackgroundCard,
    surfaceVariant = CardDark,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = TextPrimary,
    onSurface = TextPrimary,
    outline = DividerColor,
    outlineVariant = DividerColor,
    error = AccentRed
)

// Red/Orange Light Color Scheme
private val LightColorScheme = lightColorScheme(
    primary = BluePrimary,              // Red
    secondary = BlueSecondary,          // Deep Orange
    tertiary = AccentOrange,            // Orange accent
    background = BackgroundLight,
    surface = BackgroundCardLight,
    surfaceVariant = SurfaceLight,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = TextPrimaryLight,
    onSurface = TextPrimaryLight,
    outline = DividerColorLight,
    outlineVariant = DividerColorLight,
    error = AccentRed
)

@Composable
fun NeuralRailAppTheme(
    darkTheme: Boolean? = null, // null means use settings
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    // Use settings repository for theme if not explicitly set
    val darkModeFromSettings by SettingsRepository.darkModeEnabled.collectAsState()
    val isDarkTheme = darkTheme ?: darkModeFromSettings

    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (isDarkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        isDarkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val appColors = if (isDarkTheme) DarkAppColors else LightAppColors

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            // Use orange header color for status bar to match app header
            window.statusBarColor = BluePrimary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }

    CompositionLocalProvider(LocalAppColors provides appColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}
