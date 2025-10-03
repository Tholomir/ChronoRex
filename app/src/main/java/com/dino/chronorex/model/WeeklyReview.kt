package com.dino.chronorex.model

import java.time.Instant
import java.time.LocalDate
import java.util.UUID

data class WeeklyReview(
    val id: UUID = UUID.randomUUID(),
    val startDate: LocalDate,
    val endDate: LocalDate,
    val generatedAt: Instant,
    val trendHighlights: List<String>,
    val correlationHighlights: List<String>,
    val bestDay: LocalDate?,
    val toughestDay: LocalDate?,
    val adherenceSummary: String,
    val needsInAppNudge: Boolean
)
