package com.punhanaghayev.bimmerpro.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val BimmerDarkColorScheme = darkColorScheme(
    primary = BmwBlueBright,
    onPrimary = Color(0xFF041C2C),
    primaryContainer = BmwBlue,
    onPrimaryContainer = Color.White,
    secondary = BmwMotorsportDarkBlue,
    onSecondary = Color.White,
    secondaryContainer = BimmerCardElevated,
    onSecondaryContainer = TextPrimary,
    tertiary = BmwMotorsportRed,
    onTertiary = Color.White,
    background = BimmerBg,
    onBackground = TextPrimary,
    surface = BimmerSurface,
    onSurface = TextPrimary,
    surfaceVariant = BimmerCard,
    onSurfaceVariant = TextSecondary,
    outline = BimmerOutline,
    error = StatusRed,
    onError = Color.White
)

@Composable
fun BimmerProTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = BimmerDarkColorScheme,
        typography = Typography,
        content = content
    )
}
