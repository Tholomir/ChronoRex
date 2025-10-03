package com.dino.chronorex.analytics

import com.dino.chronorex.model.ActivityEntry
import com.dino.chronorex.model.Day
import com.dino.chronorex.model.SymptomEntry
import java.time.LocalDate
import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.sqrt

object InsightsCalculator {
    fun calculate(
        days: List<Day>,
        symptoms: List<SymptomEntry>,
        activities: List<ActivityEntry>
    ): InsightsResult {
        if (days.isEmpty()) {
            return InsightsResult.Empty
        }

        val dailyMetrics = dailyMetrics(days, symptoms, activities)
        if (dailyMetrics.isEmpty()) {
            return InsightsResult.Empty
        }

        val trend = buildTrend(dailyMetrics)
        val correlations = buildCorrelations(dailyMetrics)
        return InsightsResult.Ready(
            trend = trend,
            correlations = correlations,
            generatedAt = dailyMetrics.maxOf { it.date }
        )
    }

    fun dailyMetrics(
        days: List<Day>,
        symptoms: List<SymptomEntry>,
        activities: List<ActivityEntry>
    ): List<DailyMetrics> = buildDailyMetrics(days, symptoms, activities)

    private fun buildDailyMetrics(
        days: List<Day>,
        symptoms: List<SymptomEntry>,
        activities: List<ActivityEntry>
    ): List<DailyMetrics> {
        val symptomByDate = symptoms.groupBy { it.date }
        val activityByDate = activities.groupBy { it.date }

        return days
            .sortedBy { it.date }
            .map { day ->
                val fatigue = 100 - day.restedness0To100
                val symptomList = symptomByDate[day.date].orEmpty()
                val activityList = activityByDate[day.date].orEmpty()
                DailyMetrics(
                    date = day.date,
                    fatigue = fatigue.toDouble(),
                    symptomAverage = symptomList.takeIf { it.isNotEmpty() }?.map { it.severity1To10 }?.average(),
                    symptomSampleSize = symptomList.size,
                    activityAverage = activityList.takeIf { it.isNotEmpty() }?.map { it.perceivedExhaustion1To10 }?.average(),
                    activityMinutesByType = activityList.groupBy { it.type }.mapValues { entry ->
                        entry.value.mapNotNull { it.durationMinutes }.sum()
                    },
                    activitySampleSize = activityList.size
                )
            }
    }

    private fun buildTrend(dailyMetrics: List<DailyMetrics>): TrendInsights? {
        val sorted = dailyMetrics.sortedBy { it.date }
        if (sorted.isEmpty()) return null

        val points = mutableListOf<TrendPoint>()
        val fatigueValues = mutableListOf<Double>()
        sorted.forEachIndexed { index, metrics ->
            fatigueValues.add(metrics.fatigue)
            val windowStart = (index - WINDOW_SIZE + 1).coerceAtLeast(0)
            val window = fatigueValues.subList(windowStart, fatigueValues.size)
            if (window.size == WINDOW_SIZE) {
                points.add(
                    TrendPoint(
                        date = metrics.date,
                        movingAverage = window.average()
                    )
                )
            }
        }
        if (points.isEmpty()) return null
        val latest = points.last()
        val previous = points.dropLast(1).lastOrNull()
        val delta = previous?.let { latest.movingAverage - it.movingAverage }
        return TrendInsights(
            points = points.takeLast(MAX_TREND_POINTS),
            latestAverage = latest.movingAverage,
            averageDelta = delta
        )
    }

    private fun buildCorrelations(dailyMetrics: List<DailyMetrics>): List<CorrelationInsights> {
        val metricsByDate = dailyMetrics.associateBy { it.date }
        val sameDaySymptoms = metricsByDate.values
            .filter { it.symptomAverage != null }
            .map { it.fatigue to it.symptomAverage!! }
        val sameDayActivities = metricsByDate.values
            .filter { it.activityAverage != null }
            .map { it.fatigue to it.activityAverage!! }
        val lagSymptoms = metricsByDate.values
            .mapNotNull { today ->
                val yesterdayMetrics = metricsByDate[today.date.minusDays(1)]
                val symptomAvg = yesterdayMetrics?.symptomAverage ?: return@mapNotNull null
                today.fatigue to symptomAvg
            }
        val lagActivities = metricsByDate.values
            .mapNotNull { today ->
                val yesterdayMetrics = metricsByDate[today.date.minusDays(1)]
                val activityAvg = yesterdayMetrics?.activityAverage ?: return@mapNotNull null
                today.fatigue to activityAvg
            }

        return listOfNotNull(
            correlationFromSamples(
                type = CorrelationType.SYMPTOMS_SAME_DAY,
                samples = sameDaySymptoms
            ),
            correlationFromSamples(
                type = CorrelationType.ACTIVITIES_SAME_DAY,
                samples = sameDayActivities
            ),
            correlationFromSamples(
                type = CorrelationType.SYMPTOMS_LAG_ONE,
                samples = lagSymptoms
            ),
            correlationFromSamples(
                type = CorrelationType.ACTIVITIES_LAG_ONE,
                samples = lagActivities
            )
        )
    }

    private fun correlationFromSamples(
        type: CorrelationType,
        samples: List<Pair<Double, Double>>
    ): CorrelationInsights? {
        if (samples.size < MIN_SAMPLE_SIZE) return null
        val xs = samples.map { it.first }
        val ys = samples.map { it.second }
        val r = pearson(xs, ys) ?: return null
        val effect = effectSizeLabel(r)
        val confidence = confidenceLabel(xs.size)
        return CorrelationInsights(
            type = type,
            coefficient = r,
            sampleSize = xs.size,
            effectLabel = effect,
            confidenceLabel = confidence
        )
    }

    private fun pearson(xs: List<Double>, ys: List<Double>): Double? {
        if (xs.size != ys.size || xs.isEmpty()) return null
        val meanX = xs.average()
        val meanY = ys.average()
        var numerator = 0.0
        var denomX = 0.0
        var denomY = 0.0
        for (i in xs.indices) {
            val dx = xs[i] - meanX
            val dy = ys[i] - meanY
            numerator += dx * dy
            denomX += dx.pow(2)
            denomY += dy.pow(2)
        }
        if (denomX == 0.0 || denomY == 0.0) return null
        return (numerator / sqrt(denomX * denomY)).coerceIn(-1.0, 1.0)
    }

    private fun effectSizeLabel(r: Double): String = when {
        abs(r) >= 0.5 -> "large"
        abs(r) >= 0.3 -> "medium"
        abs(r) >= 0.1 -> "small"
        else -> "trace"
    }

    private fun confidenceLabel(n: Int): String = when {
        n < 10 -> "Low"
        n < 21 -> "Medium"
        else -> "High"
    }

    private const val WINDOW_SIZE = 7
    private const val MIN_SAMPLE_SIZE = 3
    private const val MAX_TREND_POINTS = 28
}

data class DailyMetrics(
    val date: LocalDate,
    val fatigue: Double,
    val symptomAverage: Double?,
    val symptomSampleSize: Int,
    val activityAverage: Double?,
    val activityMinutesByType: Map<String, Int>,
    val activitySampleSize: Int
)

data class TrendPoint(
    val date: LocalDate,
    val movingAverage: Double
)

data class TrendInsights(
    val points: List<TrendPoint>,
    val latestAverage: Double,
    val averageDelta: Double?
)

enum class CorrelationType {
    SYMPTOMS_SAME_DAY,
    ACTIVITIES_SAME_DAY,
    SYMPTOMS_LAG_ONE,
    ACTIVITIES_LAG_ONE
}

data class CorrelationInsights(
    val type: CorrelationType,
    val coefficient: Double,
    val sampleSize: Int,
    val effectLabel: String,
    val confidenceLabel: String
) {
    val headline: String
        get() = when (type) {
            CorrelationType.SYMPTOMS_SAME_DAY -> "Symptoms vs fatigue (same day)"
            CorrelationType.ACTIVITIES_SAME_DAY -> "Activity exhaustion vs fatigue (same day)"
            CorrelationType.SYMPTOMS_LAG_ONE -> "Symptoms yesterday vs fatigue today"
            CorrelationType.ACTIVITIES_LAG_ONE -> "Activity exhaustion yesterday vs fatigue today"
        }

    val narrative: String
        get() {
            val direction = when {
                coefficient > 0.05 -> "higher"
                coefficient < -0.05 -> "lower"
                else -> "similar"
            }
            val target = when (type) {
                CorrelationType.SYMPTOMS_SAME_DAY, CorrelationType.SYMPTOMS_LAG_ONE -> "symptom severity"
                else -> "activity exhaustion"
            }
            val timing = when (type) {
                CorrelationType.SYMPTOMS_LAG_ONE, CorrelationType.ACTIVITIES_LAG_ONE -> "yesterday"
                else -> "today"
            }
            val fatigueTiming = "today"
            val coeffDisplay = String.format("%.2f", coefficient)
            return "${direction.replaceFirstChar { it.uppercase() }} $target $timing correlated with fatigue $fatigueTiming (r $coeffDisplay, $effectLabel effect, $confidenceLabel confidence). Not causal."
        }
}

sealed class InsightsResult {
    data object Empty : InsightsResult()
    data class Ready(
        val trend: TrendInsights?,
        val correlations: List<CorrelationInsights>,
        val generatedAt: LocalDate
    ) : InsightsResult()
}


