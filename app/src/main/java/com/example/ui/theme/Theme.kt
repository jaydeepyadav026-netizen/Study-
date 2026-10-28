package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val LifeOsDarkColorScheme = darkColorScheme(
    primary = ElectricViolet,
    onPrimary = Color.White,
    primaryContainer = ElectricVioletDark,
    onPrimaryContainer = Color(0xFFEDE9FE),
    secondary = ElectricCyan,
    onSecondary = Color(0xFF0F172A),
    secondaryContainer = Color(0xFF0E7490),
    onSecondaryContainer = Color(0xFFCFFAFE),
    tertiary = AccentAmber,
    onTertiary = Color.Black,
    tertiaryContainer = Color(0xFF78350F),
    onTertiaryContainer = Color(0xFFFEF3C7),
    background = MidnightBackground,
    onBackground = TextWhite,
    surface = DarkSlateSurface,
    onSurface = TextWhite,
    surfaceVariant = DarkSlateSurfaceVariant,
    onSurfaceVariant = TextMuted,
    surfaceContainer = DarkSlateSurfaceContainer,
    surfaceContainerHigh = DarkSlateSurfaceHigh,
    outline = DarkSlateBorder,
    outlineVariant = Color(0xFF1E293B),
    error = AccentRed,
    onError = Color.White,
    errorContainer = AccentRedDark,
    onErrorContainer = Color(0xFFFEE2E2)
)

val LifeOsLightColorScheme = lightColorScheme(
    primary = ElectricVioletDark,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFEDE9FE),
    onPrimaryContainer = Color(0xFF4C1D95),
    secondary = Color(0xFF0284C7),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFE0F2FE),
    onSecondaryContainer = Color(0xFF075985),
    tertiary = Color(0xFFD97706),
    onTertiary = Color.White,
    background = LightBackground,
    onBackground = LightTextPrimary,
    surface = LightSurface,
    onSurface = LightTextPrimary,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = LightTextSecondary,
    surfaceContainer = Color(0xFFFFFFFF),
    surfaceContainerHigh = Color(0xFFF8FAFC),
    outline = LightBorder,
    outlineVariant = Color(0xFFCBD5E1),
    error = AccentRed,
    onError = Color.White
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // Default to premium dark midnight theme
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) LifeOsDarkColorScheme else LifeOsLightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
