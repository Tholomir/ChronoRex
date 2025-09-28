package com.dino.chronorex.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.NavOptions
import androidx.navigation.compose.rememberNavController
import com.dino.chronorex.ChronoRexAppContainer
import com.dino.chronorex.ChronoRexAppContainerHolder
import com.dino.chronorex.data.repository.SettingsRepository
import com.dino.chronorex.model.Settings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@Stable
class ChronoRexAppState(
    val navController: NavHostController,
    val container: ChronoRexAppContainer,
    coroutineScope: CoroutineScope
) {
    private val settingsRepository: SettingsRepository = container.settingsRepository

    val settingsState: StateFlow<Settings> = settingsRepository
        .observeSettings()
        .stateIn(
            scope = coroutineScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = Settings.default()
        )

    val startDestination: String
        get() = if (settingsState.value.onboardingCompleted) ChronoRexRoute.Home.route else ChronoRexRoute.Onboarding.route

    fun navigateTo(route: ChronoRexRoute, navOptions: NavOptions? = null) {
        val options = navOptions ?: NavOptions.Builder().setLaunchSingleTop(true).build()
        navController.navigate(route.route, options)
    }

    fun navigateBack() {
        navController.popBackStack()
    }

    fun markOnboardingComplete() {
        coroutineScope.launch {
            settingsRepository.update { current -> current.copy(onboardingCompleted = true) }
        }
    }
}

@Composable
fun rememberChronoRexAppState(
    navController: NavHostController = rememberNavController()
): ChronoRexAppState {
    val context = LocalContext.current.applicationContext as ChronoRexAppContainerHolder
    val container = context.container
    val coroutineScope = rememberCoroutineScope()
    return remember(navController, container) {
        ChronoRexAppState(navController, container, coroutineScope)
    }
}


