package com.dino.chronorex.ui.insights

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dino.chronorex.analytics.CorrelationInsights
import com.dino.chronorex.analytics.InsightsCalculator
import com.dino.chronorex.analytics.InsightsResult
import com.dino.chronorex.analytics.TrendInsights
import com.dino.chronorex.data.repository.ActivityRepository
import com.dino.chronorex.data.repository.DayRepository
import com.dino.chronorex.data.repository.SymptomRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

sealed class InsightsUiState {
    data object Loading : InsightsUiState()
    data object Empty : InsightsUiState()
    data class Ready(
        val trend: TrendInsights?,
        val correlations: List<CorrelationInsights>,
        val generatedAtIso: String
    ) : InsightsUiState()
}

class InsightsViewModel(
    private val dayRepository: DayRepository,
    private val symptomRepository: SymptomRepository,
    private val activityRepository: ActivityRepository
) : ViewModel() {

    private val _state = MutableStateFlow<InsightsUiState>(InsightsUiState.Loading)
    val state: StateFlow<InsightsUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                dayRepository.observeAll(),
                symptomRepository.observeAll(),
                activityRepository.observeAll()
            ) { days, symptoms, activities ->
                InsightsCalculator.calculate(days, symptoms, activities)
            }.collect { result ->
                _state.value = when (result) {
                    InsightsResult.Empty -> InsightsUiState.Empty
                    is InsightsResult.Ready -> InsightsUiState.Ready(
                        trend = result.trend,
                        correlations = result.correlations,
                        generatedAtIso = result.generatedAt.toString()
                    )
                }
            }
        }
    }
}
