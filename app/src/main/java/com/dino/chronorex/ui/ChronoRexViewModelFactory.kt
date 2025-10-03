package com.dino.chronorex.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.dino.chronorex.ChronoRexAppContainer
import com.dino.chronorex.ui.calendar.CalendarViewModel
import com.dino.chronorex.ui.checkin.DailyCheckInViewModel
import com.dino.chronorex.ui.daydetail.DayDetailViewModel
import com.dino.chronorex.ui.insights.InsightsViewModel
import com.dino.chronorex.ui.onboarding.OnboardingViewModel
import com.dino.chronorex.ui.quicklog.QuickLogViewModel
import com.dino.chronorex.ui.export.ExportViewModel
import com.dino.chronorex.ui.lock.LockViewModel
import com.dino.chronorex.ui.settings.SettingsViewModel
import com.dino.chronorex.ui.weeklyreview.WeeklyReviewViewModel
import java.time.Clock

class ChronoRexViewModelFactory(
    private val container: ChronoRexAppContainer,
    private val clock: Clock = Clock.systemDefaultZone()
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(OnboardingViewModel::class.java) ->
                OnboardingViewModel(container.settingsRepository, container.reminderManager)
            modelClass.isAssignableFrom(DailyCheckInViewModel::class.java) ->
                DailyCheckInViewModel(container.dayRepository, container.settingsRepository, clock)
            modelClass.isAssignableFrom(CalendarViewModel::class.java) ->
                CalendarViewModel(
                    container.dayRepository,
                    container.symptomRepository,
                    container.activityRepository,
                    container.reminderManager,
                    clock
                )
            modelClass.isAssignableFrom(DayDetailViewModel::class.java) ->
                DayDetailViewModel(
                    container.dayRepository,
                    container.symptomRepository,
                    container.activityRepository
                )
            modelClass.isAssignableFrom(QuickLogViewModel::class.java) ->
                QuickLogViewModel(container.symptomRepository, container.activityRepository, clock)
            modelClass.isAssignableFrom(InsightsViewModel::class.java) ->
                InsightsViewModel(
                    container.dayRepository,
                    container.symptomRepository,
                    container.activityRepository
                )
            modelClass.isAssignableFrom(WeeklyReviewViewModel::class.java) ->
                WeeklyReviewViewModel(
                    container.weeklyReviewRepository,
                    container.dayRepository,
                    container.symptomRepository,
                    container.activityRepository,
                    container.settingsRepository
                )
            modelClass.isAssignableFrom(SettingsViewModel::class.java) ->
                SettingsViewModel(container.settingsRepository, container.reminderManager)
            modelClass.isAssignableFrom(ExportViewModel::class.java) ->
                ExportViewModel(container.exportManager)
            modelClass.isAssignableFrom(LockViewModel::class.java) ->
                LockViewModel(container.settingsRepository)
            else -> throw IllegalArgumentException("Unknown ViewModel class: $modelClass")
        } as T
    }
}
