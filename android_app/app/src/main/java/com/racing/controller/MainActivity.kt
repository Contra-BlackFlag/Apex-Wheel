package com.racing.controller

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.racing.controller.ui.screens.CalibrationDialog
import com.racing.controller.ui.screens.ConnectionScreen
import com.racing.controller.ui.screens.ControllerScreen
import com.racing.controller.ui.screens.SettingsScreen
import com.racing.controller.ui.theme.CockpitBackground
import com.racing.controller.ui.theme.MobileRacingControllerTheme
import com.racing.controller.viewmodel.ControllerViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: ControllerViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Keep screen awake while racing
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        // Hide system bars for immersive landscape mode
        WindowCompat.setDecorFitsSystemWindows(window, false)
        val insetsController = WindowCompat.getInsetsController(window, window.decorView)
        insetsController.hide(WindowInsetsCompat.Type.systemBars())
        insetsController.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE

        setContent {
            MobileRacingControllerTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = CockpitBackground
                ) {
                    RacingApp(viewModel)
                }
            }
        }
    }
}

@Composable
fun RacingApp(viewModel: ControllerViewModel) {
    val navController = rememberNavController()
    var showCalibrationDialog by remember { mutableStateOf(false) }

    NavHost(
        navController = navController,
        startDestination = "controller"
    ) {
        composable("controller") {
            ControllerScreen(
                viewModel = viewModel,
                onNavigateToConnection = { navController.navigate("connection") },
                onNavigateToSettings = { navController.navigate("settings") },
                onShowCalibration = { showCalibrationDialog = true }
            )
        }
        composable("connection") {
            ConnectionScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }
        composable("settings") {
            SettingsScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }
    }

    if (showCalibrationDialog) {
        CalibrationDialog(
            onDismiss = { showCalibrationDialog = false },
            onCalibrate = { viewModel.calibrateSteering() }
        )
    }
}
