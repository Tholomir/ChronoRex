package com.dino.chronorex.ui.insights

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.dino.chronorex.analytics.CorrelationInsights
import com.dino.chronorex.analytics.TrendInsights
import com.dino.chronorex.ui.components.ChronoRexCard
import com.dino.chronorex.ui.components.ChronoRexPrimaryButton
import com.dino.chronorex.ui.theme.spacing

@Composable
fun InsightsScreen(state: InsightsUiState, onBack: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(MaterialTheme.spacing.lg),
        verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.lg)
    ) {
        when (state) {
            InsightsUiState.Loading -> LoadingCard()
            InsightsUiState.Empty -> EmptyInsightsCard()
            is InsightsUiState.Ready -> ReadyInsightsContent(state)
        }
        ChronoRexPrimaryButton(text = "Back", modifier = Modifier.fillMaxWidth(), onClick = onBack)
    }
}

@Composable
private fun LoadingCard() {
    ChronoRexCard {\n        CircularProgressIndicator()\n        Spacer(modifier = Modifier.height(MaterialTheme.spacing.sm))\n        Text(
            text = "Crunching the latest patterns...",
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

@Composable
private fun EmptyInsightsCard() {
    ChronoRexCard {
        Text(
            text = "Log a few days to unlock insights",
            style = MaterialTheme.typography.titleMedium
        )
        Text(
            text = "You need at least a week of check-ins, symptoms, and activities to compute trends and correlations.",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(top = MaterialTheme.spacing.sm)
        )
    }
}

@Composable
private fun ReadyInsightsContent(state: InsightsUiState.Ready) {
    state.trend?.let { trend ->
        TrendCard(trend)
    } ?: ChronoRexCard {
        Text(
            text = "Collect more check-ins",
            style = MaterialTheme.typography.bodyMedium
        )
        Text(
            text = "We chart fatigue once seven days of entries are available.",
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(top = MaterialTheme.spacing.sm)
        )
    }

    if (state.correlations.isNotEmpty()) {
        state.correlations.forEach { correlation ->
            CorrelationCard(correlation)
        }
    } else {
        ChronoRexCard {
            Text(
                text = "Still gathering correlation data",
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = "Keep logging symptoms and activities so we can surface the strongest relationships.",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = MaterialTheme.spacing.sm)
            )
        }
    }
}

@Composable
private fun TrendCard(trend: TrendInsights) {
    ChronoRexCard(tonal = true) {
        Text("Fatigue 7-day moving average", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(MaterialTheme.spacing.sm))
        TrendSparkline(trend)
        val delta = trend.averageDelta
        val deltaText = delta?.let {
            val sign = if (it > 0) "+" else ""
            "Last point ${String.format("%.1f", trend.latestAverage)} (${sign}${String.format("%.1f", it)} vs prior)"
        }
        deltaText?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = MaterialTheme.spacing.sm)
            )
        }
        Text(
            text = "Tap export to share with your clinician.",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(top = MaterialTheme.spacing.xs)
        )
    }
}

@Composable
private fun TrendSparkline(trend: TrendInsights) {
    val points = trend.points
    if (points.isEmpty()) return
    val maxValue = points.maxOf { it.movingAverage }
    val minValue = points.minOf { it.movingAverage }
    val range = (maxValue - minValue).takeIf { it > 0.01 } ?: 1.0
    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp)
    ) {
        val stroke = Stroke(width = 6f)
        val path = Path()
        points.forEachIndexed { index, point ->
            val x = if (points.size == 1) size.width / 2f else size.width * index / (points.size - 1)
            val normalized = (point.movingAverage - minValue) / range
            val y = size.height - (normalized * size.height).toFloat()
            if (index == 0) {
                path.moveTo(x, y)
            } else {
                path.lineTo(x, y)
            }
        }
        drawPath(
            path = path,
            color = MaterialTheme.colorScheme.primary,
            style = stroke
        )
        points.forEachIndexed { index, point ->
            val x = if (points.size == 1) size.width / 2f else size.width * index / (points.size - 1)
            val normalized = (point.movingAverage - minValue) / range
            val y = size.height - (normalized * size.height).toFloat()
            drawCircle(
                color = MaterialTheme.colorScheme.primary,
                radius = 8f,
                center = Offset(x, y)
            )
        }
    }
}

@Composable
private fun CorrelationCard(correlation: CorrelationInsights) {
    ChronoRexCard {
        Text(correlation.headline, style = MaterialTheme.typography.titleMedium)
        Text(
            text = correlation.narrative,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(top = MaterialTheme.spacing.sm)
        )
        Text(
            text = "n=${correlation.sampleSize}",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = MaterialTheme.spacing.xs)
        )
    }
}

