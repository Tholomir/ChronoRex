package com.dino.chronorex.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.LocalDate

@Entity(
    tableName = "day",
    indices = [Index(value = ["date"], unique = true)]
)
data class DayEntity(
    @PrimaryKey
    @ColumnInfo(name = "date")
    val date: LocalDate,
    @ColumnInfo(name = "timezone_offset_minutes")
    val timezoneOffsetMinutes: Int,
    @ColumnInfo(name = "restedness_0_100")
    val restedness0To100: Int,
    @ColumnInfo(name = "sleep_quality_1_5")
    val sleepQuality1To5: Int,
    @ColumnInfo(name = "notes")
    val notes: String?,
    @ColumnInfo(name = "emoji_tags")
    val emojiTags: List<String>,
    @ColumnInfo(name = "illness")
    val illness: Boolean,
    @ColumnInfo(name = "travel")
    val travel: Boolean
)
