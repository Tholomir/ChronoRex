package com.dino.chronorex

import android.app.Application
import com.dino.chronorex.data.local.ChronoRexDatabase
import com.dino.chronorex.data.repository.ActivityRepository
import com.dino.chronorex.data.repository.DayRepository
import com.dino.chronorex.data.repository.SettingsRepository
import com.dino.chronorex.data.repository.SymptomRepository
import com.dino.chronorex.notification.ReminderManager
import com.dino.chronorex.notification.ReminderNotificationChannel
import com.dino.chronorex.notification.ReminderScheduler

class ChronoRexApplication : Application(), ChronoRexAppContainerHolder {
    override lateinit var container: ChronoRexAppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        ReminderNotificationChannel.ensureCreated(this)
        container = DefaultChronoRexAppContainer(this)
    }
}

interface ChronoRexAppContainerHolder {
    val container: ChronoRexAppContainer
}

interface ChronoRexAppContainer {
    val dayRepository: DayRepository
    val symptomRepository: SymptomRepository
    val activityRepository: ActivityRepository
    val settingsRepository: SettingsRepository
    val reminderManager: ReminderManager
}

private class DefaultChronoRexAppContainer(app: Application) : ChronoRexAppContainer {
    private val database: ChronoRexDatabase by lazy { ChronoRexDatabase.getInstance(app) }

    override val dayRepository: DayRepository by lazy { DayRepository(database.dayDao()) }
    override val symptomRepository: SymptomRepository by lazy { SymptomRepository(database.symptomEntryDao()) }
    override val activityRepository: ActivityRepository by lazy { ActivityRepository(database.activityEntryDao()) }
    override val settingsRepository: SettingsRepository by lazy { SettingsRepository(database.settingsDao()) }
    private val reminderScheduler: ReminderScheduler by lazy { ReminderScheduler(app) }
    override val reminderManager: ReminderManager by lazy { ReminderManager(settingsRepository, reminderScheduler) }
}

