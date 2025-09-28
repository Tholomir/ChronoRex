package com.dino.chronorex.data.local.converter

import androidx.room.TypeConverter
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime

private const val LIST_DELIMITER = "|"

class ChronoRexConverters {
    @TypeConverter
    fun fromLocalDate(value: LocalDate?): String? = value?.toString()

    @TypeConverter
    fun toLocalDate(value: String?): LocalDate? = value?.let(LocalDate::parse)

    @TypeConverter
    fun fromInstant(value: Instant?): String? = value?.toString()

    @TypeConverter
    fun toInstant(value: String?): Instant? = value?.let(Instant::parse)

    @TypeConverter
    fun fromLocalTime(value: LocalTime?): String? = value?.toString()

    @TypeConverter
    fun toLocalTime(value: String?): LocalTime? = value?.let(LocalTime::parse)

    @TypeConverter
    fun fromStringList(value: List<String>?): String? = value?.takeIf { it.isNotEmpty() }?.joinToString(separator = LIST_DELIMITER)

    @TypeConverter
    fun toStringList(value: String?): List<String> = value?.takeIf { it.isNotBlank() }?.split(LIST_DELIMITER) ?: emptyList()
}
