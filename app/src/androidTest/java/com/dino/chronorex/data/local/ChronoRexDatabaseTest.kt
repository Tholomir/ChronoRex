package com.dino.chronorex.data.local

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.dino.chronorex.data.repository.ActivityRepository
import com.dino.chronorex.data.repository.DayRepository
import com.dino.chronorex.data.repository.SettingsRepository
import com.dino.chronorex.data.repository.SymptomRepository
import com.dino.chronorex.model.ActivityEntry
import com.dino.chronorex.model.Day
import com.dino.chronorex.model.Settings
import com.dino.chronorex.model.SymptomEntry
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.util.UUID

@RunWith(AndroidJUnit4::class)
class ChronoRexDatabaseTest {

    private lateinit var context: Context
    private lateinit var database: ChronoRexDatabase
    private lateinit var dayRepository: DayRepository
    private lateinit var symptomRepository: SymptomRepository
    private lateinit var activityRepository: ActivityRepository
    private lateinit var settingsRepository: SettingsRepository

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        database = androidx.room.Room.inMemoryDatabaseBuilder(
            context,
            ChronoRexDatabase::class.java
        )
            .allowMainThreadQueries()
            .build()

        dayRepository = DayRepository(database.dayDao())
        symptomRepository = SymptomRepository(database.symptomEntryDao())
        activityRepository = ActivityRepository(database.activityEntryDao())
        settingsRepository = SettingsRepository(database.settingsDao())
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun dayRepositoryPersistsAndDeletes() = runTest {
        val date = LocalDate.of(2025, 1, 1)
        val day = Day(
            date = date,
            timezoneOffsetMinutes = 0,
            restedness0To100 = 72,
            sleepQuality1To5 = 4,
            notes = "Felt decent",
            emojiTags = listOf("calm"),
            illness = false,
            travel = false
        )

        dayRepository.upsert(day)

        val loaded = dayRepository.getDay(date)
        assertNotNull(loaded)
        assertEquals(day, loaded)

        val observed = dayRepository.observeDay(date).first()
        assertEquals(day, observed)

        dayRepository.deleteByDate(date)
        val afterDelete = dayRepository.observeDay(date).first()
        assertNull(afterDelete)
    }

    @Test
    fun symptomAndActivityRepositoriesStoreEntries() = runTest {
        val date = LocalDate.of(2025, 2, 2)
        val symptom = SymptomEntry(
            id = UUID.randomUUID(),
            date = date,
            time = Instant.parse("2025-02-02T08:30:00Z"),
            name = "Headache",
            severity1To10 = 5,
            note = "Mild"
        )
        val activity = ActivityEntry(
            id = UUID.randomUUID(),
            date = date,
            time = Instant.parse("2025-02-02T12:00:00Z"),
            type = "Walk",
            durationMinutes = 30,
            perceivedExhaustion1To10 = 4,
            note = "Light stroll"
        )

        symptomRepository.upsert(symptom)
        activityRepository.upsert(activity)

        val symptomEntries = symptomRepository.observeSymptomsForDate(date).first()
        val activityEntries = activityRepository.observeActivitiesForDate(date).first()

        assertEquals(listOf(symptom), symptomEntries)
        assertEquals(listOf(activity), activityEntries)
    }

    @Test
    fun settingsRepositoryBootstrapsDefaultsAndUpdates() = runTest {
        val defaults = settingsRepository.loadSettings()
        assertEquals(Settings.default(), defaults)

        settingsRepository.update { current ->
            current.copy(
                reminderTime = LocalTime.of(8, 15),
                beforeFourAmIsYesterday = true
            )
        }

        val updated = settingsRepository.observeSettings().first()
        assertEquals(LocalTime.of(8, 15), updated.reminderTime)
        assertEquals(true, updated.beforeFourAmIsYesterday)
    }
}
