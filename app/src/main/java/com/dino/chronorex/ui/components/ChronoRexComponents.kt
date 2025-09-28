package com.dino.chronorex.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.dino.chronorex.ui.ChronoRexRoute
import com.dino.chronorex.ui.theme.spacing

@Composable
fun ChronoRexPrimaryButton(
    text: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    Button(
        modifier = modifier,
        onClick = onClick,
        enabled = enabled,
        shape = MaterialTheme.shapes.medium,
        contentPadding = PaddingValues(horizontal = MaterialTheme.spacing.lg, vertical = 14.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onPrimary
        )
    }
}

@Composable
fun ChronoRexCard(
    modifier: Modifier = Modifier,
    tonal: Boolean = false,
    contentPadding: PaddingValues = PaddingValues(MaterialTheme.spacing.lg),
    content: @Composable ColumnScope.() -> Unit
) {
    val colors = CardDefaults.cardColors(
        containerColor = if (tonal) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface
    )
    Card(
        modifier = modifier,
        shape = MaterialTheme.shapes.medium,
        colors = colors,
        elevation = CardDefaults.cardElevation(defaultElevation = if (tonal) 0.dp else 2.dp)
    ) {
        Column(modifier = Modifier.padding(contentPadding), content = content)
    }
}

@Immutable
data class ChronoRexChipColors(
    val container: Color,
    val label: Color,
)

@Composable
fun ChronoRexAssistChip(
    text: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    colors: ChronoRexChipColors = ChronoRexChipColors(
        container = MaterialTheme.colorScheme.secondaryContainer,
        label = MaterialTheme.colorScheme.onSecondaryContainer
    ),
    onClick: () -> Unit
) {
    AssistChip(
        modifier = modifier,
        onClick = onClick,
        enabled = enabled,
        label = {
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge,
                color = colors.label
            )
        },
        colors = AssistChipDefaults.assistChipColors(
            containerColor = colors.container,
            labelColor = colors.label
        ),
        shape = MaterialTheme.shapes.small
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChronoRexTopBar(currentRoute: String?) {
    val title = when (currentRoute) {
        ChronoRexRoute.CheckIn.route -> "Daily Check-In"
        ChronoRexRoute.Settings.route -> "Settings"
        ChronoRexRoute.Insights.route -> "Insights"
        ChronoRexRoute.DayDetail.route -> "Day Detail"
        ChronoRexRoute.Onboarding.route -> "Onboarding"
        else -> "ChronoRex"
    }
    CenterAlignedTopAppBar(
        title = { Text(title, style = MaterialTheme.typography.titleLarge) },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = MaterialTheme.colorScheme.background,
            titleContentColor = MaterialTheme.colorScheme.onBackground
        )
    )
}

@Composable
fun ToggleRow(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge
        )
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BottomQuickActionsBar(
    onLogSymptom: () -> Unit,
    onLogActivity: () -> Unit
) {
    BottomAppBar(
        modifier = Modifier.navigationBarsPadding(),
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 3.dp,
        contentPadding = PaddingValues(horizontal = MaterialTheme.spacing.lg, vertical = MaterialTheme.spacing.sm)
    ) {
        ChronoRexPrimaryButton(
            text = "Symptoms",
            modifier = Modifier.fillMaxWidth(0.45f),
            onClick = onLogSymptom
        )
        Spacer(modifier = Modifier.width(MaterialTheme.spacing.sm))
        ChronoRexPrimaryButton(
            text = "Activities",
            modifier = Modifier.fillMaxWidth(0.45f),
            onClick = onLogActivity
        )
    }
}


