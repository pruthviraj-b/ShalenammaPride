package com.pruthviraj.shalenammapride.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.graphics.Color
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = OrangePrimary,
    onPrimary = PureWhite,
    primaryContainer = OrangeEnd,
    onPrimaryContainer = PureWhite,
    secondary = TealAccent,
    onSecondary = PureWhite,
    background = Neutral900,
    onBackground = Neutral100,
    surface = Color(0xFF1E293B),
    onSurface = Neutral100,
    surfaceVariant = Color(0xFF334155),
    onSurfaceVariant = Neutral300,
    outline = Color(0xFF475569)
)

private val LightColorScheme = lightColorScheme(
    primary = OrangePrimary,
    onPrimary = PureWhite,
    primaryContainer = OrangeLight,
    onPrimaryContainer = OrangePrimary,
    secondary = TealAccent,
    onSecondary = PureWhite,
    background = Neutral50,
    onBackground = Neutral900,
    surface = PureWhite,
    onSurface = Neutral900,
    surfaceVariant = Neutral100,
    onSurfaceVariant = Neutral500,
    outline = Neutral300
)

@Composable
fun ShalenammaPrideTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
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
        typography = Typography,
        content = content
    )
}