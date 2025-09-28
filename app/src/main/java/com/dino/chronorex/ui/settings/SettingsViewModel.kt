package com.dino.chronorex.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dino.chronorex.data.repository.SettingsRepository
import com.dino.chronorex.model.Settings
import com.dino.chronorex.notification.ReminderManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalTime

data class SettingsUiState(
    val settings: Settings = Settings.default(),
    val isLoading: Boolean = true
)

class SettingsViewModel(
    private val settingsRepository: SettingsRepository,
    private val reminderManager: ReminderManager
) : ViewModel() {

    private val _state = MutableStateFlow(SettingsUiState())
    val state: StateFlow<SettingsUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            settingsRepository.observeSettings().collectLatest { settings ->
                _state.value = SettingsUiState(settings = settings, isLoading = false)
            }
        }
    }

    fun updateReminderTime(time: LocalTime?) {
        updateSettings(rescheduleReminder = true) { it.copy(reminderTime = time) }
    }

    fun updateSmartSnooze(enabled: Boolean) {
        updateSettings(rescheduleReminder = true) { it.copy(smartSnoozeEnabled = enabled) }
    }

    fun updateAutoLock(enabled: Boolean) {
        updateSettings { it.copy(autoLockOnBackground = enabled) }
    }

    fun updateBeforeFourPreference(enabled: Boolean) {
        updateSettings { it.copy(beforeFourAmIsYesterday = enabled) }
    }

    fun updateNotificationsDenied(denied: Boolean) {
        updateSettings(rescheduleReminder = true) { it.copy(notificationsDenied = denied) }
    }

    fun clearPasscode() {
        updateSettings { it.copy(passcodeHash = null, biometricsEnabled = false) }
    }

    fun setPasscode(hash: String, biometricsEnabled: Boolean) {
        updateSettings { it.copy(passcodeHash = hash, biometricsEnabled = biometricsEnabled) }
    }

    private fun updateSettings(rescheduleReminder: Boolean = false, block: (Settings) -> Settings) {
        viewModelScope.launch {
            settingsRepository.update(block)
            if (rescheduleReminder) {
                reminderManager.refreshSchedule()
            }
        }
    }
}


