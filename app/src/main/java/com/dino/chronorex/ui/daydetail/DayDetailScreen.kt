package com.dino.chronorex.ui.daydetail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import com.dino.chronorex.ui.components.ChronoRexAssistChip
import com.dino.chronorex.ui.components.ChronoRexCard
import com.dino.chronorex.ui.components.ChronoRexPrimaryButton
import com.dino.chronorex.ui.theme.spacing
import java.time.format.DateTimeFormatter
import java.util.UUID

@Composable
fun DayDetailScreen(
    state: DayDetailUiState,
    onDeleteSymptom: (UUID) -> Unit,
    onDeleteActivity: (UUID) -> Unit,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(MaterialTheme.spacing.lg),
        verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.lg)
    ) {
        if (state.isLoading && state.date == null) {
            ChronoRexCard {
                Text("Loading day...", style = MaterialTheme.typography.bodyMedium)
            }
        }

        state.date?.let { date ->
            ChronoRexCard {
                Text(
                    text = date.format(DateTimeFormatter.ISO_LOCAL_DATE),
                    style = MaterialTheme.typography.titleLarge
                )
                state.day?.let { day ->
                    Text(
                        text = "Restedness: ${day.restedness0To100}",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(top = MaterialTheme.spacing.sm)
                    )
                    Text(
                        text = "Sleep quality: ${day.sleepQuality1To5}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "Illness: ${if (day.illness) "Yes" else "No"} | Travel: ${if (day.travel) "Yes" else "No"}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(MaterialTheme.spacing.xs))
                    day.notes?.takeIf { it.isNotBlank() }?.let {
                        Text(
                            text = "Notes: $it",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    if (day.emojiTags.isNotEmpty()) {
                        Text(
                            text = "Tags: ${day.emojiTags.joinToString(", ")}",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                } ?: Text(
                    text = "No AM check-in recorded.",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = MaterialTheme.spacing.sm)
                )
            }
        }

        ChronoRexCard {
            Text("Symptoms", style = MaterialTheme.typography.titleMedium)
            if (state.symptoms.isEmpty()) {
                Text(
                    text = "No symptoms logged",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = MaterialTheme.spacing.sm)
                )
            } else {
                state.symptoms.forEach { symptom ->
                    Column(modifier = Modifier.padding(top = MaterialTheme.spacing.sm)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.padding(end = MaterialTheme.spacing.sm)) {
                                Text(symptom.name, fontWeight = FontWeight.SemiBold)
                                Text("Severity ${symptom.severity1To10}", style = MaterialTheme.typography.bodySmall)
                                symptom.note?.let { Text(it, style = MaterialTheme.typography.bodySmall) }
                            }
                            ChronoRexAssistChip(text = "Delete", onClick = { onDeleteSymptom(symptom.id) })
                        }
                        Divider(modifier = Modifier.padding(top = MaterialTheme.spacing.xs))
                    }
                }
            }
        }

        ChronoRexCard {
            Text("Activities", style = MaterialTheme.typography.titleMedium)
            if (state.activities.isEmpty()) {
                Text(
                    text = "No activities logged",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = MaterialTheme.spacing.sm)
                )
            } else {
                state.activities.forEach { activity ->
                    Column(modifier = Modifier.padding(top = MaterialTheme.spacing.sm)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.padding(end = MaterialTheme.spacing.sm)) {
                                Text(activity.type, fontWeight = FontWeight.SemiBold)
                                Text(
                                    text = "Exhaustion ${activity.perceivedExhaustion1To10}",
                                    style = MaterialTheme.typography.bodySmall
                                )
                                activity.durationMinutes?.let {
                                    Text("Duration ${it} min", style = MaterialTheme.typography.bodySmall)
                                }
                                activity.note?.let { Text(it, style = MaterialTheme.typography.bodySmall) }
                            }
                            ChronoRexAssistChip(text = "Delete", onClick = { onDeleteActivity(activity.id) })
                        }
                        Divider(modifier = Modifier.padding(top = MaterialTheme.spacing.xs))
                    }
                }
            }
        }

        ChronoRexPrimaryButton(text = "Back", onClick = onBack)
    }
}

