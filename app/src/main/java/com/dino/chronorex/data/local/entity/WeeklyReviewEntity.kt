package com.dino.chronorex.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.Instant
import java.time.LocalDate

@Entity(tableName = "weekly_review")
data class WeeklyReviewEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String,
    @ColumnInfo(name = "start_date")
    val startDate: LocalDate,
    @ColumnInfo(name = "end_date")
    val endDate: LocalDate,
    @ColumnInfo(name = "generated_at")
    val generatedAt: Instant,
    @ColumnInfo(name = "trend_highlights")
    val trendHighlights: List<String>,
    @ColumnInfo(name = "correlation_highlights")
    val correlationHighlights: List<String>,
    @ColumnInfo(name = "best_day")
    val bestDay: LocalDate?,
    @ColumnInfo(name = "toughest_day")
    val toughestDay: LocalDate?,
    @ColumnInfo(name = "adherence_summary")
    val adherenceSummary: String,
    @ColumnInfo(name = "needs_in_app_nudge")
    val needsInAppNudge: Boolean
)
