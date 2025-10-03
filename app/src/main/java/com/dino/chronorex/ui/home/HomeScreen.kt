package com.dino.chronorex.ui.home

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlin.math.roundToInt
import com.dino.chronorex.ui.ChronoRexViewModelFactory
import com.dino.chronorex.ui.calendar.CalendarUiState
import com.dino.chronorex.ui.calendar.CalendarViewModel
import com.dino.chronorex.ui.components.BottomQuickActionsBar
import com.dino.chronorex.ui.components.ChronoRexAssistChip
import com.dino.chronorex.ui.components.ChronoRexCard
import com.dino.chronorex.ui.components.ChronoRexChipColors
import com.dino.chronorex.ui.components.ChronoRexPrimaryButton
import com.dino.chronorex.ui.quicklog.QuickLogState
import com.dino.chronorex.ui.quicklog.QuickLogViewModel
import com.dino.chronorex.ui.weeklyreview.WeeklyReviewUiState
import com.dino.chronorex.ui.weeklyreview.WeeklyReviewViewModel
import com.dino.chronorex.ui.theme.spacing
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter

@Composable
fun HomeScreenRoute(
    factory: ChronoRexViewModelFactory,
    onNavigateCheckIn: () -> Unit,
    onNavigateDetail: (LocalDate) -> Unit,
    onNavigateInsights: () -> Unit,
    onNavigateSettings: () -> Unit,
    onNavigateWeeklyReview: () -> Unit,
    onNavigateExport: () -> Unit
) {
    val calendarViewModel: CalendarViewModel = viewModel(factory = factory)
    val quickLogViewModel: QuickLogViewModel = viewModel(factory = factory)
    val weeklyReviewViewModel: WeeklyReviewViewModel = viewModel(factory = factory)
    val calendarState by calendarViewModel.state.collectAsState()
    val quickLogState by quickLogViewModel.state.collectAsState()
    val weeklyReviewState by weeklyReviewViewModel.state.collectAsState()

    Scaffold(
        bottomBar = {
            BottomQuickActionsBar(
                onLogSymptom = { quickLogViewModel.submitSymptom(calendarState.selectedDate) },
                onLogActivity = { quickLogViewModel.submitActivity(calendarState.selectedDate) }
            )
        }
    ) { innerPadding ->
        HomeScreen(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            state = calendarState,
            quickLogState = quickLogState,
            weeklyReviewState = weeklyReviewState,
            onSelectDate = calendarViewModel::selectDate,
            onResetToToday = calendarViewModel::resetToToday,
            onNavigateCheckIn = onNavigateCheckIn,
            onNavigateDetail = onNavigateDetail,
            onNavigateInsights = onNavigateInsights,
            onNavigateSettings = onNavigateSettings,
            onLogSymptomQuick = { date -> quickLogViewModel.submitSymptom(date) },
            onLogActivityQuick = { date -> quickLogViewModel.submitActivity(date) },
            onMarkIllness = calendarViewModel::markIllness,
            onMarkTravel = calendarViewModel::markTravel,
            onUpdateSymptomName = quickLogViewModel::updateSymptomName,
            onUpdateSymptomSeverity = quickLogViewModel::updateSymptomSeverity,
            onUpdateSymptomNote = quickLogViewModel::updateSymptomNote,
            onSubmitSymptom = { date -> quickLogViewModel.submitSymptom(date) },
            onUpdateActivityType = quickLogViewModel::updateActivityType,
            onUpdateActivityDuration = quickLogViewModel::updateActivityDuration,
            onUpdateActivityPerceivedExhaustion = quickLogViewModel::updateActivityPerceivedExhaustion,
            onUpdateActivityNote = quickLogViewModel::updateActivityNote,
            onSubmitActivity = { date -> quickLogViewModel.submitActivity(date) },
            onNavigateWeeklyReview = {
                weeklyReviewState.latestReview?.let { weeklyReviewViewModel.markReviewOpened(it.id) }
                onNavigateWeeklyReview()
            },
            onDismissWeeklyReviewBanner = {
                weeklyReviewState.latestReview?.id?.let(weeklyReviewViewModel::markReviewOpened)
            },
            onNavigateExport = onNavigateExport
        )
    }
}

enum class QuickAction { LogSymptom, LogActivity, EditCheckIn, MarkIllness, MarkTravel }

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    state: CalendarUiState,
    quickLogState: QuickLogState,
    weeklyReviewState: WeeklyReviewUiState,
    onSelectDate: (LocalDate) -> Unit,
    onResetToToday: () -> Unit,
    onNavigateCheckIn: () -> Unit,
    onNavigateDetail: (LocalDate) -> Unit,
    onNavigateInsights: () -> Unit,
    onNavigateSettings: () -> Unit,
    onLogSymptomQuick: (LocalDate) -> Unit,
    onLogActivityQuick: (LocalDate) -> Unit,
    onMarkIllness: (LocalDate) -> Unit,
    onMarkTravel: (LocalDate) -> Unit,
    onUpdateSymptomName: (String) -> Unit,
    onUpdateSymptomSeverity: (Int) -> Unit,
    onUpdateSymptomNote: (String) -> Unit,
    onSubmitSymptom: (LocalDate) -> Unit,
    onUpdateActivityType: (String) -> Unit,
    onUpdateActivityDuration: (Int?) -> Unit,
    onUpdateActivityPerceivedExhaustion: (Int) -> Unit,
    onUpdateActivityNote: (String) -> Unit,
    onSubmitActivity: (LocalDate) -> Unit
) {
    var quickActionsForDate by remember { mutableStateOf<LocalDate?>(null) }

    Column(
        modifier = modifier
            .padding(MaterialTheme.spacing.lg),
        verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.lg)
    ) {
        GreetingSection(today = state.today, onNavigateSettings = onNavigateSettings)
        SparklineSection(state = state, onNavigateInsights = onNavigateInsights)
        if (weeklyReviewState.showBanner) {
            WeeklyReviewBanner(onView = onNavigateWeeklyReview, onDismiss = onDismissWeeklyReviewBanner)
        } else if (weeklyReviewState.hasEnoughData) {
            WeeklyReviewTeaser(onNavigateWeeklyReview = onNavigateWeeklyReview)
        }
        if (!state.isTodayLogged) {
            CheckInPromptCard(onNavigateCheckIn = onNavigateCheckIn)
        }
        CalendarSection(
            state = state,
            onSelectDate = onSelectDate,
            onResetToToday = onResetToToday,
            onNavigateDetail = onNavigateDetail,
            onNavigateCheckIn = onNavigateCheckIn,
            onLongPressDate = { date -> quickActionsForDate = date }
        )
        val symptomSuggestions = topUsedLabels(
            state.symptomsByDate.values.flatten().map { it.name },
            limit = 6
        )
        val activitySuggestions = topUsedLabels(
            state.activitiesByDate.values.flatten().map { it.type },
            limit = 8
        )
        val durationSuggestions = topDurationPresets(
            state.activitiesByDate.values.flatten().mapNotNull { it.durationMinutes },
            limit = 5
        )

        QuickLogSection(
            selectedDate = state.selectedDate,
            state = quickLogState,
            symptomSuggestions = symptomSuggestions,
            activitySuggestions = activitySuggestions,
            durationSuggestions = durationSuggestions,
            onUpdateSymptomName = onUpdateSymptomName,
            onUpdateSymptomSeverity = onUpdateSymptomSeverity,
            onUpdateSymptomNote = onUpdateSymptomNote,
            onSubmitSymptom = onSubmitSymptom,
            onUpdateActivityType = onUpdateActivityType,
            onUpdateActivityDuration = onUpdateActivityDuration,
            onUpdateActivityPerceivedExhaustion = onUpdateActivityPerceivedExhaustion,
            onUpdateActivityNote = onUpdateActivityNote,
            onSubmitActivity = onSubmitActivity
        )
        ChronoRexPrimaryButton(
            text = "Export data",
            modifier = Modifier.fillMaxWidth(),
            onClick = onNavigateExport
        )
        ChronoRexPrimaryButton(
            text = if (state.isTodayLogged) "Edit today's check-in" else "Start today's check-in",
            onClick = onNavigateCheckIn
        )
    }

    quickActionsForDate?.let { date ->
        QuickActionSheet(
            date = date,
            onDismiss = { quickActionsForDate = null },
            onAction = { action ->
                when (action) {
                    QuickAction.LogSymptom -> onLogSymptomQuick(date)
                    QuickAction.LogActivity -> onLogActivityQuick(date)
                    QuickAction.EditCheckIn -> onNavigateCheckIn()
                    QuickAction.MarkIllness -> onMarkIllness(date)
                    QuickAction.MarkTravel -> onMarkTravel(date)
                }
                quickActionsForDate = null
            }
        )
    }
}



@Composable
private fun GreetingSection(today: LocalDate, onNavigateSettings: () -> Unit) {
    ChronoRexCard {
        Text("Today is ${today}", style = MaterialTheme.typography.titleMedium)
        ChronoRexPrimaryButton(
            text = "Settings",
            modifier = Modifier.padding(top = MaterialTheme.spacing.sm),
            onClick = onNavigateSettings
        )
    }
}


@Composable
private fun SparklineSection(
    state: CalendarUiState,
    onNavigateInsights: () -> Unit
) {
    val recentDays = remember(state.days, state.today) {
        state.days
            .filter { !it.date.isAfter(state.today) }
            .sortedBy { it.date }
            .takeLast(14)
    }
    val averageRestedness = remember(recentDays) {
        if (recentDays.isEmpty()) 0.0 else recentDays.map { it.restedness0To100 }.average()
    }
    ChronoRexCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onNavigateInsights),
        tonal = true
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.sm)) {
            Text("Restedness trend", style = MaterialTheme.typography.titleMedium)
            if (recentDays.size < 2) {
                Text(
                    text = "Log a few mornings to see your trend line.",
                    style = MaterialTheme.typography.bodyMedium
                )
            } else {
                SparklineChart(values = recentDays.map { it.restedness0To100.toFloat() })
                Text(
                    text = "14-day avg ${averageRestedness.roundToInt()} | Today ${recentDays.last().restedness0To100}",
                    style = MaterialTheme.typography.bodySmall
                )
            }
            Text(
                text = "Tap to open insights",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun SparklineChart(values: List<Float>) {
    val density = LocalDensity.current
    val strokeWidth = with(density) { 3.dp.toPx() }
    val pointRadius = strokeWidth * 1.5f
    val trendColor = MaterialTheme.colorScheme.tertiary
    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
            .padding(top = MaterialTheme.spacing.sm)
    ) {
        if (values.isEmpty()) return@Canvas
        val count = values.size
        val maxValue = values.maxOrNull() ?: 100f
        val minValue = values.minOrNull() ?: 0f
        val range = (maxValue - minValue).takeIf { it > 0f } ?: 1f
        val path = Path()
        values.forEachIndexed { index, value ->
            val x = if (count == 1) size.width / 2f else size.width * index / (count - 1)
            val normalized = (value - minValue) / range
            val y = size.height - (normalized * size.height)
            if (index == 0) {
                path.moveTo(x, y)
            } else {
                path.lineTo(x, y)
            }
        }
        drawPath(
            path = path,
            color = trendColor,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
        )
        values.forEachIndexed { index, value ->
            val x = if (count == 1) size.width / 2f else size.width * index / (count - 1)
            val normalized = (value - minValue) / range
            val y = size.height - (normalized * size.height)
            drawCircle(
                color = trendColor,
                radius = pointRadius,
                center = Offset(x, y)
            )
        }
    }
}

@Composable
private fun CheckInPromptCard(onNavigateCheckIn: () -> Unit) {
    ChronoRexCard {
        Text("No AM check-in yet", style = MaterialTheme.typography.titleMedium)
        Text(
            text = "Capture restedness and sleep to unlock trends.",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(top = MaterialTheme.spacing.sm)
        )
        ChronoRexPrimaryButton(
            text = "Start check-in",
            modifier = Modifier.padding(top = MaterialTheme.spacing.sm),
            onClick = onNavigateCheckIn
        )
    }
}

@Composable
private fun WeeklyReviewBanner(onView: () -> Unit, onDismiss: () -> Unit) {
    ChronoRexCard {
        Text(
            text = "Your weekly review is ready",
            style = MaterialTheme.typography.titleMedium
        )
        Text(
            text = "See seven-day highlights and trends without leaving the device.",
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(top = MaterialTheme.spacing.xs)
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = MaterialTheme.spacing.sm),
            horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.sm)
        ) {
            ChronoRexPrimaryButton(
                text = "View now",
                modifier = Modifier.fillMaxWidth(0.55f),
                onClick = onView
            )
            ChronoRexAssistChip(
                text = "Later",
                onClick = onDismiss,
                colors = ChronoRexChipColors(
                    container = MaterialTheme.colorScheme.secondaryContainer,
                    label = MaterialTheme.colorScheme.onSecondaryContainer
                )
            )
        }
    }
}

@Composable
private fun WeeklyReviewTeaser(onNavigateWeeklyReview: () -> Unit) {
    ChronoRexCard(tonal = true) {
        Text(
            text = "Weekly summary",
            style = MaterialTheme.typography.titleMedium
        )
        Text(
            text = "Open a one-page recap of fatigue, symptoms, and activities.",
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(top = MaterialTheme.spacing.xs)
        )
        ChronoRexPrimaryButton(
            text = "Open weekly review",
            modifier = Modifier.padding(top = MaterialTheme.spacing.sm),
            onClick = onNavigateWeeklyReview
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun CalendarSection(
    state: CalendarUiState,
    onSelectDate: (LocalDate) -> Unit,
    onResetToToday: () -> Unit,
    onNavigateDetail: (LocalDate) -> Unit,
    onNavigateCheckIn: () -> Unit,
    onLongPressDate: (LocalDate) -> Unit
) {
    val month = YearMonth.from(state.selectedDate)
    val firstDay = month.atDay(1)
    val daysInMonth = month.lengthOfMonth()
    val offset = firstDay.dayOfWeek.value % 7
    val today = state.today
    val formatter = DateTimeFormatter.ofPattern("MMMM yyyy")

    ChronoRexCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = formatter.format(month), style = MaterialTheme.typography.titleMedium)
            ChronoRexAssistChip(text = "Today", onClick = onResetToToday)
        }
        LazyVerticalGrid(
            columns = GridCells.Fixed(7),
            modifier = Modifier
                .fillMaxWidth()
                .height(240.dp)
                .padding(top = MaterialTheme.spacing.sm),
            horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.xs),
            verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.xs)
        ) {
            items(offset) { Spacer(modifier = Modifier.height(1.dp)) }
            items(daysInMonth) { index ->
                val date = firstDay.plusDays(index.toLong())
                val dayEntry = state.days.firstOrNull { it.date == date }
                val symptoms = state.symptomsByDate[date].orEmpty()
                val activities = state.activitiesByDate[date].orEmpty()
                val background = dayEntry?.restedness0To100?.let { restednessColor(it) } ?: MaterialTheme.colorScheme.surfaceVariant
                val isSelected = date == state.selectedDate
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .clip(MaterialTheme.shapes.small)
                        .background(background)
                        .padding(6.dp)
                        .pointerInput(date) {
                            detectTapGestures(
                                onTap = { onSelectDate(date) },
                                onLongPress = { onLongPressDate(date) }
                            )
                        }
                ) {
                    Text(date.dayOfMonth.toString(), style = MaterialTheme.typography.labelLarge)
                    val badgeText = when {
                        symptoms.isNotEmpty() && activities.isNotEmpty() -> "SA"
                        symptoms.isNotEmpty() -> "S"
                        activities.isNotEmpty() -> "A"
                        else -> ""
                    }
                    if (badgeText.isNotEmpty()) {
                        Text(badgeText, style = MaterialTheme.typography.labelSmall)
                    }
                    if (date == today) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary)
                        )
                    }
                    if (isSelected) {
                        ChronoRexAssistChip(
                            text = "Detail",
                            modifier = Modifier.padding(top = MaterialTheme.spacing.xs),
                            onClick = { onNavigateDetail(date) }
                        )
                    }
                    if (date == state.today) {
                        ChronoRexAssistChip(
                            text = if (state.isTodayLogged) "Edit" else "Check-in",
                            modifier = Modifier.padding(top = MaterialTheme.spacing.xs),
                            onClick = onNavigateCheckIn
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun restednessColor(value: Int): Color {
    val scheme = MaterialTheme.colorScheme
    return when {
        value < 25 -> scheme.error.copy(alpha = 0.24f)
        value < 50 -> scheme.tertiary.copy(alpha = 0.20f)
        value < 75 -> scheme.primary.copy(alpha = 0.18f)
        else -> scheme.secondary.copy(alpha = 0.18f)
    }
}


private fun topUsedLabels(values: List<String>, limit: Int): List<String> {
    if (values.isEmpty()) return emptyList()
    val counts = mutableMapOf<String, Int>()
    val displayNames = mutableMapOf<String, String>()
    values.forEach { raw ->
        val trimmed = raw.trim()
        if (trimmed.isEmpty()) return@forEach
        val key = trimmed.lowercase()
        displayNames.putIfAbsent(key, trimmed)
        counts[key] = counts.getOrDefault(key, 0) + 1
    }
    if (counts.isEmpty()) return emptyList()
    return counts.entries
        .sortedWith { a, b ->
            val countCompare = b.value.compareTo(a.value)
            if (countCompare != 0) {
                countCompare
            } else {
                displayNames[a.key].orEmpty().compareTo(displayNames[b.key].orEmpty())
            }
        }
        .mapNotNull { displayNames[it.key] }
        .take(limit)
}

private fun topDurationPresets(values: List<Int>, limit: Int): List<Int> {
    if (values.isEmpty()) return emptyList()
    val counts = mutableMapOf<Int, Int>()
    values.forEach { minutes ->
        if (minutes <= 0) return@forEach
        val rounded = ((minutes + 4) / 5) * 5
        counts[rounded] = counts.getOrDefault(rounded, 0) + 1
    }
    if (counts.isEmpty()) return emptyList()
    return counts.entries
        .sortedWith { a, b ->
            val countCompare = b.value.compareTo(a.value)
            if (countCompare != 0) {
                countCompare
            } else {
                a.key.compareTo(b.key)
            }
        }
        .map { it.key }
        .take(limit)
}

@Composable
private fun PresetChipRow(
    items: List<String>,
    selectedValue: String,
    onSelect: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(top = MaterialTheme.spacing.sm),
        horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.xs)
    ) {
        items.forEach { item ->
            val isSelected = selectedValue.equals(item, ignoreCase = true)
            ChronoRexAssistChip(
                text = item,
                onClick = { onSelect(item) },
                colors = chipColors(isSelected)
            )
        }
    }
}

@Composable
private fun DurationPresetRow(
    presets: List<Int>,
    selectedDuration: Int?,
    onSelect: (Int?) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(top = MaterialTheme.spacing.sm),
        horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.xs)
    ) {
        presets.forEach { minutes ->
            ChronoRexAssistChip(
                text = "$minutes min",
                onClick = { onSelect(minutes) },
                colors = chipColors(selectedDuration == minutes)
            )
        }
        ChronoRexAssistChip(
            text = "Clear",
            onClick = { onSelect(null) },
            colors = chipColors(selectedDuration == null)
        )
    }
}

@Composable
private fun chipColors(isSelected: Boolean): ChronoRexChipColors {
    return if (isSelected) {
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
}

@Composable
private fun QuickLogSection(
    selectedDate: LocalDate,
    state: QuickLogState,
    symptomSuggestions: List<String>,
    activitySuggestions: List<String>,
    durationSuggestions: List<Int>,
    onUpdateSymptomName: (String) -> Unit,
    onUpdateSymptomSeverity: (Int) -> Unit,
    onUpdateSymptomNote: (String) -> Unit,
    onSubmitSymptom: (LocalDate) -> Unit,
    onUpdateActivityType: (String) -> Unit,
    onUpdateActivityDuration: (Int?) -> Unit,
    onUpdateActivityPerceivedExhaustion: (Int) -> Unit,
    onUpdateActivityNote: (String) -> Unit,
    onSubmitActivity: (LocalDate) -> Unit
) {
    ChronoRexCard {
        Text("Quick symptom log", style = MaterialTheme.typography.titleMedium)
        if (symptomSuggestions.isNotEmpty()) {
            PresetChipRow(
                items = symptomSuggestions,
                selectedValue = state.symptomDraft.name,
                onSelect = onUpdateSymptomName
            )
        }
        OutlinedTextField(
            value = state.symptomDraft.name,
            onValueChange = onUpdateSymptomName,
            label = { Text("Symptom") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = MaterialTheme.spacing.sm)
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.sm),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(top = MaterialTheme.spacing.sm)
        ) {
            Text("Severity: ${state.symptomDraft.severity}")
            ChronoRexAssistChip(text = "-", onClick = { onUpdateSymptomSeverity((state.symptomDraft.severity - 1).coerceAtLeast(1)) })
            ChronoRexAssistChip(text = "+", onClick = { onUpdateSymptomSeverity((state.symptomDraft.severity + 1).coerceAtMost(10)) })
        }
        OutlinedTextField(
            value = state.symptomDraft.note,
            onValueChange = onUpdateSymptomNote,
            label = { Text("Notes") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = MaterialTheme.spacing.sm)
        )
        ChronoRexPrimaryButton(
            text = "Save symptom",
            modifier = Modifier.padding(top = MaterialTheme.spacing.sm),
            onClick = { onSubmitSymptom(selectedDate) },
            enabled = state.symptomDraft.isValid && !state.isSavingSymptom
        )
        Spacer(modifier = Modifier.height(MaterialTheme.spacing.lg))
        Text("Quick activity log", style = MaterialTheme.typography.titleMedium)
        if (activitySuggestions.isNotEmpty()) {
            PresetChipRow(
                items = activitySuggestions,
                selectedValue = state.activityDraft.type,
                onSelect = onUpdateActivityType
            )
        }
        OutlinedTextField(
            value = state.activityDraft.type,
            onValueChange = onUpdateActivityType,
            label = { Text("Activity") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = MaterialTheme.spacing.sm)
        )
        DurationPresetRow(
            presets = durationSuggestions,
            selectedDuration = state.activityDraft.durationMinutes,
            onSelect = onUpdateActivityDuration
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.sm),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(top = MaterialTheme.spacing.sm)
        ) {
            val minutes = state.activityDraft.durationMinutes ?: 0
            Text("Minutes: $minutes")
            ChronoRexAssistChip(text = "-", onClick = { onUpdateActivityDuration((minutes - 5).coerceAtLeast(0)) })
            ChronoRexAssistChip(text = "+", onClick = { onUpdateActivityDuration(minutes + 5) })
        }
        Row(
            horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.sm),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(top = MaterialTheme.spacing.sm)
        ) {
            Text("Exhaustion: ${state.activityDraft.perceivedExhaustion}")
            ChronoRexAssistChip(text = "-", onClick = { onUpdateActivityPerceivedExhaustion((state.activityDraft.perceivedExhaustion - 1).coerceAtLeast(1)) })
            ChronoRexAssistChip(text = "+", onClick = { onUpdateActivityPerceivedExhaustion((state.activityDraft.perceivedExhaustion + 1).coerceAtMost(10)) })
        }
        OutlinedTextField(
            value = state.activityDraft.note,
            onValueChange = onUpdateActivityNote,
            label = { Text("Notes") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = MaterialTheme.spacing.sm)
        )
        ChronoRexPrimaryButton(
            text = "Save activity",
            modifier = Modifier.padding(top = MaterialTheme.spacing.sm),
            onClick = { onSubmitActivity(selectedDate) },
            enabled = state.activityDraft.isValid && !state.isSavingActivity
        )
    }
}



@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun QuickActionSheet(
    date: LocalDate,
    onDismiss: () -> Unit,
    onAction: (QuickAction) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(MaterialTheme.spacing.lg),
            verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.sm)
        ) {
            Text(text = "Actions for ${date}", style = MaterialTheme.typography.titleMedium)
            ChronoRexPrimaryButton(text = "Log symptom", onClick = { onAction(QuickAction.LogSymptom) })
            ChronoRexPrimaryButton(text = "Log activity", onClick = { onAction(QuickAction.LogActivity) })
            ChronoRexPrimaryButton(text = "Edit AM check-in", onClick = { onAction(QuickAction.EditCheckIn) })
            ChronoRexPrimaryButton(text = "Mark illness", onClick = { onAction(QuickAction.MarkIllness) })
            ChronoRexPrimaryButton(text = "Mark travel", onClick = { onAction(QuickAction.MarkTravel) })
        }
    }
}

