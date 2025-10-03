package com.dino.chronorex.analytics

import com.dino.chronorex.model.ActivityEntry
import com.dino.chronorex.model.Day
import com.dino.chronorex.model.SymptomEntry
import com.dino.chronorex.model.WeeklyReview
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import kotlin.math.abs

object WeeklyReviewGenerator {
    fun generate(
        days: List<Day>,
        symptoms: List<SymptomEntry>,
        activities: List<ActivityEntry>,
        notificationsDenied: Boolean,
        clock: Clock = Clock.systemDefaultZone()
    ): WeeklyReview? {
        if (days.isEmpty()) return null
        val metrics = InsightsCalculator.dailyMetrics(days, symptoms, activities)
        if (metrics.size < 7) return null
        val endDate = metrics.maxOf { it.date }
        val startDate = endDate.minusDays(6)
        val periodMetrics = metrics.filter { it.date in startDate..endDate }
        if (periodMetrics.isEmpty()) return null

        val previousStart = startDate.minusDays(7)
        val previousEnd = startDate.minusDays(1)
        val previousMetrics = metrics.filter { it.date in previousStart..previousEnd }

        val avgFatigueCurrent = periodMetrics.map { it.fatigue }.average()
        val avgFatiguePrevious = previousMetrics.takeIf { it.isNotEmpty() }?.map { it.fatigue }?.average()
        val fatigueDelta = avgFatiguePrevious?.let { avgFatigueCurrent - it }

        val trendHighlights = buildList {
            val avgText = "Average fatigue was ${avgFatigueCurrent.format(1)}"
            val deltaText = fatigueDelta?.let { delta ->
                val direction = if (delta > 0) "higher" else "lower"
                " (${abs(delta).format(1)} points $direction than last week)"
            } ?: ""
            add(avgText + deltaText)

            val bestDay = periodMetrics.minByOrNull { it.fatigue }
            bestDay?.let {
                add("Most rested day: ${it.date} (restedness ${(100 - it.fatigue).format(0)})")
            }

            val illnessCount = days.count { it.date in startDate..endDate && it.illness }
            val travelCount = days.count { it.date in startDate..endDate && it.travel }
            if (illnessCount > 0 || travelCount > 0) {
                val parts = buildList {
                    if (illnessCount > 0) add("illness on $illnessCount day${if (illnessCount == 1) "" else "s"}")
                    if (travelCount > 0) add("travel on $travelCount day${if (travelCount == 1) "" else "s"}")
                }
                add(parts.joinToString(prefix = "Flags: "))
            } else {
                add("No illness or travel flags this week")
            }
        }

        val correlations = when (val result = InsightsCalculator.calculate(days, symptoms, activities)) {
            is InsightsResult.Ready -> result.correlations
            InsightsResult.Empty -> emptyList()
        }
        val correlationHighlights = correlations
            .sortedByDescending { abs(it.coefficient) }
            .take(3)
            .map { it.narrative }

        val bestDayDate = periodMetrics.minByOrNull { it.fatigue }?.date
        val toughestDayDate = periodMetrics.maxByOrNull { it.fatigue }?.date

        val adherenceSummary = buildAdherenceSummary(startDate, endDate, days, symptoms, activities)

        return WeeklyReview(
            startDate = startDate,
            endDate = endDate,
            generatedAt = Instant.now(clock),
            trendHighlights = trendHighlights,
            correlationHighlights = correlationHighlights,
            bestDay = bestDayDate,
            toughestDay = toughestDayDate,
            adherenceSummary = adherenceSummary,
            needsInAppNudge = notificationsDenied
        )
    }

    private fun buildAdherenceSummary(
        start: LocalDate,
        end: LocalDate,
        days: List<Day>,
        symptoms: List<SymptomEntry>,
        activities: List<ActivityEntry>
    ): String {
        val checkIns = days.count { it.date in start..end }
        val symptomDays = symptoms.groupBy { it.date }.keys.count { it in start..end }
        val activityDays = activities.groupBy { it.date }.keys.count { it in start..end }
        return "Logged $checkIns/7 check-ins, symptoms on $symptomDays day${if (symptomDays == 1) "" else "s"}, activities on $activityDays day${if (activityDays == 1) "" else "s"}."
    }

    private fun Double.format(decimals: Int): String = String.format("%.${decimals}f", this)
}
