package com.dino.chronorex.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color

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

@Composable
fun ChronoRexTheme(content: @Composable () -> Unit) {
    CompositionLocalProvider(LocalSpacing provides ChronoRexSpacing()) {
        MaterialTheme(
            colorScheme = ChronoRexLightColors,
            typography = ChronoRexTypography,
            shapes = ChronoRexShapes,
            content = content
        )
    }
}
