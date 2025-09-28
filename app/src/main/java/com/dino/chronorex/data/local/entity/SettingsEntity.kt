package com.dino.chronorex.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.Instant
import java.time.LocalTime

@Entity(tableName = "settings")
data class SettingsEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: Int = SINGLETON_ID,
    @ColumnInfo(name = "reminder_time")
    val reminderTime: LocalTime?,
    @ColumnInfo(name = "passcode_hash")
    val passcodeHash: String?,
    @ColumnInfo(name = "biometrics_enabled")
    val biometricsEnabled: Boolean,
    @ColumnInfo(name = "theme")
    val theme: String,
    @ColumnInfo(name = "smart_snooze_enabled")
    val smartSnoozeEnabled: Boolean,
    @ColumnInfo(name = "auto_lock_on_background")
    val autoLockOnBackground: Boolean,
    @ColumnInfo(name = "before_four_am_is_yesterday")
    val beforeFourAmIsYesterday: Boolean,
    @ColumnInfo(name = "notifications_denied")
    val notificationsDenied: Boolean,
    @ColumnInfo(name = "onboarding_completed")
    val onboardingCompleted: Boolean,
    @ColumnInfo(name = "snoozed_until")
    val snoozedUntil: Instant?
) {
    companion object {
        const val SINGLETON_ID: Int = 0
    }
}
