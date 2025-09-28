package com.dino.chronorex.ui.calendar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dino.chronorex.data.repository.ActivityRepository
import com.dino.chronorex.data.repository.DayRepository
import com.dino.chronorex.data.repository.SymptomRepository
import com.dino.chronorex.model.ActivityEntry
import com.dino.chronorex.notification.ReminderManager
import com.dino.chronorex.model.Day
import com.dino.chronorex.model.SymptomEntry
import java.time.Clock
import java.time.LocalDate
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

data class CalendarUiState(
    val days: List<Day> = emptyList(),
    val selectedDate: LocalDate = LocalDate.now(),
    val today: LocalDate = LocalDate.now(),
    val symptomsByDate: Map<LocalDate, List<SymptomEntry>> = emptyMap(),
    val activitiesByDate: Map<LocalDate, List<ActivityEntry>> = emptyMap()
) {
    val selectedSymptoms: List<SymptomEntry> get() = symptomsByDate[selectedDate].orEmpty()
    val selectedActivities: List<ActivityEntry> get() = activitiesByDate[selectedDate].orEmpty()
    val selectedDay: Day? get() = days.firstOrNull { it.date == selectedDate }
    val todayEntry: Day? get() = days.firstOrNull { it.date == today }
    val isTodayLogged: Boolean get() = todayEntry != null
}

class CalendarViewModel(
    private val dayRepository: DayRepository,
    private val symptomRepository: SymptomRepository,
    private val activityRepository: ActivityRepository,
    private val reminderManager: ReminderManager,
    private val clock: Clock = Clock.systemDefaultZone()
) : ViewModel() {

    private val selectedDate = MutableStateFlow(LocalDate.now(clock))
    private val _state = MutableStateFlow(
        CalendarUiState(
            selectedDate = selectedDate.value,
            today = LocalDate.now(clock)
        )
    )
    val state: StateFlow<CalendarUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                dayRepository.observeAll(),
                symptomRepository.observeAll(),
                activityRepository.observeAll(),
                selectedDate
            ) { days, symptoms, activities, date ->
                CalendarUiState(
                    days = days,
                    selectedDate = date,
                    today = LocalDate.now(clock),
                    symptomsByDate = symptoms.groupBy { it.date },
                    activitiesByDate = activities.groupBy { it.date }
                )
            }.collect { uiState ->
                _state.value = uiState
            }
        }
    }

    fun selectDate(date: LocalDate) {
        selectedDate.value = date
    }

    fun resetToToday() {
        selectedDate.value = LocalDate.now(clock)
    }

    fun markIllness(date: LocalDate) {
        markFlag(date, illness = true)
    }

    fun markTravel(date: LocalDate) {
        markFlag(date, travel = true)
    }

    private fun markFlag(date: LocalDate, illness: Boolean = false, travel: Boolean = false) {
        viewModelScope.launch {
            val existing = dayRepository.getDay(date)
            val zone = clock.zone
            val offsetMinutes = date.atStartOfDay(zone).offset.totalSeconds / 60
            val base = existing ?: Day(
                date = date,
                timezoneOffsetMinutes = offsetMinutes,
                restedness0To100 = 50,
                sleepQuality1To5 = 3,
                notes = null,
                emojiTags = emptyList(),
                illness = false,
                travel = false
            )
            val updated = base.copy(
                illness = illness || base.illness,
                travel = travel || base.travel
            )
            dayRepository.upsert(updated)
            if (illness || travel) {
                reminderManager.snoozeForNextDay()
            }
        }
    }
}


