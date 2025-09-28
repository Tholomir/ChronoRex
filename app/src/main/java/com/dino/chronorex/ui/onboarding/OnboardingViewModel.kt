package com.dino.chronorex.ui.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dino.chronorex.data.repository.SettingsRepository
import com.dino.chronorex.model.Settings
import com.dino.chronorex.notification.ReminderManager
import java.security.MessageDigest
import java.time.LocalTime
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class OnboardingStep {
    Privacy,
    MascotIntro,
    Reminder,
    AppLock,
    Ready
}

data class OnboardingUiState(
    val step: OnboardingStep = OnboardingStep.Privacy,
    val reminderTime: LocalTime = LocalTime.of(8, 0),
    val smartSnoozeEnabled: Boolean = true,
    val passcodeRequested: Boolean = false,
    val biometricsRequested: Boolean = false,
    val autoLockOnBackground: Boolean = false,
    val notificationsDenied: Boolean = false,
    val passcodeInput: String = "",
    val passcodeConfirm: String = "",
    val passcodeError: String? = null,
    val isSaving: Boolean = false,
    val completed: Boolean = false,
    val settingsPreview: Settings = Settings.default()
)

class OnboardingViewModel(
    private val settingsRepository: SettingsRepository,
    private val reminderManager: ReminderManager
) : ViewModel() {

    private val _state = MutableStateFlow(OnboardingUiState())
    val state: StateFlow<OnboardingUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            val existing = settingsRepository.loadSettings()
            _state.update { current ->
                current.copy(
                    reminderTime = existing.reminderTime ?: LocalTime.of(8, 0),
                    smartSnoozeEnabled = existing.smartSnoozeEnabled,
                    autoLockOnBackground = existing.autoLockOnBackground,
                    biometricsRequested = existing.biometricsEnabled,
                    notificationsDenied = existing.notificationsDenied,
                    settingsPreview = existing
                )
            }
        }
    }

    fun goToNextStep() {
        _state.update { current ->
            val nextStep = when (current.step) {
                OnboardingStep.Privacy -> OnboardingStep.MascotIntro
                OnboardingStep.MascotIntro -> OnboardingStep.Reminder
                OnboardingStep.Reminder -> OnboardingStep.AppLock
                OnboardingStep.AppLock -> OnboardingStep.Ready
                OnboardingStep.Ready -> OnboardingStep.Ready
            }
            current.copy(step = nextStep, passcodeError = null)
        }
    }

    fun goToPreviousStep() {
        _state.update { current ->
            val previousStep = when (current.step) {
                OnboardingStep.Privacy -> OnboardingStep.Privacy
                OnboardingStep.MascotIntro -> OnboardingStep.Privacy
                OnboardingStep.Reminder -> OnboardingStep.MascotIntro
                OnboardingStep.AppLock -> OnboardingStep.Reminder
                OnboardingStep.Ready -> OnboardingStep.AppLock
            }
            current.copy(step = previousStep, passcodeError = null)
        }
    }

    fun updateReminderTime(time: LocalTime) {
        _state.update { it.copy(reminderTime = time) }
    }

    fun updateSmartSnooze(enabled: Boolean) {
        _state.update { it.copy(smartSnoozeEnabled = enabled) }
    }

    fun updatePasscodeRequested(requested: Boolean) {
        _state.update {
            if (!requested) it.copy(
                passcodeRequested = false,
                biometricsRequested = false,
                passcodeInput = "",
                passcodeConfirm = "",
                passcodeError = null
            ) else it.copy(passcodeRequested = true)
        }
    }

    fun updateBiometricsRequested(requested: Boolean) {
        _state.update {
            if (!it.passcodeRequested) it.copy(biometricsRequested = false) else it.copy(biometricsRequested = requested)
        }
    }

    fun updateAutoLockOnBackground(enabled: Boolean) {
        _state.update { it.copy(autoLockOnBackground = enabled) }
    }

    fun updatePasscodeInput(value: String) {
        _state.update { it.copy(passcodeInput = value.take(MAX_PASSCODE_LENGTH), passcodeError = null) }
    }

    fun updatePasscodeConfirm(value: String) {
        _state.update { it.copy(passcodeConfirm = value.take(MAX_PASSCODE_LENGTH), passcodeError = null) }
    }

    fun setNotificationsDenied(denied: Boolean) {
        _state.update { it.copy(notificationsDenied = denied) }
    }

    fun completeOnboarding(onCompleted: () -> Unit) {
        val snapshot = _state.value
        if (snapshot.completed || snapshot.isSaving) return
        if (snapshot.passcodeRequested && snapshot.passcodeInput.length < MIN_PASSCODE_LENGTH) {
            _state.update { it.copy(passcodeError = "Passcode must be at least $MIN_PASSCODE_LENGTH digits") }
            return
        }
        if (snapshot.passcodeRequested && snapshot.passcodeInput != snapshot.passcodeConfirm) {
            _state.update { it.copy(passcodeError = "Passcodes do not match") }
            return
        }
        viewModelScope.launch {
            _state.update { it.copy(isSaving = true) }
            val passcodeHash = if (snapshot.passcodeRequested) snapshot.passcodeInput.toSha256() else null
            settingsRepository.update { current ->
                current.copy(
                    reminderTime = snapshot.reminderTime,
                    smartSnoozeEnabled = snapshot.smartSnoozeEnabled,
                    autoLockOnBackground = snapshot.autoLockOnBackground,
                    passcodeHash = passcodeHash,
                    biometricsEnabled = snapshot.passcodeRequested && snapshot.biometricsRequested,
                    notificationsDenied = snapshot.notificationsDenied,
                    onboardingCompleted = true
                )
            }
            reminderManager.refreshSchedule()
            _state.update { it.copy(isSaving = false, completed = true) }
            onCompleted()
        }
    }

    private fun String.toSha256(): String {
        val digest = MessageDigest.getInstance("SHA-256")
        return digest.digest(toByteArray()).joinToString(separator = "") { byte ->
            ((byte.toInt() and 0xFF) + 0x100).toString(16).substring(1)
        }
    }

    companion object {
        private const val MAX_PASSCODE_LENGTH = 8
        private const val MIN_PASSCODE_LENGTH = 4
    }
}




