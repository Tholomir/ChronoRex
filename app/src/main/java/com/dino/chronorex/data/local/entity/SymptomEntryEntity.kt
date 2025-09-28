package com.dino.chronorex.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.Instant
import java.time.LocalDate

@Entity(
    tableName = "symptom_entry",
    indices = [Index(value = ["date"], unique = false)]
)
data class SymptomEntryEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String,
    @ColumnInfo(name = "date")
    val date: LocalDate,
    @ColumnInfo(name = "time")
    val time: Instant,
    @ColumnInfo(name = "name")
    val name: String,
    @ColumnInfo(name = "severity_1_10")
    val severity1To10: Int,
    @ColumnInfo(name = "note")
    val note: String?
)
