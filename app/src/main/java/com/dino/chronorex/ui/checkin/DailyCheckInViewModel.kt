package com.dino.chronorex.ui.checkin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dino.chronorex.data.repository.DayRepository
import com.dino.chronorex.data.repository.SettingsRepository
import com.dino.chronorex.model.Day
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Clock
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime

data class DailyCheckInUiState(

    val targetDate: LocalDate,

    val timezoneOffsetMinutes: Int,

    val restedness: Int,

    val sleepQuality: Int,

    val notes: String,

    val emojiTags: List<String>,

    val illness: Boolean,

    val travel: Boolean,

    val existingEntry: Day? = null,

    val isSaving: Boolean = false,

    val beforeFourAmIsYesterday: Boolean = false,

    val undoRequestId: Long? = null

) {

    val canSave: Boolean = restedness in 0..100 && sleepQuality in 1..5



    val hasChanges: Boolean = existingEntry?.let { existing ->

        existing.restedness0To100 != restedness ||

            existing.sleepQuality1To5 != sleepQuality ||

            (existing.notes.orEmpty() != notes) ||

            existing.emojiTags != emojiTags ||

            existing.illness != illness ||

            existing.travel != travel

    } ?: true



    companion object {

        const val DEFAULT_RESTEDNESS = 50

        const val DEFAULT_SLEEP_QUALITY = 3

    }

}



class DailyCheckInViewModel(
    private val dayRepository: DayRepository,
    private val settingsRepository: SettingsRepository,
    private val clock: Clock = Clock.systemDefaultZone()
) : ViewModel() {

    private val targetDateFlow = MutableStateFlow(currentEntryDate(beforeFourPreference = false))
    private val _state: MutableStateFlow<DailyCheckInUiState>
    val state: StateFlow<DailyCheckInUiState> get() = _state.asStateFlow()

    private var observeJob: Job? = null
    private var lastSavedSnapshot: Day? = null

    init {
        val initialOffset = currentOffsetMinutes()
        _state = MutableStateFlow(
            DailyCheckInUiState(
                targetDate = targetDateFlow.value,
                timezoneOffsetMinutes = initialOffset,
                restedness = DailyCheckInUiState.DEFAULT_RESTEDNESS,
                sleepQuality = DailyCheckInUiState.DEFAULT_SLEEP_QUALITY,
                notes = "",
                emojiTags = emptyList(),
                illness = false,
                travel = false
            )
        )

        viewModelScope.launch {
            settingsRepository.observeSettings().collectLatest { settings ->
                val entryDate = currentEntryDate(settings.beforeFourAmIsYesterday)
                targetDateFlow.value = entryDate
                _state.update { current ->
                    current.copy(
                        targetDate = entryDate,
                        beforeFourAmIsYesterday = settings.beforeFourAmIsYesterday,
                        timezoneOffsetMinutes = currentOffsetMinutes()
                    )
                }
                observeCurrentDay()
            }
        }
    }

    private fun observeCurrentDay() {
        observeJob?.cancel()
        observeJob = viewModelScope.launch {
            targetDateFlow.collectLatest { date ->
                dayRepository.observeDay(date).collectLatest { day ->
                    applyDay(day)
                }
            }
        }
    }

    private fun applyDay(day: Day?) {
        _state.update { current ->
            if (day != null) {
                current.copy(
                    targetDate = day.date,
                    timezoneOffsetMinutes = day.timezoneOffsetMinutes,
                    restedness = day.restedness0To100,
                    sleepQuality = day.sleepQuality1To5,
                    notes = day.notes.orEmpty(),
                    emojiTags = day.emojiTags,
                    illness = day.illness,
                    travel = day.travel,
                    existingEntry = day
                )
            } else {
                current.copy(
                    existingEntry = null
                )
            }
        }
    }

    fun updateRestedness(value: Int) {
        _state.update { it.copy(restedness = value.coerceIn(0, 100)) }
    }

    fun updateSleepQuality(value: Int) {
        _state.update { it.copy(sleepQuality = value.coerceIn(1, 5)) }
    }

    fun updateNotes(notes: String) {
        _state.update { it.copy(notes = notes) }
    }

    fun updateEmojiTags(tags: List<String>) {
        _state.update { it.copy(emojiTags = tags.distinct()) }
    }

    fun updateIllness(checked: Boolean) {
        _state.update { it.copy(illness = checked) }
    }

    fun updateTravel(checked: Boolean) {
        _state.update { it.copy(travel = checked) }
    }

    fun setBeforeFourAmPreference(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.update { current -> current.copy(beforeFourAmIsYesterday = enabled) }
        }
    }

    fun setTargetDate(date: LocalDate) {
        targetDateFlow.value = date
    }

    fun saveEntry() {
        val snapshot = _state.value
        if (!snapshot.canSave || snapshot.isSaving) return
        viewModelScope.launch {
            _state.update { it.copy(isSaving = true) }
            val day = Day(
                date = snapshot.targetDate,
                timezoneOffsetMinutes = currentOffsetMinutes(),
                restedness0To100 = snapshot.restedness,
                sleepQuality1To5 = snapshot.sleepQuality,
                notes = snapshot.notes.ifBlank { null },
                emojiTags = snapshot.emojiTags,
                illness = snapshot.illness,
                travel = snapshot.travel
            )
            lastSavedSnapshot = snapshot.existingEntry
            dayRepository.upsert(day)
            _state.update { it.copy(isSaving = false, undoRequestId = System.currentTimeMillis()) }
        }
    }

    fun clearUndoRequest() {
        lastSavedSnapshot = null
        _state.update { it.copy(undoRequestId = null) }
    }

    fun undoLastSave() {
        val snapshot = _state.value
        viewModelScope.launch {
            val previous = lastSavedSnapshot
            if (previous == null) {
                dayRepository.deleteByDate(snapshot.targetDate)
            } else {
                dayRepository.upsert(previous)
            }
            lastSavedSnapshot = null
            _state.update { it.copy(undoRequestId = null) }
        }
    }

    private fun currentEntryDate(beforeFourPreference: Boolean): LocalDate {
        val now = ZonedDateTime.now(clock)
        return if (beforeFourPreference && now.toLocalTime().isBefore(FOUR_AM)) {
            now.minusDays(1).toLocalDate()
        } else {
            now.toLocalDate()
        }
    }

    private fun currentOffsetMinutes(): Int {
        val zoneId = ZoneId.systemDefault()
        val now = LocalDateTime.now(clock).atZone(zoneId)
        return now.offset.totalSeconds / 60
    }

    companion object {
        private val FOUR_AM: LocalTime = LocalTime.of(4, 0)
    }
}
