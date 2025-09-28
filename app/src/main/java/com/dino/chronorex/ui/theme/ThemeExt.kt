package com.dino.chronorex.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable

val MaterialTheme.spacing: ChronoRexSpacing
    @Composable get() = LocalSpacing.current
