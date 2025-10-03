package com.dino.chronorex.ui.lock

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dino.chronorex.data.repository.SettingsRepository
import java.security.MessageDigest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

data class AppLockUiState(
    val isLocked: Boolean = false,
    val passcodeRequired: Boolean = false,
    val biometricsEnabled: Boolean = false,
    val errorMessage: String? = null
)

class LockViewModel(
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _state = MutableStateFlow(AppLockUiState())
    val state: StateFlow<AppLockUiState> = _state.asStateFlow()

    private var passcodeHash: String? = null
    private var autoLockOnBackground: Boolean = false
    private var initialized = false

    init {
        viewModelScope.launch {
            settingsRepository.observeSettings().collectLatest { settings ->
                val previousHash = passcodeHash
                passcodeHash = settings.passcodeHash
                autoLockOnBackground = settings.autoLockOnBackground
                val requiresPasscode = !settings.passcodeHash.isNullOrBlank()
                var isLocked = _state.value.isLocked
                if (!requiresPasscode) {
                    isLocked = false
                } else if (!initialized || previousHash == null && settings.passcodeHash != null) {
                    isLocked = true
                }
                _state.value = AppLockUiState(
                    isLocked = isLocked,
                    passcodeRequired = requiresPasscode,
                    biometricsEnabled = settings.biometricsEnabled,
                    errorMessage = null
                )
                initialized = true
            }
        }
    }

    fun handleAppBackgrounded() {
        if (passcodeHash != null && autoLockOnBackground) {
            _state.value = _state.value.copy(isLocked = true, errorMessage = null)
        }
    }

    fun submitPasscode(input: String) {
        val expected = passcodeHash ?: return
        val actual = input.toSha256()
        if (actual == expected) {
            _state.value = _state.value.copy(isLocked = false, errorMessage = null)
        } else {
            _state.value = _state.value.copy(errorMessage = "Incorrect passcode")
        }
    }

    fun unlockWithBiometrics() {
        _state.value = _state.value.copy(isLocked = false, errorMessage = null)
    }

    fun reportBiometricFailure(message: String?) {
        _state.value = _state.value.copy(errorMessage = message ?: "Biometric unlock failed")
    }

    fun clearError() {
        if (_state.value.errorMessage != null) {
            _state.value = _state.value.copy(errorMessage = null)
        }
    }

    private fun String.toSha256(): String {
        val digest = MessageDigest.getInstance("SHA-256")
        return digest.digest(toByteArray()).joinToString(separator = "") { byte ->
            ((byte.toInt() and 0xFF) + 0x100).toString(16).substring(1)
        }
    }
}
