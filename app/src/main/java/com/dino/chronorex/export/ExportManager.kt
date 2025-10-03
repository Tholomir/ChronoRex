package com.dino.chronorex.export

import android.content.ContentValues
import android.content.Context
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.provider.MediaStore
import androidx.core.content.FileProvider
import com.dino.chronorex.analytics.InsightsCalculator
import com.dino.chronorex.analytics.InsightsResult
import com.dino.chronorex.analytics.TrendPoint
import com.dino.chronorex.data.repository.ActivityRepository
import com.dino.chronorex.data.repository.DayRepository
import com.dino.chronorex.data.repository.SettingsRepository
import com.dino.chronorex.data.repository.SymptomRepository
import com.dino.chronorex.data.repository.WeeklyReviewRepository
import com.dino.chronorex.model.ActivityEntry
import com.dino.chronorex.model.Day
import com.dino.chronorex.model.Settings
import com.dino.chronorex.model.SymptomEntry
import com.dino.chronorex.model.WeeklyReview
import java.io.File
import java.io.FileOutputStream
import java.time.Clock
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.abs
import kotlin.math.max
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext

class ExportManager(
    private val context: Context,
    private val dayRepository: DayRepository,
    private val symptomRepository: SymptomRepository,
    private val activityRepository: ActivityRepository,
    private val settingsRepository: SettingsRepository,
    private val weeklyReviewRepository: WeeklyReviewRepository,
    private val clock: Clock = Clock.systemDefaultZone()
) {

    suspend fun createCsvExport(): ExportResult = withContext(Dispatchers.IO) {
        val timestamp = DateTimeFormatter.ofPattern("yyyyMMdd_HHmm", Locale.US)
            .format(java.time.LocalDateTime.now(clock))
        val exportDir = prepareExportDir()

        val days = dayRepository.observeAll().first()
        val symptoms = symptomRepository.observeAll().first()
        val activities = activityRepository.observeAll().first()
        val settings = settingsRepository.loadSettings()
        val weeklyReviews = weeklyReviewRepository.getAll()

        val files = listOf(
            writeCsv(exportDir, "chronorex_days_${timestamp}.csv", buildDaysCsv(days)),
            writeCsv(exportDir, "chronorex_symptoms_${timestamp}.csv", buildSymptomsCsv(symptoms)),
            writeCsv(exportDir, "chronorex_activities_${timestamp}.csv", buildActivitiesCsv(activities)),
            writeCsv(exportDir, "chronorex_settings_${timestamp}.csv", buildSettingsCsv(settings)),
            writeCsv(exportDir, "chronorex_weekly_reviews_${timestamp}.csv", buildWeeklyReviewsCsv(weeklyReviews)),
            writeCsv(exportDir, "chronorex_data_dictionary_${timestamp}.csv", buildDataDictionaryCsv())
        )

        ExportResult(
            uris = files.map(::fileUri),
            fileNames = files.map(File::getName),
            mimeType = MIME_CSV,
            label = "ChronoRex CSV $timestamp",
            description = "Includes days, symptoms, activities, settings, weekly reviews, and a data dictionary."
        )
    }

    suspend fun createPdfExport(): ExportResult = withContext(Dispatchers.IO) {
        val timestamp = DateTimeFormatter.ofPattern("yyyyMMdd_HHmm", Locale.US)
            .format(java.time.LocalDateTime.now(clock))
        val exportDir = prepareExportDir()

        val days = dayRepository.observeAll().first()
        val symptoms = symptomRepository.observeAll().first()
        val activities = activityRepository.observeAll().first()
        val insights = InsightsCalculator.calculate(days, symptoms, activities)

        val file = File(exportDir, "chronorex_report_${timestamp}.pdf")
        buildPdfReport(file, days, symptoms, activities, insights)

        ExportResult(
            uris = listOf(fileUri(file)),
            fileNames = listOf(file.name),
            mimeType = MIME_PDF,
            label = "ChronoRex report $timestamp",
            description = "One-page clinician summary"
        )
    }

    suspend fun saveToDownloads(result: ExportResult): List<Uri> = withContext(Dispatchers.IO) {
        val resolver = context.contentResolver
        result.uris.zip(result.fileNames).map { (uri, name) ->
            val values = ContentValues().apply {
                put(MediaStore.Downloads.DISPLAY_NAME, name)
                put(MediaStore.Downloads.MIME_TYPE, result.mimeType)
            }
            val destination = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)
                ?: return@map uri
            resolver.openOutputStream(destination)?.use { output ->
                resolver.openInputStream(uri)?.use { input ->
                    input.copyTo(output)
                }
            }
            destination
        }
    }

    private fun prepareExportDir(): File {
        val exportDir = File(context.cacheDir, "exports")
        if (!exportDir.exists()) {
            exportDir.mkdirs()
        } else {
            exportDir.listFiles()?.forEach(File::delete)
        }
        return exportDir
    }

    private fun writeCsv(directory: File, fileName: String, contents: String): File {
        val file = File(directory, fileName)
        FileOutputStream(file).use { stream ->
            stream.write(contents.toByteArray(Charsets.UTF_8))
        }
        return file
    }

    private fun fileUri(file: File): Uri = FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider",
        file
    )

    private fun buildDaysCsv(days: List<Day>): String {
        val header = listOf(
            "date",
            "timezone_offset_minutes",
            "restedness_0_100",
            "sleep_quality_1_5",
            "notes",
            "emoji_tags",
            "illness",
            "travel"
        )
        val rows = days.sortedBy { it.date }.map { day ->
            csvRow(
                day.date,
                day.timezoneOffsetMinutes,
                day.restedness0To100,
                day.sleepQuality1To5,
                day.notes.orEmpty(),
                day.emojiTags.joinToString(" "),
                day.illness,
                day.travel
            )
        }
        return (listOf(csvRow(*header.toTypedArray())) + rows).joinToString(separator = "\n") + "\n"
    }

    private fun buildSymptomsCsv(symptoms: List<SymptomEntry>): String {
        val header = csvRow("id", "date", "time", "name", "severity_1_10", "note")
        val rows = symptoms.sortedBy { it.time }.map { entry ->
            csvRow(
                entry.id,
                entry.date,
                entry.time,
                entry.name,
                entry.severity1To10,
                entry.note.orEmpty()
            )
        }
        return (listOf(header) + rows).joinToString(separator = "\n") + "\n"
    }

    private fun buildActivitiesCsv(activities: List<ActivityEntry>): String {
        val header = csvRow("id", "date", "time", "type", "duration_minutes", "perceived_exhaustion_1_10", "note")
        val rows = activities.sortedBy { it.time }.map { entry ->
            csvRow(
                entry.id,
                entry.date,
                entry.time,
                entry.type,
                entry.durationMinutes ?: "",
                entry.perceivedExhaustion1To10,
                entry.note.orEmpty()
            )
        }
        return (listOf(header) + rows).joinToString(separator = "\n") + "\n"
    }

    private fun buildSettingsCsv(settings: Settings): String {
        val header = csvRow(
            "reminder_time",
            "passcode_hash",
            "biometrics_enabled",
            "theme",
            "smart_snooze_enabled",
            "auto_lock_on_background",
            "before_four_am_is_yesterday",
            "notifications_denied",
            "onboarding_completed",
            "snoozed_until"
        )
        val row = csvRow(
            settings.reminderTime,
            settings.passcodeHash.orEmpty(),
            settings.biometricsEnabled,
            settings.theme,
            settings.smartSnoozeEnabled,
            settings.autoLockOnBackground,
            settings.beforeFourAmIsYesterday,
            settings.notificationsDenied,
            settings.onboardingCompleted,
            settings.snoozedUntil
        )
        return listOf(header, row).joinToString(separator = "\n") + "\n"
    }

    private fun buildWeeklyReviewsCsv(reviews: List<WeeklyReview>): String {
        val header = csvRow(
            "id",
            "start_date",
            "end_date",
            "generated_at",
            "trend_highlights",
            "correlation_highlights",
            "best_day",
            "toughest_day",
            "adherence_summary",
            "needs_in_app_nudge"
        )
        val rows = reviews.sortedByDescending { it.generatedAt }.map { review ->
            csvRow(
                review.id,
                review.startDate,
                review.endDate,
                review.generatedAt,
                review.trendHighlights.joinToString(" | "),
                review.correlationHighlights.joinToString(" | "),
                review.bestDay ?: "",
                review.toughestDay ?: "",
                review.adherenceSummary,
                review.needsInAppNudge
            )
        }
        return (listOf(header) + rows).joinToString(separator = "\n") + "\n"
    }

    private fun buildDataDictionaryCsv(): String {
        val rows = listOf(
            csvRow("entity", "column", "description"),
            csvRow("Day", "date", "ISO-8601 day for the AM check-in"),
            csvRow("Day", "restedness_0_100", "User reported restedness (higher is better)"),
            csvRow("Day", "sleep_quality_1_5", "Sleep quality rating"),
            csvRow("SymptomEntry", "severity_1_10", "Symptom severity rating"),
            csvRow("ActivityEntry", "perceived_exhaustion_1_10", "Activity exhaustion rating"),
            csvRow("Settings", "reminder_time", "Local reminder time for AM check-in"),
            csvRow("WeeklyReview", "trend_highlights", "Summary bullets generated for seven-day window"),
            csvRow("WeeklyReview", "correlation_highlights", "Narrative describing correlation strength"),
            csvRow("WeeklyReview", "adherence_summary", "Text summary of adherence for the week")
        )
        return rows.joinToString(separator = "\n") + "\n"
    }

    private fun csvRow(vararg values: Any?): String = values.joinToString(",") { value ->
        val text = value?.toString() ?: ""
        val escaped = text.replace("\"", "\"\"")
        if (escaped.contains(',') || escaped.contains('\n')) {
            "\"$escaped\""
        } else {
            escaped
        }
    }

    private fun buildPdfReport(
        file: File,
        days: List<Day>,
        symptoms: List<SymptomEntry>,
        activities: List<ActivityEntry>,
        insights: InsightsResult
    ) {
        val document = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, 1).create()
        val page = document.startPage(pageInfo)
        val canvas = page.canvas
        val headerPaint = Paint().apply {
            color = Color.BLACK
            textSize = 20f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val bodyPaint = Paint().apply {
            color = Color.DKGRAY
            textSize = 12f
        }

        var y = 40f
        canvas.drawText("ChronoRex Weekly Report", 40f, y, headerPaint)
        y += 24f
        canvas.drawText("Generated ${LocalDate.now(clock)}", 40f, y, bodyPaint)
        y += verschillende voorbeeld tekst
