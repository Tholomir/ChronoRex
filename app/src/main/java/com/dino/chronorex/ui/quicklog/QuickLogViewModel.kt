package com.dino.chronorex.ui.quicklog

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dino.chronorex.data.repository.ActivityRepository
import com.dino.chronorex.data.repository.SymptomRepository
import com.dino.chronorex.model.ActivityEntry
import com.dino.chronorex.model.SymptomEntry
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.util.UUID

data class SymptomDraft(
    val name: String = "",
    val severity: Int = 5,
    val note: String = ""
) {
    val isValid: Boolean get() = name.isNotBlank()
}

data class ActivityDraft(
    val type: String = "",
    val durationMinutes: Int? = null,
    val perceivedExhaustion: Int = 5,
    val note: String = ""
) {
    val isValid: Boolean get() = type.isNotBlank()
}

data class QuickLogState(
    val symptomDraft: SymptomDraft = SymptomDraft(),
    val activityDraft: ActivityDraft = ActivityDraft(),
    val isSavingSymptom: Boolean = false,
    val isSavingActivity: Boolean = false
)

class QuickLogViewModel(
    private val symptomRepository: SymptomRepository,
    private val activityRepository: ActivityRepository,
    private val clock: Clock = Clock.systemDefaultZone()
) : ViewModel() {

    private val _state = MutableStateFlow(QuickLogState())
    val state: StateFlow<QuickLogState> = _state.asStateFlow()

    fun updateSymptomName(name: String) {
        _state.update { it.copy(symptomDraft = it.symptomDraft.copy(name = name)) }
    }

    fun updateSymptomSeverity(severity: Int) {
        _state.update {
            it.copy(symptomDraft = it.symptomDraft.copy(severity = severity.coerceIn(1, 10)))
        }
    }

    fun updateSymptomNote(note: String) {
        _state.update { it.copy(symptomDraft = it.symptomDraft.copy(note = note)) }
    }

    fun submitSymptom(date: LocalDate, onSaved: () -> Unit = {}) {
        val draft = _state.value.symptomDraft
        if (!draft.isValid || _state.value.isSavingSymptom) return
        viewModelScope.launch {
            _state.update { it.copy(isSavingSymptom = true) }
            val entry = SymptomEntry(
                id = UUID.randomUUID(),
                date = date,
                time = Instant.now(clock),
                name = draft.name.trim(),
                severity1To10 = draft.severity,
                note = draft.note.ifBlank { null }
            )
            symptomRepository.upsert(entry)
            _state.update { it.copy(symptomDraft = SymptomDraft(), isSavingSymptom = false) }
            onSaved()
        }
    }

    fun updateActivityType(type: String) {
        _state.update { it.copy(activityDraft = it.activityDraft.copy(type = type)) }
    }

    fun updateActivityDuration(minutes: Int?) {
        val sanitised = minutes?.coerceAtLeast(0)
        _state.update { it.copy(activityDraft = it.activityDraft.copy(durationMinutes = sanitised)) }
    }

    fun updateActivityPerceivedExhaustion(exhaustion: Int) {
        _state.update {
            it.copy(activityDraft = it.activityDraft.copy(perceivedExhaustion = exhaustion.coerceIn(1, 10)))
        }
    }

    fun updateActivityNote(note: String) {
        _state.update { it.copy(activityDraft = it.activityDraft.copy(note = note)) }
    }

    fun submitActivity(date: LocalDate, onSaved: () -> Unit = {}) {
        val draft = _state.value.activityDraft
        if (!draft.isValid || _state.value.isSavingActivity) return
        viewModelScope.launch {
            _state.update { it.copy(isSavingActivity = true) }
            val entry = ActivityEntry(
                id = UUID.randomUUID(),
                date = date,
                time = Instant.now(clock),
                type = draft.type.trim(),
                durationMinutes = draft.durationMinutes,
                perceivedExhaustion1To10 = draft.perceivedExhaustion,
                note = draft.note.ifBlank { null }
            )
            activityRepository.upsert(entry)
            _state.update { it.copy(activityDraft = ActivityDraft(), isSavingActivity = false) }
            onSaved()
        }
    }
}
