package com.punhanaghayev.bimmerpro.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color

val LocalThemeStyle = compositionLocalOf { AppThemeStyle.M_CARBON }

@Composable
fun BimmerProTheme(
    themeStyle: AppThemeStyle = AppThemeStyle.M_CARBON,
    content: @Composable () -> Unit
) {
    val scheme = if (themeStyle.isDark) {
        darkColorScheme(
            primary = themeStyle.primaryBright,
            onPrimary = Color(0xFF041C2C),
            primaryContainer = themeStyle.primary,
            onPrimaryContainer = Color.White,
            secondary = themeStyle.cardElevated,
            onSecondary = themeStyle.textPrimary,
            secondaryContainer = themeStyle.cardElevated,
            onSecondaryContainer = themeStyle.textPrimary,
            tertiary = themeStyle.accentRed,
            onTertiary = Color.White,
            background = themeStyle.bg,
            onBackground = themeStyle.textPrimary,
            surface = themeStyle.surface,
            onSurface = themeStyle.textPrimary,
            surfaceVariant = themeStyle.card,
            onSurfaceVariant = themeStyle.textSecondary,
            outline = themeStyle.outline,
            error = StatusRed,
            onError = Color.White
        )
    } else {
        lightColorScheme(
            primary = themeStyle.primary,
            onPrimary = Color.White,
            primaryContainer = themeStyle.primary,
            onPrimaryContainer = Color.White,
            secondary = themeStyle.cardElevated,
            onSecondary = themeStyle.textPrimary,
            background = themeStyle.bg,
            onBackground = themeStyle.textPrimary,
            surface = themeStyle.surface,
            onSurface = themeStyle.textPrimary,
            surfaceVariant = themeStyle.card,
            onSurfaceVariant = themeStyle.textSecondary,
            outline = themeStyle.outline,
            error = StatusRed,
            onError = Color.White
        )
    }

    CompositionLocalProvider(LocalThemeStyle provides themeStyle) {
        MaterialTheme(
            colorScheme = scheme,
            typography = Typography,
            content = content
        )
    }
}
