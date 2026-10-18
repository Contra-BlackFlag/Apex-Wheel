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

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.racing.controller.model.ButtonShape
import com.racing.controller.model.HudElementId
import com.racing.controller.model.HudLayoutProfile
import com.racing.controller.model.RacingTeam
import kotlin.math.roundToInt

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
    val buttonShape = remember(settings.buttonShape) { ButtonShape.fromId(settings.buttonShape) }

    // Load active HUD layout profile (custom user coordinates or default presets)
    val hudLayout = remember(settings.hudLayoutJson, settings.swapThrottleBrake) {
        if (settings.hudLayoutJson.isNotBlank()) {
            HudLayoutProfile.fromJson(settings.hudLayoutJson)
        } else if (settings.swapThrottleBrake) {
            HudLayoutProfile.leftHandedProfile()
        } else {
            HudLayoutProfile.defaultProfile()
        }
    }

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
        // Top HUD Control Bar with active F1 Team Badge and Centered Calibrate Button
        TopControlBar(
            connectionState = connectionState,
            team = team,
            onTeamClick = { showThemeDialog = true },
            onCalibrateClick = onShowCalibration,
            onConnectionClick = onNavigateToConnection,
            onSettingsClick = onNavigateToSettings,
            modifier = Modifier.align(Alignment.TopCenter)
        )

        // Dynamic PUBG Mobile Custom HUD Surface
        BoxWithConstraints(
            modifier = Modifier.fillMaxSize()
        ) {
            val screenW = constraints.maxWidth.toFloat()
            val screenH = constraints.maxHeight.toFloat()
            val density = LocalDensity.current

            // 1. BRAKE PEDAL
            val brake = hudLayout.getConfig(HudElementId.BRAKE)
            if (brake.visible) {
                val sizeDp = 110.dp * brake.scale * settings.buttonScale
                val sizePx = with(density) { sizeDp.toPx() }
                Box(
                    modifier = Modifier
                        .offset {
                            IntOffset(
                                (screenW * brake.xNormalized - sizePx / 2f).roundToInt(),
                                (screenH * brake.yNormalized - sizePx / 2f).roundToInt()
                            )
                        }
                        .graphicsLayer { alpha = brake.opacity }
                ) {
                    TactileCircleButton(
                        text = "Brake",
                        size = sizeDp,
                        buttonShape = buttonShape,
                        onValueChange = { viewModel.setBrake(it) }
                    )
                }
            }

            // 2. THROTTLE / ACCELERATOR PEDAL
            val throttle = hudLayout.getConfig(HudElementId.THROTTLE)
            if (throttle.visible) {
                val sizeDp = 110.dp * throttle.scale * settings.buttonScale
                val sizePx = with(density) { sizeDp.toPx() }
                Box(
                    modifier = Modifier
                        .offset {
                            IntOffset(
                                (screenW * throttle.xNormalized - sizePx / 2f).roundToInt(),
                                (screenH * throttle.yNormalized - sizePx / 2f).roundToInt()
                            )
                        }
                        .graphicsLayer { alpha = throttle.opacity }
                ) {
                    TactileCircleButton(
                        text = "Accl",
                        size = sizeDp,
                        buttonShape = buttonShape,
                        onValueChange = { viewModel.setThrottle(it) }
                    )
                }
            }

            // 3. CLUTCH PEDAL
            val clutch = hudLayout.getConfig(HudElementId.CLUTCH)
            if (clutch.visible && settings.showClutch) {
                val wDp = 130.dp * clutch.scale * settings.buttonScale
                val hDp = 84.dp * clutch.scale * settings.buttonScale
                val wPx = with(density) { wDp.toPx() }
                val hPx = with(density) { hDp.toPx() }
                Box(
                    modifier = Modifier
                        .offset {
                            IntOffset(
                                (screenW * clutch.xNormalized - wPx / 2f).roundToInt(),
                                (screenH * clutch.yNormalized - hPx / 2f).roundToInt()
                            )
                        }
                        .graphicsLayer { alpha = clutch.opacity }
                ) {
                    TactileClutchButton(
                        width = wDp,
                        height = hDp,
                        buttonShape = buttonShape,
                        onValueChange = { viewModel.setClutch(it) }
                    )
                }
            }

            // 4. GEAR SHIFTER
            val gears = hudLayout.getConfig(HudElementId.GEAR_SHIFTER)
            if (gears.visible) {
                val wDp = 100.dp * gears.scale * settings.buttonScale.coerceAtLeast(0.85f)
                val hDp = 60.dp * gears.scale * settings.buttonScale.coerceAtLeast(0.85f)
                val wPx = with(density) { wDp.toPx() }
                val hPx = with(density) { hDp.toPx() }
                Box(
                    modifier = Modifier
                        .offset {
                            IntOffset(
                                (screenW * gears.xNormalized - wPx / 2f).roundToInt(),
                                (screenH * gears.yNormalized - hPx / 2f).roundToInt()
                            )
                        }
                        .graphicsLayer { alpha = gears.opacity }
                ) {
                    GearArrows(
                        width = wDp,
                        height = hDp,
                        onGearUpChange = { viewModel.setGearUp(it) },
                        onGearDownChange = { viewModel.setGearDown(it) }
                    )
                }
            }

            // 5. STEERING WHEEL
            val wheel = hudLayout.getConfig(HudElementId.STEERING_WHEEL)
            if (wheel.visible) {
                val diamDp = 230.dp * wheel.scale
                val diamPx = with(density) { diamDp.toPx() }
                Box(
                    modifier = Modifier
                        .offset {
                            IntOffset(
                                (screenW * wheel.xNormalized - diamPx / 2f).roundToInt(),
                                (screenH * wheel.yNormalized - diamPx / 2f).roundToInt()
                            )
                        }
                        .graphicsLayer { alpha = wheel.opacity }
                ) {
                    SteeringWheelView(
                        rotationAngleDegrees = steeringAngle,
                        diameter = diamDp,
                        team = team,
                        touchSteeringEnabled = settings.useTouchSteering,
                        onTouchSteer = { viewModel.setTouchSteering(it) }
                    )
                }
            }

            // 6. HANDBRAKE / E-BRAKE
            val handbrake = hudLayout.getConfig(HudElementId.HANDBRAKE)
            if (handbrake.visible && settings.showHandbrake) {
                val sizeDp = 58.dp * handbrake.scale * settings.buttonScale
                val sizePx = with(density) { sizeDp.toPx() }
                Box(
                    modifier = Modifier
                        .offset {
                            IntOffset(
                                (screenW * handbrake.xNormalized - sizePx / 2f).roundToInt(),
                                (screenH * handbrake.yNormalized - sizePx / 2f).roundToInt()
                            )
                        }
                        .graphicsLayer { alpha = handbrake.opacity }
                ) {
                    TactileEBrakeButton(
                        size = sizeDp,
                        buttonShape = buttonShape,
                        onPressChange = { viewModel.setHandbrake(it) }
                    )
                }
            }

            // 7. REV LIGHT STRIP
            val rev = hudLayout.getConfig(HudElementId.REV_LIGHTS)
            if (rev.visible && settings.showRevLights) {
                val wDp = 240.dp * rev.scale
                val hDp = 14.dp * rev.scale
                val wPx = with(density) { wDp.toPx() }
                val hPx = with(density) { hDp.toPx() }
                Box(
                    modifier = Modifier
                        .offset {
                            IntOffset(
                                (screenW * rev.xNormalized - wPx / 2f).roundToInt(),
                                (screenH * rev.yNormalized - hPx / 2f).roundToInt()
                            )
                        }
                        .graphicsLayer { alpha = rev.opacity }
                ) {
                    RevLightStrip(
                        throttle = inputs.throttle,
                        width = wDp,
                        height = hDp
                    )
                }
            }

            // 8. DIGITAL STEERING DEGREE TELEMETRY BADGE
            val telem = hudLayout.getConfig(HudElementId.TELEMETRY_BADGE)
            if (telem.visible && settings.showSteeringDegree) {
                val wDp = 110.dp * telem.scale
                val hDp = 30.dp * telem.scale
                val wPx = with(density) { wDp.toPx() }
                val hPx = with(density) { hDp.toPx() }
                Box(
                    modifier = Modifier
                        .offset {
                            IntOffset(
                                (screenW * telem.xNormalized - wPx / 2f).roundToInt(),
                                (screenH * telem.yNormalized - hPx / 2f).roundToInt()
                            )
                        }
                        .graphicsLayer { alpha = telem.opacity }
                ) {
                    com.racing.controller.ui.components.SteeringTelemetryBadge(
                        steeringAngle = steeringAngle,
                        team = team
                    )
                }
            }

            // 9. SELECT BUTTON
            val sel = hudLayout.getConfig(HudElementId.SELECT_BTN)
            if (sel.visible) {
                val wDp = 118.dp * sel.scale
                val hDp = 38.dp * sel.scale
                val wPx = with(density) { wDp.toPx() }
                val hPx = with(density) { hDp.toPx() }
                Box(
                    modifier = Modifier
                        .offset {
                            IntOffset(
                                (screenW * sel.xNormalized - wPx / 2f).roundToInt(),
                                (screenH * sel.yNormalized - hPx / 2f).roundToInt()
                            )
                        }
                        .graphicsLayer { alpha = sel.opacity }
                ) {
                    TactilePillButton(
                        text = "Select",
                        width = wDp,
                        height = hDp,
                        onPressChange = { viewModel.setSelect(it) }
                    )
                }
            }

            // 10. START BUTTON
            val start = hudLayout.getConfig(HudElementId.START_BTN)
            if (start.visible) {
                val wDp = 118.dp * start.scale
                val hDp = 38.dp * start.scale
                val wPx = with(density) { wDp.toPx() }
                val hPx = with(density) { hDp.toPx() }
                Box(
                    modifier = Modifier
                        .offset {
                            IntOffset(
                                (screenW * start.xNormalized - wPx / 2f).roundToInt(),
                                (screenH * start.yNormalized - hPx / 2f).roundToInt()
                            )
                        }
                        .graphicsLayer { alpha = start.opacity }
                ) {
                    TactilePillButton(
                        text = "Start",
                        width = wDp,
                        height = hDp,
                        onPressChange = { viewModel.setStart(it) }
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
