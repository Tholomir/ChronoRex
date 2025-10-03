package com.dino.chronorex.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val ChronoRexLightColors = lightColorScheme(
    primary = ChronoRexFern,
    onPrimary = Color.White,
    primaryContainer = ChronoRexDinoMint,
    onPrimaryContainer = ChronoRexInk,
    secondary = ChronoRexDinoMint,
    onSecondary = ChronoRexInk,
    secondaryContainer = ChronoRexSkyEgg,
    onSecondaryContainer = ChronoRexInk,
    tertiary = ChronoRexLava,
    onTertiary = Color.White,
    background = ChronoRexBoneWhite,
    onBackground = ChronoRexInk,
    surface = ChronoRexBoneWhite,
    onSurface = ChronoRexInk,
    surfaceVariant = ChronoRexSkyEgg,
    onSurfaceVariant = ChronoRexInkMuted,
    outline = ChronoRexDivider,
    error = ChronoRexLava,
    onError = Color.White
)

private val ChronoRexDarkColors = darkColorScheme(
    primary = ChronoRexFern,
    onPrimary = ChronoRexInkLight,
    primaryContainer = ChronoRexFernBright,
    onPrimaryContainer = ChronoRexNightSurface,
    secondary = ChronoRexFernBright,
    onSecondary = ChronoRexNightSurface,
    secondaryContainer = ChronoRexSkyNight,
    onSecondaryContainer = ChronoRexInkLight,
    tertiary = ChronoRexLava,
    onTertiary = Color.White,
    background = ChronoRexNight,
    onBackground = ChronoRexInkLight,
    surface = ChronoRexNightSurface,
    onSurface = ChronoRexInkLight,
    surfaceVariant = ChronoRexSkyNight,
    onSurfaceVariant = ChronoRexInkMutedLight,
    outline = ChronoRexFernBright,
    error = ChronoRexLava,
    onError = Color.White
)

@Composable
fun ChronoRexTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    useDynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val colorScheme = remember(darkTheme, useDynamicColor, context) {
        when {
            useDynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
                val dynamicScheme = if (darkTheme) {
                    dynamicDarkColorScheme(context)
                } else {
                    dynamicLightColorScheme(context)
                }
                dynamicScheme.withChronoRexTokens(darkTheme)
            }
            darkTheme -> ChronoRexDarkColors
            else -> ChronoRexLightColors
        }
    }

    CompositionLocalProvider(LocalSpacing provides ChronoRexSpacing()) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = ChronoRexTypography,
            shapes = ChronoRexShapes,
            content = content
        )
    }
}

private fun ColorScheme.withChronoRexTokens(darkTheme: Boolean): ColorScheme = copy(
    primary = ChronoRexFern,
    onPrimary = Color.White,
    primaryContainer = ChronoRexDinoMint,
    onPrimaryContainer = ChronoRexInk,
    secondary = if (darkTheme) ChronoRexFernBright else ChronoRexDinoMint,
    onSecondary = if (darkTheme) ChronoRexNightSurface else ChronoRexInk,
    secondaryContainer = if (darkTheme) ChronoRexSkyNight else ChronoRexSkyEgg,
    onSecondaryContainer = if (darkTheme) ChronoRexInkLight else ChronoRexInk,
    tertiary = ChronoRexLava,
    onTertiary = Color.White,
    background = if (darkTheme) ChronoRexNight else ChronoRexBoneWhite,
    onBackground = if (darkTheme) ChronoRexInkLight else ChronoRexInk,
    surface = if (darkTheme) ChronoRexNightSurface else ChronoRexBoneWhite,
    onSurface = if (darkTheme) ChronoRexInkLight else ChronoRexInk,
    surfaceVariant = if (darkTheme) ChronoRexSkyNight else ChronoRexSkyEgg,
    onSurfaceVariant = if (darkTheme) ChronoRexInkMutedLight else ChronoRexInkMuted,
    outline = if (darkTheme) ChronoRexFernBright.copy(alpha = 0.4f) else ChronoRexDivider,
    error = ChronoRexLava,
    onError = Color.White
)
