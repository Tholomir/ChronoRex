package com.dino.chronorex.ui.lock

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import com.dino.chronorex.ui.components.ChronoRexCard
import com.dino.chronorex.ui.components.ChronoRexPrimaryButton
import com.dino.chronorex.ui.theme.spacing

@Composable
fun LockOverlay(
    state: AppLockUiState,
    onSubmitPasscode: (String) -> Unit,
    onClearError: () -> Unit,
    onBiometricRequested: (() -> Unit)?
) {
    val passcode = remember { mutableStateOf("") }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background.copy(alpha = 0.94f))
            .padding(MaterialTheme.spacing.lg),
        verticalArrangement = Arrangement.Center
    ) {
        ChronoRexCard {
            Text(
                text = "Unlock ChronoRex",
                style = MaterialTheme.typography.titleLarge
            )
            Text(
                text = "Enter your passcode to continue.",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = MaterialTheme.spacing.sm)
            )
            OutlinedTextField(
                value = passcode.value,
                onValueChange = {
                    if (state.errorMessage != null) onClearError()
                    passcode.value = it.take(8)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = MaterialTheme.spacing.sm),
                visualTransformation = PasswordVisualTransformation(),
                singleLine = true,
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                )
            )
            state.errorMessage?.let { error ->
                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = MaterialTheme.spacing.xs)
                )
            }
            ChronoRexPrimaryButton(
                text = "Unlock",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = MaterialTheme.spacing.sm),
                onClick = {
                    onSubmitPasscode(passcode.value)
                }
            )
            if (state.biometricsEnabled && onBiometricRequested != null) {
                ChronoRexPrimaryButton(
                    text = "Use biometrics",
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = MaterialTheme.spacing.xs),
                    onClick = onBiometricRequested
                )
            }
        }
    }
}
