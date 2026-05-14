package me.asteroidus.swissgrades.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = SwissBlueDark,
    onPrimary = AppBackgroundDark,
    primaryContainer = AppSurfaceVariantDark,
    onPrimaryContainer = AppTextPrimaryDark,
    secondary = SwissBlueDark,
    onSecondary = AppBackgroundDark,
    secondaryContainer = AppSurfaceVariantDark,
    onSecondaryContainer = AppTextPrimaryDark,
    tertiary = AppPositiveDark,
    onTertiary = AppBackgroundDark,
    tertiaryContainer = AppPositiveContainerDark,
    onTertiaryContainer = AppPositiveDark,
    background = AppBackgroundDark,
    onBackground = AppTextPrimaryDark,
    surface = AppSurfaceDark,
    onSurface = AppTextPrimaryDark,
    surfaceVariant = AppSurfaceVariantDark,
    onSurfaceVariant = AppTextSecondaryDark,
    outline = AppOutlineDark,
    outlineVariant = AppOutlineDark,
    error = AppWarningDark,
    onError = AppBackgroundDark,
    errorContainer = AppWarningContainerDark,
    onErrorContainer = AppWarningDark
)

private val LightColorScheme = lightColorScheme(
    primary = SwissBlue,
    onPrimary = AppSurfaceLight,
    primaryContainer = AppSurfaceVariantLight,
    onPrimaryContainer = AppTextPrimaryLight,
    secondary = SwissBlue,
    onSecondary = AppSurfaceLight,
    secondaryContainer = AppSurfaceVariantLight,
    onSecondaryContainer = AppTextPrimaryLight,
    tertiary = AppPositiveLight,
    onTertiary = AppSurfaceLight,
    tertiaryContainer = AppPositiveContainerLight,
    onTertiaryContainer = AppPositiveLight,
    background = AppBackgroundLight,
    onBackground = AppTextPrimaryLight,
    surface = AppSurfaceLight,
    onSurface = AppTextPrimaryLight,
    surfaceVariant = AppSurfaceVariantLight,
    onSurfaceVariant = AppTextSecondaryLight,
    outline = AppOutlineLight,
    outlineVariant = AppOutlineLight,
    error = AppWarningLight,
    onError = AppSurfaceLight,
    errorContainer = AppWarningContainerLight,
    onErrorContainer = AppWarningLight
)

@Composable
fun SwissGradesTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
