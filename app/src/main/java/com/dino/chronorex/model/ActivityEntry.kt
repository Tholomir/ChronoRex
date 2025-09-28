package com.dino.chronorex.model

import java.time.Instant
import java.time.LocalDate
import java.util.UUID

data class ActivityEntry(
    val id: UUID,
    val date: LocalDate,
    val time: Instant,
    val type: String,
    val durationMinutes: Int?,
    val perceivedExhaustion1To10: Int,
    val note: String?
)
