package com.dino.chronorex.notification

import com.dino.chronorex.data.repository.SettingsRepository
import com.dino.chronorex.model.Settings
import java.time.Clock
import java.time.Instant
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ReminderManager(
    private val settingsRepository: SettingsRepository,
    private val scheduler: ReminderScheduler,
    private val clock: Clock = Clock.systemDefaultZone()
) {

    suspend fun refreshSchedule() = withContext(Dispatchers.IO) {
        val settings = settingsRepository.loadSettings()
        applySchedule(settings)
    }

    suspend fun snoozeForNextDay() = withContext(Dispatchers.IO) {
        val settings = settingsRepository.loadSettings()
        if (!settings.smartSnoozeEnabled || settings.reminderTime == null) return@withContext
        val zone = ZoneId.systemDefault()
        val nextRegular = nextReminderDateTime(settings.reminderTime, zone)
        val snoozed = nextRegular.plusDays(1)
        settingsRepository.update { current ->
            current.copy(snoozedUntil = snoozed.toInstant())
        }
        scheduler.schedule(snoozed.toInstant().toEpochMilli())
    }

    suspend fun onReminderTriggered() = withContext(Dispatchers.IO) {
        settingsRepository.update { it.copy(snoozedUntil = null) }
        refreshSchedule()
    }

    private suspend fun applySchedule(settings: Settings) {
        val reminderTime = settings.reminderTime
        if (reminderTime == null || settings.notificationsDenied) {
            scheduler.cancel()
            return
        }
        val zone = ZoneId.systemDefault()
        val now = ZonedDateTime.now(clock).withSecond(0).withNano(0)
        var scheduled = nextReminderDateTime(reminderTime, zone)
        var shouldClearSnooze = false
        settings.snoozedUntil?.let { stored ->
            val snoozedAt = ZonedDateTime.ofInstant(stored, zone)
            if (snoozedAt.isAfter(now)) {
                if (snoozedAt.isAfter(scheduled)) {
                    scheduled = snoozedAt
                }
            } else {
                shouldClearSnooze = true
            }
        }
        scheduler.schedule(scheduled.toInstant().toEpochMilli())
        if (shouldClearSnooze) {
            settingsRepository.update { it.copy(snoozedUntil = null) }
        }
    }

    private fun nextReminderDateTime(reminderTime: LocalTime, zone: ZoneId): ZonedDateTime {
        val now = ZonedDateTime.now(clock).withSecond(0).withNano(0)
        var scheduled = now.withHour(reminderTime.hour).withMinute(reminderTime.minute)
        if (!scheduled.isAfter(now)) {
            scheduled = scheduled.plusDays(1)
        }
        return scheduled
    }
}
