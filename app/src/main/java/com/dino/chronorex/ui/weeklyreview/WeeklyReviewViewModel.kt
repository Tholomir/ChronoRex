package com.dino.chronorex.ui.weeklyreview

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dino.chronorex.analytics.WeeklyReviewGenerator
import com.dino.chronorex.data.repository.ActivityRepository
import com.dino.chronorex.data.repository.DayRepository
import com.dino.chronorex.data.repository.SettingsRepository
import com.dino.chronorex.data.repository.SymptomRepository
import com.dino.chronorex.data.repository.WeeklyReviewRepository
import com.dino.chronorex.model.WeeklyReview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.time.Clock
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import java.util.UUID

data class WeeklyReviewUiState(
    val latestReview: WeeklyReview? = null,
    val showBanner: Boolean = false,
    val hasEnoughData: Boolean = false
)

class WeeklyReviewViewModel(
    private val weeklyReviewRepository: WeeklyReviewRepository,
    private val dayRepository: DayRepository,
    private val symptomRepository: SymptomRepository,
    private val activityRepository: ActivityRepository,
    private val settingsRepository: SettingsRepository,
    private val clock: Clock = Clock.systemDefaultZone()
) : ViewModel() {

    private val _state = MutableStateFlow(WeeklyReviewUiState())
    val state: StateFlow<WeeklyReviewUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                dayRepository.observeAll(),
                symptomRepository.observeAll(),
                activityRepository.observeAll(),
                settingsRepository.observeSettings(),
                weeklyReviewRepository.observeLatest()
            ) { days, symptoms, activities, settings, latest ->
                WeeklyReviewInputs(days, symptoms, activities, settings.notificationsDenied, latest)
            }.collect { inputs ->
                maybeGenerateReview(inputs)
                val hasData = inputs.days.size >= 7
                val latest = inputs.latestReview
                _state.value = WeeklyReviewUiState(
                    latestReview = latest,
                    showBanner = latest?.needsInAppNudge == true,
                    hasEnoughData = hasData
                )
            }
        }
    }

    fun markReviewOpened(id: UUID) {
        viewModelScope.launch {
            weeklyReviewRepository.markNudgeSeen(id)
        }
    }

    private suspend fun maybeGenerateReview(inputs: WeeklyReviewInputs) {
        val days = inputs.days
        if (days.size < 7) return
        val latestDay = days.maxOfOrNull { it.date } ?: return
        val latestStored = inputs.latestReview
        if (latestStored != null && !shouldGenerateNew(latestStored, latestDay)) {
            return
        }
        val generated = WeeklyReviewGenerator.generate(
            days = days,
            symptoms = inputs.symptoms,
            activities = inputs.activities,
            notificationsDenied = inputs.notificationsDenied,
            clock = clock
        ) ?: return
        if (latestStored == null || generated.endDate.isAfter(latestStored.endDate)) {
            weeklyReviewRepository.upsert(generated)
        }
    }

    private fun shouldGenerateNew(latest: WeeklyReview, latestDay: LocalDate): Boolean {
        val daysBetween = ChronoUnit.DAYS.between(latest.endDate, latestDay)
        return latestDay.isAfter(latest.endDate) && daysBetween >= 7
    }
}

private data class WeeklyReviewInputs(
    val days: List<com.dino.chronorex.model.Day>,
    val symptoms: List<com.dino.chronorex.model.SymptomEntry>,
    val activities: List<com.dino.chronorex.model.ActivityEntry>,
    val notificationsDenied: Boolean,
    val latestReview: WeeklyReview?
)

