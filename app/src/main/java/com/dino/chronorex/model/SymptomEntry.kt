package com.dino.chronorex.model

import java.time.Instant
import java.time.LocalDate
import java.util.UUID

data class SymptomEntry(
    val id: UUID,
    val date: LocalDate,
    val time: Instant,
    val name: String,
    val severity1To10: Int,
    val note: String?
)
