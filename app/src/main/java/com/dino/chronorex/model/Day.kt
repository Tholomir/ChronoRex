package com.dino.chronorex.model

import java.time.LocalDate

data class Day(
    val date: LocalDate,
    val timezoneOffsetMinutes: Int,
    val restedness0To100: Int,
    val sleepQuality1To5: Int,
    val notes: String?,
    val emojiTags: List<String>,
    val illness: Boolean,
    val travel: Boolean
) {
    val fatigueToday0To100: Int get() = 100 - restedness0To100
}
