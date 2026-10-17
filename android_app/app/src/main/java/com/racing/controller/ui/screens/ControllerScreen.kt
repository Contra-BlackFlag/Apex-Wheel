package com.racing.controller.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.racing.controller.ui.components.GearArrows
import com.racing.controller.ui.components.RevLightStrip
import com.racing.controller.ui.components.SteeringWheelView
import com.racing.controller.ui.components.TactileCircleButton
import com.racing.controller.ui.components.TactileClutchButton
import com.racing.controller.ui.components.TactileEBrakeButton
import com.racing.controller.ui.components.TactilePillButton
import com.racing.controller.ui.components.TopControlBar
import com.racing.controller.ui.theme.CockpitBackground
import com.racing.controller.viewmodel.ControllerViewModel

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.racing.controller.model.RacingTeam

@Composable
fun ControllerScreen(
    viewModel: ControllerViewModel,
    onNavigateToConnection: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onShowCalibration: () -> Unit
) {
    val connectionState by viewModel.connectionState.collectAsState()
    val steeringAngle by viewModel.steeringAngleDegrees.collectAsState()
    val settings by viewModel.settingsState.collectAsState()
    val inputs by viewModel.inputs.collectAsState()

    var showThemeDialog by remember { mutableStateOf(false) }
    val team = remember(settings.selectedTeam) { RacingTeam.fromId(settings.selectedTeam) }

    DisposableEffect(Unit) {
        viewModel.onResumeScreen()
        onDispose {
            viewModel.onPauseScreen()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(CockpitBackground)
    ) {
        // Top HUD Control Bar with active F1 Team Badge
        TopControlBar(
            connectionState = connectionState,
            team = team,
            onTeamClick = { showThemeDialog = true },
            onCalibrateClick = onShowCalibration,
            onConnectionClick = onNavigateToConnection,
            onSettingsClick = onNavigateToSettings,
            modifier = Modifier.align(Alignment.TopCenter)
        )

        // Main Controller Surface (faithfully matches reference image)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 48.dp, vertical = 24.dp)
        ) {
            // LEFT COLUMN: Brake (top) and Clutch (bottom)
            Box(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(top = 16.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Brake (Circular)
                    TactileCircleButton(
                        text = "Brake",
                        size = 110.dp,
                        onValueChange = { viewModel.setBrake(it) }
                    )

                    Spacer(modifier = Modifier.height(38.dp))

                    // Clutch (Rounded-bottom rectangle)
                    TactileClutchButton(
                        width = 130.dp,
                        height = 84.dp,
                        onValueChange = { viewModel.setClutch(it) }
                    )
                }
            }

            // CENTER: Steering Wheel & Select/Start buttons
            Box(
                modifier = Modifier.align(Alignment.Center)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Rev Light Strip (glows red to green with accelerator press)
                    RevLightStrip(
                        throttle = inputs.throttle,
                        width = 240.dp,
                        height = 14.dp,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )

                    // Dedicated Digital Telemetry HUD Pill (Outside the wheel, zero interference!)
                    com.racing.controller.ui.components.SteeringTelemetryBadge(
                        steeringAngle = steeringAngle,
                        team = team
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    // Steering Wheel with authentic circular rim & official team logo
                    SteeringWheelView(
                        rotationAngleDegrees = steeringAngle,
                        diameter = 230.dp,
                        team = team,
                        touchSteeringEnabled = settings.useTouchSteering,
                        onTouchSteer = { viewModel.setTouchSteering(it) }
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Select Button (pill)
                    TactilePillButton(
                        text = "Select",
                        width = 118.dp,
                        height = 38.dp,
                        onPressChange = { viewModel.setSelect(it) }
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Start Button (pill)
                    TactilePillButton(
                        text = "Start",
                        width = 118.dp,
                        height = 38.dp,
                        onPressChange = { viewModel.setStart(it) }
                    )
                }
            }

            // LOWER CENTER-RIGHT: E-BRAKE button
            TactileEBrakeButton(
                size = 58.dp,
                onPressChange = { viewModel.setHandbrake(it) },
                modifier = Modifier
                    .align(Alignment.Center)
                    .offset(x = 185.dp, y = 60.dp)
            )

            // RIGHT COLUMN: Accelerator (top) and Gear Up/Down (bottom)
            Box(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(top = 16.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Accelerator (Circular)
                    TactileCircleButton(
                        text = "Accl",
                        size = 110.dp,
                        onValueChange = { viewModel.setThrottle(it) }
                    )

                    Spacer(modifier = Modifier.height(30.dp))

                    // Gear Shift Chevrons (Up and Down)
                    GearArrows(
                        width = 100.dp,
                        height = 60.dp,
                        onGearUpChange = { viewModel.setGearUp(it) },
                        onGearDownChange = { viewModel.setGearDown(it) }
                    )
                }
            }
        }
    }

    // F1 Steering Wheel Theme Selector Dialog
    if (showThemeDialog) {
        F1ThemeDialog(
            currentTeam = team,
            onSelectTeam = { newTeam ->
                viewModel.saveSettings(settings.copy(selectedTeam = newTeam.id))
            },
            onDismiss = { showThemeDialog = false }
        )
    }
}
