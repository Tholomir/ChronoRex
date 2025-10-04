package com.dino.chronorex.ui.checkin

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.dino.chronorex.ui.components.ChronoRexAssistChip
import com.dino.chronorex.ui.components.ChronoRexCard
import com.dino.chronorex.ui.components.ChronoRexChipColors
import com.dino.chronorex.ui.components.ChronoRexPrimaryButton
import com.dino.chronorex.ui.components.ChronoRexSecondaryButton
import com.dino.chronorex.ui.theme.spacing
import java.time.LocalTime

@Composable
fun DailyCheckInScreen(
    state: DailyCheckInUiState,
    onChangeRestedness: (Int) -> Unit,
    onChangeSleepQuality: (Int) -> Unit,
    onToggleIllness: (Boolean) -> Unit,
    onToggleTravel: (Boolean) -> Unit,
    onUpdateNotes: (String) -> Unit,
    onUpdateEmojiTags: (List<String>) -> Unit,
    onSetBeforeFourPreference: (Boolean) -> Unit,
    onSave: () -> Unit,
    onUndo: () -> Unit,
    onUndoTimeout: () -> Unit,
    onBack: () -> Unit
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val showBeforeFourDialog = remember { mutableStateOf(false) }

    LaunchedEffect(state.beforeFourAmIsYesterday) {
        if (!state.beforeFourAmIsYesterday && LocalTime.now().isBefore(LocalTime.of(4, 0))) {
            showBeforeFourDialog.value = true
        }
    }

    LaunchedEffect(state.undoRequestId) {
        if (state.undoRequestId == null) return@LaunchedEffect
        val timeoutJob = launch {
            delay(5_000)
            snackbarHostState.currentSnackbarData?.dismiss()
        }
        val result = try {
            snackbarHostState.showSnackbar(
                message = "Logged. T-Rexcellent.",
                actionLabel = "Undo",
                withDismissAction = true,
                duration = SnackbarDuration.Indefinite
            )
        } finally {
            timeoutJob.cancel()
        }
        when (result) {
            SnackbarResult.ActionPerformed -> onUndo()
            SnackbarResult.Dismissed -> onUndoTimeout()
        }
    }

    if (showBeforeFourDialog.value) {
        AlertDialog(
            onDismissRequest = { showBeforeFourDialog.value = false },
            title = { Text("Assign entries before 4 AM") },
            text = {
                Text("Would you like entries logged before 4:00 AM to count toward yesterday?")
            },
            confirmButton = {
                TextButton(onClick = {
                    onSetBeforeFourPreference(true)
                    showBeforeFourDialog.value = false
                }) { Text("Yes, use yesterday") }
            },
            dismissButton = {
                TextButton(onClick = {
                    onSetBeforeFourPreference(false)
                    showBeforeFourDialog.value = false
                }) { Text("No, keep today") }
            }
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        val scrollState = rememberScrollState()
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .imePadding()
                .verticalScroll(scrollState)
                .padding(MaterialTheme.spacing.lg),
            verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.lg)
        ) {
            ChronoRexCard {
                Text(
                    text = "Log for ${state.targetDate}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                RestednessSection(value = state.restedness, onChange = onChangeRestedness)
                Spacer(modifier = Modifier.height(MaterialTheme.spacing.sm))
                SleepQualitySection(value = state.sleepQuality, onChange = onChangeSleepQuality)
                Spacer(modifier = Modifier.height(MaterialTheme.spacing.md))
                FlagRow(label = "Illness", checked = state.illness, onCheckedChange = onToggleIllness)
                FlagRow(label = "Travel", checked = state.travel, onCheckedChange = onToggleTravel)
                NotesAndTagsSection(
                    notes = state.notes,
                    onUpdateNotes = onUpdateNotes,
                    tags = state.emojiTags,
                    onUpdateTags = onUpdateEmojiTags,
                    suggestions = state.recentEmojiSuggestions
                )
            }
            ChronoRexPrimaryButton(
                text = "Save entry",
                modifier = Modifier.fillMaxWidth(),
                onClick = onSave,
                enabled = state.canSave && state.hasChanges && !state.isSaving
            )
            ChronoRexSecondaryButton(
                text = "Back",
                modifier = Modifier.fillMaxWidth(),
                onClick = onBack
            )
        }
    }
}

@Composable
private fun RestednessSection(value: Int, onChange: (Int) -> Unit) {
    Text("Restedness", style = MaterialTheme.typography.titleSmall)
    Slider(
        value = value.toFloat(),
        onValueChange = { onChange(it.toInt()) },
        valueRange = 0f..100f,
        steps = 20
    )
    Text(
        text = "$value | 0 Wiped out | 25 Heavy | 50 Meh | 75 Pretty good | 100 Fully charged",
        style = MaterialTheme.typography.bodySmall
    )
}

@Composable
private fun SleepQualitySection(value: Int, onChange: (Int) -> Unit) {
    Text("Sleep quality", style = MaterialTheme.typography.titleSmall)
    Row(horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.sm)) {
        (1..5).forEach { rating ->
            val colors = if (rating == value) {
                ChronoRexChipColors(
                    container = MaterialTheme.colorScheme.primary,
                    label = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                ChronoRexChipColors(
                    container = MaterialTheme.colorScheme.secondaryContainer,
                    label = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
            ChronoRexAssistChip(text = rating.toString(), onClick = { onChange(rating) }, colors = colors)
        }
    }
}

@Composable
private fun FlagRow(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.sm),
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(top = MaterialTheme.spacing.sm)
    ) {
        Checkbox(checked = checked, onCheckedChange = onCheckedChange)
        Text(label)
    }
}

@Composable
private fun NotesAndTagsSection(
    notes: String,
    onUpdateNotes: (String) -> Unit,
    tags: List<String>,
    onUpdateTags: (List<String>) -> Unit,
    suggestions: List<String>
) {
    var expanded by rememberSaveable { mutableStateOf(false) }
    var pendingTag by rememberSaveable { mutableStateOf("") }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = MaterialTheme.spacing.md)
            .clickable { expanded = !expanded },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Notes & emoji tags",
            style = MaterialTheme.typography.titleSmall,
            modifier = Modifier.weight(1f)
        )
        Icon(
            imageVector = if (expanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
            contentDescription = if (expanded) "Collapse notes" else "Expand notes",
            modifier = Modifier.size(24.dp)
        )
    }
    if (tags.isNotEmpty()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(top = MaterialTheme.spacing.sm),
            horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.xs),
            verticalAlignment = Alignment.CenterVertically
        ) {
            tags.forEach { tag ->
                ChronoRexAssistChip(
                    text = tag,
                    onClick = {
                        onUpdateTags(tags.filterNot { it.equals(tag, ignoreCase = true) })
                    }
                )
            }
            ChronoRexAssistChip(text = "Clear", onClick = { onUpdateTags(emptyList()) })
        }
    }
    if (expanded) {
        OutlinedTextField(
            value = notes,
            onValueChange = onUpdateNotes,
            label = { Text("Notes") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = MaterialTheme.spacing.sm)
        )
        if (suggestions.isNotEmpty()) {
            Text(
                text = "Recent emojis",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = MaterialTheme.spacing.sm)
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.xs)
            ) {
                suggestions.forEach { emoji ->
                    ChronoRexAssistChip(
                        text = emoji,
                        onClick = {
                            val updated = (tags + emoji).distinct().takeLast(6)
                            onUpdateTags(updated)
                        }
                    )
                }
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = MaterialTheme.spacing.sm),
            horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.xs),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = pendingTag,
                onValueChange = { pendingTag = it },
                label = { Text("Add emoji or tag") },
                modifier = Modifier.weight(1f),
                singleLine = true
            )
            ChronoRexAssistChip(
                text = "Add",
                enabled = pendingTag.isNotBlank(),
                onClick = {
                    val sanitized = pendingTag.trim()
                    if (sanitized.isNotEmpty()) {
                        val current = tags.filterNot { it.equals(sanitized, ignoreCase = true) }
                        val updated = (current + sanitized).takeLast(6)
                        onUpdateTags(updated)
                        pendingTag = ""
                    }
                }
            )
        }
        Text(
            text = "Tap a chip to remove it. We keep the six most recent emojis for quick reuse.",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = MaterialTheme.spacing.xs)
        )
    }
}
