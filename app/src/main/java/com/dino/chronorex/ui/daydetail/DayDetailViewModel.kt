package com.dino.chronorex.ui.daydetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dino.chronorex.data.repository.ActivityRepository
import com.dino.chronorex.data.repository.DayRepository
import com.dino.chronorex.data.repository.SymptomRepository
import com.dino.chronorex.model.ActivityEntry
import com.dino.chronorex.model.Day
import com.dino.chronorex.model.SymptomEntry
import java.time.LocalDate
import java.util.UUID
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch

data class DayDetailUiState(
    val date: LocalDate? = null,
    val day: Day? = null,
    val symptoms: List<SymptomEntry> = emptyList(),
    val activities: List<ActivityEntry> = emptyList(),
    val isLoading: Boolean = true
)

class DayDetailViewModel(
    private val dayRepository: DayRepository,
    private val symptomRepository: SymptomRepository,
    private val activityRepository: ActivityRepository
) : ViewModel() {

    private val selectedDate = MutableStateFlow<LocalDate?>(null)
    private val _state = MutableStateFlow(DayDetailUiState())
    val state: StateFlow<DayDetailUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            selectedDate
                .filterNotNull()
                .flatMapLatest { date ->
                    combine(
                        dayRepository.observeDay(date),
                        symptomRepository.observeSymptomsForDate(date),
                        activityRepository.observeActivitiesForDate(date)
                    ) { day, symptoms, activities ->
                        DayDetailUiState(
                            date = date,
                            day = day,
                            symptoms = symptoms,
                            activities = activities,
                            isLoading = false
                        )
                    }
                }
                .collect { uiState -> _state.value = uiState }
        }
    }

    fun setDate(date: LocalDate) {
        selectedDate.value = date
        _state.value = _state.value.copy(isLoading = true, date = date)
    }

    fun deleteSymptom(id: UUID) {
        viewModelScope.launch { symptomRepository.delete(id) }
    }

    fun deleteActivity(id: UUID) {
        viewModelScope.launch { activityRepository.delete(id) }
    }
}
