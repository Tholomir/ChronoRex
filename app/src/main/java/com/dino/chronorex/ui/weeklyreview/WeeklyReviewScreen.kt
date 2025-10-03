package com.dino.chronorex.ui.weeklyreview

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.dino.chronorex.model.WeeklyReview
import com.dino.chronorex.ui.components.ChronoRexCard
import com.dino.chronorex.ui.components.ChronoRexPrimaryButton
import com.dino.chronorex.ui.theme.spacing

@Composable
fun WeeklyReviewScreen(review: WeeklyReview?, onBack: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(MaterialTheme.spacing.lg),
        verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.lg)
    ) {
        if (review == null) {
            ChronoRexCard {
                Text(
                    text = "No weekly review yet",
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = "Log at least seven days of check-ins, symptoms, and activities to unlock the weekly summary.",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = MaterialTheme.spacing.sm)
                )
            }
        } else {
            WeeklyReviewBody(review)
        }
        ChronoRexPrimaryButton(text = "Back", modifier = Modifier.fillMaxWidth(), onClick = onBack)
    }
}

@Composable
private fun WeeklyReviewBody(review: WeeklyReview) {
    ChronoRexCard {
        Text(
            text = "Week of ${review.startDate} – ${review.endDate}",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(MaterialTheme.spacing.sm))
        SectionTitle("Trend highlights")
        review.trendHighlights.forEach { highlight ->
            Text(
                text = "• $highlight",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = MaterialTheme.spacing.xs)
            )
        }
        Spacer(modifier = Modifier.height(MaterialTheme.spacing.sm))
        if (review.correlationHighlights.isNotEmpty()) {
            SectionTitle("Correlation signals")
            review.correlationHighlights.forEach { highlight ->
                Text(
                    text = "• $highlight",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = MaterialTheme.spacing.xs)
                )
            }
            Spacer(modifier = Modifier.height(MaterialTheme.spacing.sm))
        }
        SectionTitle("Best day")
        Text(
            text = review.bestDay?.toString() ?: "Not enough data",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(top = MaterialTheme.spacing.xs)
        )
        Spacer(modifier = Modifier.height(MaterialTheme.spacing.sm))
        SectionTitle("Toughest day")
        Text(
            text = review.toughestDay?.toString() ?: "Not enough data",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(top = MaterialTheme.spacing.xs)
        )
        Spacer(modifier = Modifier.height(MaterialTheme.spacing.sm))
        SectionTitle("Adherence")
        Text(
            text = review.adherenceSummary,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(top = MaterialTheme.spacing.xs)
        )
        Spacer(modifier = Modifier.height(MaterialTheme.spacing.sm))
        Text(
            text = "All insights stay on-device until you export.",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold
    )
}
