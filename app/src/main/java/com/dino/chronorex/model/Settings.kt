package com.dino.chronorex.model

import java.time.Instant
import java.time.LocalTime

enum class AppTheme { LIGHT }

data class Settings(
    val reminderTime: LocalTime?,
    val passcodeHash: String?,
    val biometricsEnabled: Boolean,
    val theme: AppTheme,
    val smartSnoozeEnabled: Boolean,
    val autoLockOnBackground: Boolean,
    val beforeFourAmIsYesterday: Boolean,
    val notificationsDenied: Boolean,
    val onboardingCompleted: Boolean,
    val snoozedUntil: Instant?
) {
    companion object {
        fun default(): Settings = Settings(
            reminderTime = null,
            passcodeHash = null,
            biometricsEnabled = false,
            theme = AppTheme.LIGHT,
            smartSnoozeEnabled = true,
            autoLockOnBackground = false,
            beforeFourAmIsYesterday = false,
            notificationsDenied = false,
            onboardingCompleted = false,
            snoozedUntil = null
        )
    }
}
