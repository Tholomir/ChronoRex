
package com.dino.chronorex.data.mapper

import com.dino.chronorex.data.local.entity.ActivityEntryEntity
import com.dino.chronorex.data.local.entity.DayEntity
import com.dino.chronorex.data.local.entity.SettingsEntity
import com.dino.chronorex.data.local.entity.SymptomEntryEntity
import com.dino.chronorex.data.local.entity.WeeklyReviewEntity
import com.dino.chronorex.model.ActivityEntry
import com.dino.chronorex.model.AppTheme
import com.dino.chronorex.model.Day
import com.dino.chronorex.model.Settings
import com.dino.chronorex.model.SymptomEntry
import com.dino.chronorex.model.WeeklyReview
import java.util.UUID

fun DayEntity.toModel(): Day = Day(
    date = date,
    timezoneOffsetMinutes = timezoneOffsetMinutes,
    restedness0To100 = restedness0To100,
    sleepQuality1To5 = sleepQuality1To5,
    notes = notes,
    emojiTags = emojiTags,
    illness = illness,
    travel = travel
)

fun Day.toEntity(): DayEntity = DayEntity(
    date = date,
    timezoneOffsetMinutes = timezoneOffsetMinutes,
    restedness0To100 = restedness0To100,
    sleepQuality1To5 = sleepQuality1To5,
    notes = notes,
    emojiTags = emojiTags,
    illness = illness,
    travel = travel
)

fun SymptomEntryEntity.toModel(): SymptomEntry = SymptomEntry(
    id = UUID.fromString(id),
    date = date,
    time = time,
    name = name,
    severity1To10 = severity1To10,
    note = note
)

fun SymptomEntry.toEntity(): SymptomEntryEntity = SymptomEntryEntity(
    id = id.toString(),
    date = date,
    time = time,
    name = name,
    severity1To10 = severity1To10,
    note = note
)

fun ActivityEntryEntity.toModel(): ActivityEntry = ActivityEntry(
    id = UUID.fromString(id),
    date = date,
    time = time,
    type = type,
    durationMinutes = durationMinutes,
    perceivedExhaustion1To10 = perceivedExhaustion1To10,
    note = note
)

fun ActivityEntry.toEntity(): ActivityEntryEntity = ActivityEntryEntity(
    id = id.toString(),
    date = date,
    time = time,
    type = type,
    durationMinutes = durationMinutes,
    perceivedExhaustion1To10 = perceivedExhaustion1To10,
    note = note
)

fun SettingsEntity.toModel(): Settings = Settings(
    reminderTime = reminderTime,
    passcodeHash = passcodeHash,
    biometricsEnabled = biometricsEnabled,
    theme = runCatching { AppTheme.valueOf(theme) }.getOrDefault(AppTheme.LIGHT),
    smartSnoozeEnabled = smartSnoozeEnabled,
    autoLockOnBackground = autoLockOnBackground,
    beforeFourAmIsYesterday = beforeFourAmIsYesterday,
    notificationsDenied = notificationsDenied,
    onboardingCompleted = onboardingCompleted,
    snoozedUntil = snoozedUntil
)

fun Settings.toEntity(): SettingsEntity = SettingsEntity(
    reminderTime = reminderTime,
    passcodeHash = passcodeHash,
    biometricsEnabled = biometricsEnabled,
    theme = theme.name,
    smartSnoozeEnabled = smartSnoozeEnabled,
    autoLockOnBackground = autoLockOnBackground,
    beforeFourAmIsYesterday = beforeFourAmIsYesterday,
    notificationsDenied = notificationsDenied,
    onboardingCompleted = onboardingCompleted,
    snoozedUntil = snoozedUntil
)

fun WeeklyReviewEntity.toModel(): WeeklyReview = WeeklyReview(
    id = UUID.fromString(id),
    startDate = startDate,
    endDate = endDate,
    generatedAt = generatedAt,
    trendHighlights = trendHighlights,
    correlationHighlights = correlationHighlights,
    bestDay = bestDay,
    toughestDay = toughestDay,
    adherenceSummary = adherenceSummary,
    needsInAppNudge = needsInAppNudge
)

fun WeeklyReview.toEntity(): WeeklyReviewEntity = WeeklyReviewEntity(
    id = id.toString(),
    startDate = startDate,
    endDate = endDate,
    generatedAt = generatedAt,
    trendHighlights = trendHighlights,
    correlationHighlights = correlationHighlights,
    bestDay = bestDay,
    toughestDay = toughestDay,
    adherenceSummary = adherenceSummary,
    needsInAppNudge = needsInAppNudge
)
