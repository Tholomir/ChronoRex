package com.dino.chronorex.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
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
import com.dino.chronorex.ui.export.ExportScreen
import com.dino.chronorex.ui.export.ExportViewModel
import com.dino.chronorex.ui.home.HomeScreenRoute
import com.dino.chronorex.ui.insights.InsightsScreen
import com.dino.chronorex.ui.insights.InsightsViewModel
import com.dino.chronorex.ui.lock.LockOverlay
import com.dino.chronorex.ui.lock.LockViewModel
import com.dino.chronorex.ui.onboarding.OnboardingScreen
import com.dino.chronorex.ui.onboarding.OnboardingViewModel
import com.dino.chronorex.ui.theme.ChronoRexTheme
import com.dino.chronorex.ui.theme.spacing
import com.dino.chronorex.ui.weeklyreview.WeeklyReviewScreen
import com.dino.chronorex.ui.weeklyreview.WeeklyReviewViewModel
import java.time.LocalDate
import java.time.LocalTime

sealed class ChronoRexRoute(val route: String) {
    data object Onboarding : ChronoRexRoute("onboarding")
    data object Home : ChronoRexRoute("home")
    data object CheckIn : ChronoRexRoute("check_in")
    data object Insights : ChronoRexRoute("insights")
    data object Export : ChronoRexRoute("export")
    data object Settings : ChronoRexRoute("settings")
    data object WeeklyReview : ChronoRexRoute("weekly_review")
    data object DayDetail : ChronoRexRoute("day_detail/{dateIso}") {
        const val ARG_DATE = "dateIso"
        fun create(date: LocalDate) = "day_detail/$date"
    }
}

@Composable
fun ChronoRexApp() {
    ChronoRexTheme {
        val appState = rememberChronoRexAppState()
        val navController = appState.navController
        val viewModelFactory = remember(appState.container) { ChronoRexViewModelFactory(appState.container) }
        val lockViewModel: LockViewModel = viewModel(factory = viewModelFactory)
        val lockState by lockViewModel.state.collectAsState()
        val lifecycleOwner = LocalLifecycleOwner.current
        DisposableEffect(lifecycleOwner) {
            val observer = LifecycleEventObserver { _, event ->
                if (event == Lifecycle.Event.ON_STOP) {
                    lockViewModel.handleAppBackgrounded()
                }
            }
            lifecycleOwner.lifecycle.addObserver(observer)
            onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
        }

        val biometricLauncher = rememberBiometricLauncher(lockViewModel, lockState.biometricsEnabled)

        val backStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = backStackEntry?.destination?.route

        Box(modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)) {
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
                            onNavigateSettings = { appState.navigateTo(ChronoRexRoute.Settings) },
                            onNavigateWeeklyReview = { appState.navigateTo(ChronoRexRoute.WeeklyReview) },
                            onNavigateExport = { appState.navigateTo(ChronoRexRoute.Export) }
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
                        val viewModel: InsightsViewModel = viewModel(factory = viewModelFactory)
                        val state by viewModel.state.collectAsState()
                        InsightsScreen(state = state, onBack = { appState.navigateBack() })
                    }
                    composable(ChronoRexRoute.Export.route) {
                        val viewModel: ExportViewModel = viewModel(factory = viewModelFactory)
                        val state by viewModel.state.collectAsState()
                        ExportScreen(
                            state = state,
                            onGenerateCsv = viewModel::generateCsv,
                            onGeneratePdf = viewModel::generatePdf,
                            onClearError = viewModel::clearError,
                            onBack = { appState.navigateBack() },
                            saveToDownloads = viewModel::saveToDownloads
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
                    composable(ChronoRexRoute.WeeklyReview.route) {
                        val viewModel: WeeklyReviewViewModel = viewModel(factory = viewModelFactory)
                        val state by viewModel.state.collectAsState()
                        val review = state.latestReview
                        LaunchedEffect(review?.id) {
                            review?.let { viewModel.markReviewOpened(it.id) }
                        }
                        WeeklyReviewScreen(review = review, onBack = { appState.navigateBack() })
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

            if (lockState.isLocked && lockState.passcodeRequired) {
                LockOverlay(
                    state = lockState,
                    onSubmitPasscode = lockViewModel::submitPasscode,
                    onClearError = lockViewModel::clearError,
                    onBiometricRequested = biometricLauncher
                )
            }
        }
    }
}

@Composable
private fun rememberBiometricLauncher(
    lockViewModel: LockViewModel,
    enabled: Boolean
): (() -> Unit)? {
    if (!enabled) return null
    val context = LocalContext.current
    val activity = context as? FragmentActivity ?: return null
    val executor = remember { ContextCompat.getMainExecutor(context) }
    val prompt = remember(lockViewModel) {
        BiometricPrompt(activity, executor, object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                lockViewModel.unlockWithBiometrics()
            }

            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                lockViewModel.reportBiometricFailure(errString.toString())
            }

            override fun onAuthenticationFailed() {
                lockViewModel.reportBiometricFailure("Biometric not recognized")
            }
        })
    }
    val promptInfo = remember {
        BiometricPrompt.PromptInfo.Builder()
            .setTitle("Unlock ChronoRex")
            .setSubtitle("Use biometrics to continue")
            .setNegativeButtonText("Cancel")
            .build()
    }
    return {
        prompt.authenticate(promptInfo)
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
