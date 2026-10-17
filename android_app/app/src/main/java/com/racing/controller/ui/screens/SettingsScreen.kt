package com.racing.controller.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.racing.controller.ui.theme.AccentRed
import com.racing.controller.ui.theme.CockpitBackground
import com.racing.controller.ui.theme.SurfaceControl
import com.racing.controller.ui.theme.SurfaceDark
import com.racing.controller.ui.theme.TextPrimary
import com.racing.controller.ui.theme.TextSecondary
import com.racing.controller.viewmodel.ControllerViewModel
import kotlin.math.roundToInt

@Composable
fun SettingsScreen(
    viewModel: ControllerViewModel,
    onBack: () -> Unit
) {
    val settings by viewModel.settingsState.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(CockpitBackground)
            .padding(24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            // Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = TextPrimary
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Controller Settings",
                    color = TextPrimary,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Two-column landscape settings
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Column 1: Steering Mechanics
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            text = "Steering Dynamics",
                            color = TextPrimary,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Sensitivity Slider
                        SettingSlider(
                            label = "Sensitivity: ${(settings.steeringSensitivity * 100).roundToInt() / 100f}x",
                            value = settings.steeringSensitivity,
                            range = 0.5f..2.5f,
                            onValueChange = {
                                viewModel.saveSettings(settings.copy(steeringSensitivity = it))
                            }
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Max Angle Range Slider
                        SettingSlider(
                            label = "Steering Range: ±${settings.maxSteeringAngleDegrees.roundToInt()}°",
                            value = settings.maxSteeringAngleDegrees,
                            range = 20f..90f,
                            onValueChange = {
                                viewModel.saveSettings(settings.copy(maxSteeringAngleDegrees = it))
                            }
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Deadzone Slider
                        SettingSlider(
                            label = "Dead Zone: ${settings.deadZonePercent.roundToInt()}%",
                            value = settings.deadZonePercent,
                            range = 0f..15f,
                            onValueChange = {
                                viewModel.saveSettings(settings.copy(deadZonePercent = it))
                            }
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Smoothing Filter Slider
                        SettingSlider(
                            label = "Smoothing (Lag vs Noise): ${(settings.smoothingFactor * 100).roundToInt()}%",
                            value = settings.smoothingFactor,
                            range = 0.0f..0.7f,
                            onValueChange = {
                                viewModel.saveSettings(settings.copy(smoothingFactor = it))
                            }
                        )
                    }
                }

                // Column 2: Toggles & Hardware
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            text = "Preferences & Controls",
                            color = TextPrimary,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Invert Steering Toggle
                        SettingToggle(
                            label = "Invert Steering Axis",
                            checked = settings.invertSteering,
                            onCheckedChange = {
                                viewModel.saveSettings(settings.copy(invertSteering = it))
                            }
                        )

                        // Touch Steering Toggle
                        SettingToggle(
                            label = "Touch Steering (Wheel Drag)",
                            checked = settings.useTouchSteering,
                            onCheckedChange = {
                                viewModel.saveSettings(settings.copy(useTouchSteering = it))
                            }
                        )

                        // Haptic Feedback Toggle
                        SettingToggle(
                            label = "Haptic Vibration Feedback",
                            checked = settings.hapticFeedbackEnabled,
                            onCheckedChange = {
                                viewModel.saveSettings(settings.copy(hapticFeedbackEnabled = it))
                            }
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Update Rate Hz Slider
                        SettingSlider(
                            label = "Transmission Rate: ${settings.updateRateHz} Hz",
                            value = settings.updateRateHz.toFloat(),
                            range = 30f..100f,
                            onValueChange = {
                                viewModel.saveSettings(settings.copy(updateRateHz = it.roundToInt()))
                            }
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // F1 Steering Wheel Theme Card
                        val currentTeam = remember(settings.selectedTeam) {
                            com.racing.controller.model.RacingTeam.fromId(settings.selectedTeam)
                        }
                        var showThemeDialog by remember { mutableStateOf(false) }

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { showThemeDialog = true },
                            colors = CardDefaults.cardColors(containerColor = SurfaceControl),
                            border = androidx.compose.foundation.BorderStroke(1.dp, currentTeam.secondaryColor.copy(alpha = 0.6f))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    com.racing.controller.ui.components.TeamLogoBadge(
                                        team = currentTeam,
                                        size = 32.dp
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            text = "F1 Steering Wheel Theme",
                                            color = TextPrimary,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = currentTeam.teamName,
                                            color = currentTeam.secondaryColor,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }

                                Text(
                                    text = "Change",
                                    color = TextSecondary,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }

                        if (showThemeDialog) {
                            F1ThemeDialog(
                                currentTeam = currentTeam,
                                onSelectTeam = { newTeam ->
                                    viewModel.saveSettings(settings.copy(selectedTeam = newTeam.id))
                                },
                                onDismiss = { showThemeDialog = false }
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Button(
                            onClick = { viewModel.resetSettingsToDefaults() },
                            colors = ButtonDefaults.buttonColors(containerColor = SurfaceControl),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Reset to Defaults", color = AccentRed)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingSlider(
    label: String,
    value: Float,
    range: ClosedFloatingPointRange<Float>,
    onValueChange: (Float) -> Unit
) {
    Column {
        Text(text = label, color = TextSecondary, fontSize = 13.sp)
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = range,
            colors = SliderDefaults.colors(
                thumbColor = TextPrimary,
                activeTrackColor = TextPrimary,
                inactiveTrackColor = SurfaceControl
            )
        )
    }
}

@Composable
private fun SettingToggle(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, color = TextPrimary, fontSize = 13.sp)
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = TextPrimary,
                checkedTrackColor = SurfaceControl,
                uncheckedThumbColor = TextSecondary,
                uncheckedTrackColor = CockpitBackground
            )
        )
    }
}
