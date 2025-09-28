package com.dino.chronorex.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.navArgument
import com.dino.chronorex.ui.checkin.DailyCheckInScreen
import com.dino.chronorex.ui.checkin.DailyCheckInViewModel
import com.dino.chronorex.ui.components.ChronoRexCard
import com.dino.chronorex.ui.components.ChronoRexPrimaryButton
import com.dino.chronorex.ui.components.ChronoRexTopBar
import com.dino.chronorex.ui.daydetail.DayDetailScreen
import com.dino.chronorex.ui.daydetail.DayDetailViewModel
import com.dino.chronorex.ui.home.HomeScreenRoute
import com.dino.chronorex.ui.onboarding.OnboardingScreen
import com.dino.chronorex.ui.onboarding.OnboardingViewModel
import com.dino.chronorex.ui.theme.ChronoRexTheme
import com.dino.chronorex.ui.theme.spacing
import java.time.LocalDate
import java.time.LocalTime

sealed class ChronoRexRoute(val route: String) {
    data object Onboarding : ChronoRexRoute("onboarding")
    data object Home : ChronoRexRoute("home")
    data object CheckIn : ChronoRexRoute("check_in")
    data object Insights : ChronoRexRoute("insights")
    data object Settings : ChronoRexRoute("settings")
    data object DayDetail : ChronoRexRoute("day_detail/{dateIso}") {
        const val ARG_DATE = "dateIso"
        fun create(date: LocalDate) = "day_detail/${date}"
    }
}

@Composable
fun ChronoRexApp() {
    ChronoRexTheme {
        val appState = rememberChronoRexAppState()
        val navController = appState.navController
        val viewModelFactory = remember(appState.container) { ChronoRexViewModelFactory(appState.container) }
        val backStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = backStackEntry?.destination?.route

        Scaffold(
            topBar = { ChronoRexTopBar(currentRoute) }
        ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = appState.startDestination,
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize()
            ) {
                composable(ChronoRexRoute.Onboarding.route) {
                    val viewModel: OnboardingViewModel = viewModel(factory = viewModelFactory)
                    val state by viewModel.state.collectAsState()
                    val context = LocalContext.current
                    val canRequestNotifications = remember { Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU }
                    val notificationsGranted = if (!canRequestNotifications) {
                        true
                    } else {
                        ContextCompat.checkSelfPermission(
                            context,
                            Manifest.permission.POST_NOTIFICATIONS
                        ) == PackageManager.PERMISSION_GRANTED
                    }
                    val permissionLauncher = rememberLauncherForActivityResult(
                        ActivityResultContracts.RequestPermission()
                    ) { granted ->
                        viewModel.setNotificationsDenied(!granted)
                    }

                    LaunchedEffect(notificationsGranted, state.notificationsDenied) {
                        if (notificationsGranted && state.notificationsDenied) {
                            viewModel.setNotificationsDenied(false)
                        }
                    }

                    OnboardingScreen(
                        state = state,
                        canRequestNotifications = canRequestNotifications,
                        onSetReminderToNow = { viewModel.updateReminderTime(LocalTime.now()) },
                        onUpdateReminderTime = viewModel::updateReminderTime,
                        onToggleSmartSnooze = viewModel::updateSmartSnooze,
                        onTogglePasscode = viewModel::updatePasscodeRequested,
                        onToggleBiometrics = viewModel::updateBiometricsRequested,
                        onToggleAutoLock = viewModel::updateAutoLockOnBackground,
                        onRequestNotifications = {
                            if (canRequestNotifications) {
                                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                            }
                        },
                        onUpdatePasscodeValue = viewModel::updatePasscodeInput,
                        onUpdatePasscodeConfirm = viewModel::updatePasscodeConfirm,
                        onBack = viewModel::goToPreviousStep,
                        onNext = viewModel::goToNextStep,
                        onComplete = {
                            viewModel.completeOnboarding {
                                appState.markOnboardingComplete()
                                navController.navigate(ChronoRexRoute.Home.route) {
                                    popUpTo(ChronoRexRoute.Onboarding.route) { inclusive = true }
                                    launchSingleTop = true
                                }
                                navController.navigate(ChronoRexRoute.CheckIn.route) {
                                    launchSingleTop = true
                                }
                            }
                        }
                    )
                }
                composable(ChronoRexRoute.Home.route) {
                    HomeScreenRoute(
                        factory = viewModelFactory,
                        onNavigateCheckIn = { appState.navigateTo(ChronoRexRoute.CheckIn) },
                        onNavigateDetail = { date -> navController.navigate(ChronoRexRoute.DayDetail.create(date)) },
                        onNavigateInsights = { appState.navigateTo(ChronoRexRoute.Insights) },
                        onNavigateSettings = { appState.navigateTo(ChronoRexRoute.Settings) }
                    )
                }
                composable(ChronoRexRoute.CheckIn.route) {
                    val viewModel: DailyCheckInViewModel = viewModel(factory = viewModelFactory)
                    val state by viewModel.state.collectAsState()
                    DailyCheckInScreen(
                        state = state,
                        onChangeRestedness = viewModel::updateRestedness,
                        onChangeSleepQuality = viewModel::updateSleepQuality,
                        onToggleIllness = viewModel::updateIllness,
                        onToggleTravel = viewModel::updateTravel,
                        onUpdateNotes = viewModel::updateNotes,
                        onUpdateEmojiTags = viewModel::updateEmojiTags,
                        onSetBeforeFourPreference = viewModel::setBeforeFourAmPreference,
                        onSave = viewModel::saveEntry,
                        onUndo = viewModel::undoLastSave,
                        onUndoTimeout = viewModel::clearUndoRequest,
                        onBack = { appState.navigateBack() }
                    )
                }
                composable(ChronoRexRoute.Insights.route) {
                    PlaceholderScreen(
                        title = "Insights",
                        message = "Trend and correlation insights will appear here.",
                        primaryActionLabel = "Back",
                        onPrimaryAction = { appState.navigateBack() }
                    )
                }
                composable(ChronoRexRoute.Settings.route) {
                    PlaceholderScreen(
                        title = "Settings",
                        message = "Reminder preferences and privacy controls will appear here.",
                        primaryActionLabel = "Back",
                        onPrimaryAction = { appState.navigateBack() }
                    )
                }
                composable(
                    route = ChronoRexRoute.DayDetail.route,
                    arguments = listOf(navArgument(ChronoRexRoute.DayDetail.ARG_DATE) { type = NavType.StringType })
                ) { entry ->
                    val viewModel: DayDetailViewModel = viewModel(factory = viewModelFactory)
                    val dateArg = entry.arguments?.getString(ChronoRexRoute.DayDetail.ARG_DATE)
                    LaunchedEffect(dateArg) {
                        dateArg?.let { LocalDate.parse(it) }?.let(viewModel::setDate)
                    }
                    val state by viewModel.state.collectAsState()
                    DayDetailScreen(
                        state = state,
                        onDeleteSymptom = viewModel::deleteSymptom,
                        onDeleteActivity = viewModel::deleteActivity,
                        onBack = { appState.navigateBack() }
                    )
                }
            }
        }
    }
}

@Composable
private fun PlaceholderScreen(
    title: String,
    message: String,
    primaryActionLabel: String,
    onPrimaryAction: () -> Unit,
    secondaryActionLabel: String? = null,
    onSecondaryAction: (() -> Unit)? = null
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(MaterialTheme.spacing.lg),
        verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.lg)
    ) {
        ChronoRexCard {
            Text(text = title, style = MaterialTheme.typography.titleLarge)
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = MaterialTheme.spacing.sm)
            )
        }
        ChronoRexPrimaryButton(text = primaryActionLabel, onClick = onPrimaryAction)
        if (secondaryActionLabel != null && onSecondaryAction != null) {
            ChronoRexPrimaryButton(text = secondaryActionLabel, onClick = onSecondaryAction)
        }
    }
}
