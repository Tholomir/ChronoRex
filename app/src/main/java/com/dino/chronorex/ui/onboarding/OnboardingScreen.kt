package com.dino.chronorex.ui.onboarding

import android.app.TimePickerDialog
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import com.dino.chronorex.ui.components.ChronoRexCard
import com.dino.chronorex.ui.components.ChronoRexPrimaryButton
import com.dino.chronorex.ui.components.ToggleRow
import com.dino.chronorex.ui.theme.spacing
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@Composable
fun OnboardingScreen(
    state: OnboardingUiState,
    canRequestNotifications: Boolean,
    onSetReminderToNow: () -> Unit,
    onUpdateReminderTime: (LocalTime) -> Unit,
    onToggleSmartSnooze: (Boolean) -> Unit,
    onTogglePasscode: (Boolean) -> Unit,
    onToggleBiometrics: (Boolean) -> Unit,
    onToggleAutoLock: (Boolean) -> Unit,
    onRequestNotifications: () -> Unit,
    onUpdatePasscodeValue: (String) -> Unit,
    onUpdatePasscodeConfirm: (String) -> Unit,
    onBack: () -> Unit,
    onNext: () -> Unit,
    onComplete: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(MaterialTheme.spacing.lg),
        verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.lg)
    ) {
        Text(
            text = "Step ${state.step.ordinal + 1} of ${OnboardingStep.values().size}",
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        when (state.step) {
            OnboardingStep.Privacy -> PrivacyStepContent()
            OnboardingStep.MascotIntro -> MascotStepContent()
            OnboardingStep.Reminder -> ReminderStepContent(
                state = state,
                canRequestNotifications = canRequestNotifications,
                onSetReminderToNow = onSetReminderToNow,
                onUpdateReminderTime = onUpdateReminderTime,
                onToggleSmartSnooze = onToggleSmartSnooze,
                onRequestNotifications = onRequestNotifications
            )
            OnboardingStep.AppLock -> PasscodeStepContent(
                state = state,
                onTogglePasscode = onTogglePasscode,
                onToggleBiometrics = onToggleBiometrics,
                onToggleAutoLock = onToggleAutoLock,
                onUpdatePasscodeValue = onUpdatePasscodeValue,
                onUpdatePasscodeConfirm = onUpdatePasscodeConfirm
            )
            OnboardingStep.Ready -> ReadyStepContent(state)
        }
        Row(
            horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.sm),
            modifier = Modifier.fillMaxWidth()
        ) {
            ChronoRexPrimaryButton(
                text = "Back",
                modifier = Modifier.fillMaxWidth(0.45f),
                onClick = onBack,
                enabled = state.step != OnboardingStep.Privacy
            )
            val primaryLabel = if (state.step == OnboardingStep.Ready) "Finish" else "Next"
            val action = if (state.step == OnboardingStep.Ready) onComplete else onNext
            ChronoRexPrimaryButton(
                text = primaryLabel,
                modifier = Modifier.fillMaxWidth(0.45f),
                onClick = action,
                enabled = !state.isSaving
            )
        }
        state.passcodeError?.let { error ->
            Text(
                text = error,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error
            )
        }
    }
}

@Composable
private fun PrivacyStepContent() {
    ChronoRexCard {
        Text("Your data stays on this device", style = MaterialTheme.typography.titleMedium)
        Text(
            text = "ChronoRex never syncs to the cloud. You choose if and when to export to clinicians.",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(top = MaterialTheme.spacing.sm)
        )
        Text(
            text = "You can wipe entries or adjust reminders anytime in Settings.",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(top = MaterialTheme.spacing.sm)
        )
    }
}

@Composable
private fun MascotStepContent() {
    ChronoRexCard {
        Text("Meet ChronoRex", style = MaterialTheme.typography.titleMedium)
        Text(
            text = "A friendly dino keeps the tone light while highlighting helpful pacing patterns.",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(top = MaterialTheme.spacing.sm)
        )
    }
}

@Composable
private fun ReminderStepContent(
    state: OnboardingUiState,
    canRequestNotifications: Boolean,
    onSetReminderToNow: () -> Unit,
    onUpdateReminderTime: (LocalTime) -> Unit,
    onToggleSmartSnooze: (Boolean) -> Unit,
    onRequestNotifications: () -> Unit
) {
    val context = LocalContext.current
    val reminderLabel = state.reminderTime.format(DateTimeFormatter.ofPattern("HH:mm"))
    ChronoRexCard {
        Text("Pick a reminder", style = MaterialTheme.typography.titleMedium)
        Text(
            text = "Morning reminder: $reminderLabel",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(top = MaterialTheme.spacing.sm)
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.sm),
            modifier = Modifier.padding(top = MaterialTheme.spacing.sm)
        ) {
            ChronoRexPrimaryButton(
                text = "Choose time",
                modifier = Modifier.fillMaxWidth(0.45f),
                onClick = {
                    val base = state.reminderTime
                    TimePickerDialog(
                        context,
                        { _, hour, minute -> onUpdateReminderTime(LocalTime.of(hour, minute)) },
                        base.hour,
                        base.minute,
                        true
                    ).show()
                }
            )
            ChronoRexPrimaryButton(
                text = "Use now",
                modifier = Modifier.fillMaxWidth(0.45f),
                onClick = onSetReminderToNow
            )
        }
        Spacer(modifier = Modifier.height(MaterialTheme.spacing.sm))
        ToggleRow(
            label = "Smart snooze (pause reminders when you mark illness or travel)",
            checked = state.smartSnoozeEnabled,
            onCheckedChange = onToggleSmartSnooze
        )
        if (canRequestNotifications) {
            ChronoRexPrimaryButton(
                text = if (state.notificationsDenied) "Open notification settings" else "Allow notifications",
                modifier = Modifier.padding(top = MaterialTheme.spacing.sm),
                onClick = onRequestNotifications
            )
            if (state.notificationsDenied) {
                Text(
                    text = "Notifications are off. You can enable them later in Settings.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(top = MaterialTheme.spacing.xs)
                )
            }
        }
    }
}

@Composable
private fun PasscodeStepContent(
    state: OnboardingUiState,
    onTogglePasscode: (Boolean) -> Unit,
    onToggleBiometrics: (Boolean) -> Unit,
    onToggleAutoLock: (Boolean) -> Unit,
    onUpdatePasscodeValue: (String) -> Unit,
    onUpdatePasscodeConfirm: (String) -> Unit
) {
    ChronoRexCard {
        Text("Protect the app", style = MaterialTheme.typography.titleMedium)
        ToggleRow(
            label = "Require passcode",
            checked = state.passcodeRequested,
            onCheckedChange = onTogglePasscode
        )
        if (state.passcodeRequested) {
            Spacer(modifier = Modifier.height(MaterialTheme.spacing.sm))
            OutlinedTextField(
                value = state.passcodeInput,
                onValueChange = onUpdatePasscodeValue,
                label = { Text("Passcode") },
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = PasswordVisualTransformation()
            )
            Spacer(modifier = Modifier.height(MaterialTheme.spacing.sm))
            OutlinedTextField(
                value = state.passcodeConfirm,
                onValueChange = onUpdatePasscodeConfirm,
                label = { Text("Confirm passcode") },
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = PasswordVisualTransformation()
            )
            Spacer(modifier = Modifier.height(MaterialTheme.spacing.sm))
            ToggleRow(
                label = "Allow biometric unlock",
                checked = state.biometricsRequested,
                onCheckedChange = onToggleBiometrics
            )
        }
        ToggleRow(
            label = "Auto-lock on background",
            checked = state.autoLockOnBackground,
            onCheckedChange = onToggleAutoLock
        )
        if (!state.passcodeRequested) {
            Text(
                text = "Enable a passcode to unlock biometric and auto-lock options.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = MaterialTheme.spacing.sm)
            )
        }
    }
}

@Composable
private fun ReadyStepContent(state: OnboardingUiState) {
    ChronoRexCard {
        Text("All set!", style = MaterialTheme.typography.titleMedium)
        Text(
            text = "Reminder: ${state.reminderTime.format(DateTimeFormatter.ofPattern("HH:mm"))}",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(top = MaterialTheme.spacing.sm)
        )
        Text("Smart snooze: ${if (state.smartSnoozeEnabled) "On" else "Off"}", style = MaterialTheme.typography.bodyMedium)
        Text("Passcode: ${if (state.passcodeRequested) "Enabled" else "Off"}", style = MaterialTheme.typography.bodyMedium)
        Text("Biometrics: ${if (state.passcodeRequested && state.biometricsRequested) "Enabled" else "Off"}", style = MaterialTheme.typography.bodyMedium)
        Text("Auto-lock: ${if (state.autoLockOnBackground) "On" else "Off"}", style = MaterialTheme.typography.bodyMedium)
    }
}
