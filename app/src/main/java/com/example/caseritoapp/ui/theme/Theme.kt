package com.example.caseritoapp.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val LightColorScheme = lightColorScheme(
    primary = CaseritoPrimary,
    onPrimary = CaseritoOnPrimary,
    primaryContainer = CaseritoPrimaryContainer,
    onPrimaryContainer = CaseritoOnPrimaryContainer,

    secondary = CaseritoSecondary,
    onSecondary = CaseritoOnSecondary,
    secondaryContainer = CaseritoSecondaryContainer,
    onSecondaryContainer = CaseritoOnSecondaryContainer,

    tertiary = CaseritoTertiary,
    onTertiary = CaseritoOnTertiary,
    tertiaryContainer = CaseritoTertiaryContainer,
    onTertiaryContainer = CaseritoOnTertiaryContainer,

    background = CaseritoBackground,
    onBackground = CaseritoOnBackground,

    surface = CaseritoSurface,
    onSurface = CaseritoOnSurface,
    surfaceVariant = CaseritoSurfaceVariant,
    onSurfaceVariant = CaseritoOnSurfaceVariant,

    outline = CaseritoOutline,
    error = CaseritoError
)

private val DarkColorScheme = darkColorScheme(
    primary = CaseritoDarkPrimary,
    onPrimary = CaseritoDarkOnPrimary,
    primaryContainer = CaseritoDarkPrimaryContainer,
    onPrimaryContainer = CaseritoDarkOnPrimaryContainer,

    secondary = CaseritoDarkSecondary,
    onSecondary = CaseritoDarkOnSecondary,
    secondaryContainer = CaseritoDarkSecondaryContainer,
    onSecondaryContainer = CaseritoDarkOnSecondaryContainer,

    tertiary = CaseritoDarkTertiary,
    onTertiary = CaseritoDarkOnTertiary,
    tertiaryContainer = CaseritoDarkTertiaryContainer,
    onTertiaryContainer = CaseritoDarkOnTertiaryContainer,

    background = CaseritoDarkBackground,
    onBackground = CaseritoDarkOnBackground,

    surface = CaseritoDarkSurface,
    onSurface = CaseritoDarkOnSurface,
    surfaceVariant = CaseritoDarkSurfaceVariant,
    onSurfaceVariant = CaseritoDarkOnSurfaceVariant,

    outline = CaseritoDarkOutline,
    error = CaseritoDarkError
)

@Composable
fun CaseritoAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Desactivamos dynamicColor por defecto para preservar la identidad naranja de Caserito
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = CaseritoTypography,
        shapes = CaseritoShapes,
        content = content
    )
}
