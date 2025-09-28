package com.dino.chronorex.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.Instant
import java.time.LocalDate

@Entity(
    tableName = "activity_entry",
    indices = [Index(value = ["date"], unique = false)]
)
data class ActivityEntryEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String,
    @ColumnInfo(name = "date")
    val date: LocalDate,
    @ColumnInfo(name = "time")
    val time: Instant,
    @ColumnInfo(name = "type")
    val type: String,
    @ColumnInfo(name = "duration_minutes")
    val durationMinutes: Int?,
    @ColumnInfo(name = "perceived_exhaustion_1_10")
    val perceivedExhaustion1To10: Int,
    @ColumnInfo(name = "note")
    val note: String?
)
