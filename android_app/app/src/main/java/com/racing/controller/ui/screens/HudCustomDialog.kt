package com.racing.controller.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.racing.controller.model.ButtonShape
import com.racing.controller.model.ControllerSettings
import com.racing.controller.ui.theme.AccentGreen
import com.racing.controller.ui.theme.AccentRed
import com.racing.controller.ui.theme.CockpitBackground
import com.racing.controller.ui.theme.ControlBorderDark
import com.racing.controller.ui.theme.SurfaceControl
import com.racing.controller.ui.theme.SurfaceDark
import com.racing.controller.ui.theme.TextPrimary
import com.racing.controller.ui.theme.TextSecondary
import kotlin.math.roundToInt

@Composable
fun HudCustomDialog(
    currentSettings: ControllerSettings,
    onSaveSettings: (ControllerSettings) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedShape by remember { mutableStateOf(ButtonShape.fromId(currentSettings.buttonShape)) }
    var scale by remember { mutableFloatStateOf(currentSettings.buttonScale) }
    var swapPedals by remember { mutableStateOf(currentSettings.swapThrottleBrake) }
    var showRevLights by remember { mutableStateOf(currentSettings.showRevLights) }
    var showSteeringDegree by remember { mutableStateOf(currentSettings.showSteeringDegree) }
    var showClutch by remember { mutableStateOf(currentSettings.showClutch) }
    var showHandbrake by remember { mutableStateOf(currentSettings.showHandbrake) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = CockpitBackground
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp, vertical = 14.dp)
            ) {
                // Top Header Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(SurfaceControl),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Tune,
                                contentDescription = "HUD",
                                tint = TextPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Cockpit HUD Customizer",
                                color = Color.White,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Customize button shapes, sizes, pedals layout & telemetry",
                                color = TextSecondary,
                                fontSize = 11.sp
                            )
                        }
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = TextSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                var showLayoutEditor by remember { mutableStateOf(false) }

                // Minimalist Drag & Drop Editor Launcher Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .clickable { showLayoutEditor = true },
                    colors = CardDefaults.cardColors(containerColor = Color(0x1800E5FF)),
                    border = BorderStroke(1.dp, Color(0x4400E5FF))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Tune,
                                contentDescription = "Editor",
                                tint = Color(0xFF00E5FF),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Drag & Drop Canvas Editor",
                                color = TextPrimary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        Text(
                            text = "Open Editor →",
                            color = Color(0xFF00E5FF),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                if (showLayoutEditor) {
                    HudLayoutEditorDialog(
                        currentSettings = currentSettings,
                        onSaveLayout = { updated ->
                            onSaveSettings(updated)
                            showLayoutEditor = false
                            onDismiss()
                        },
                        onDismiss = { showLayoutEditor = false }
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Scrollable Content
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                ) {
                    // 1. Live Interactive Cockpit Preview Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = "LIVE HUD PREVIEW",
                                color = TextSecondary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                            Spacer(modifier = Modifier.height(10.dp))

                            // Mini preview surface
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(110.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color(0xFF0D0E12))
                                    .border(1.dp, ControlBorderDark, RoundedCornerShape(10.dp))
                                    .padding(horizontal = 24.dp, vertical = 8.dp)
                            ) {
                                val previewShape = selectedShape.toComposeShape(14)
                                val leftIsBrake = !swapPedals

                                // Left Mini Pedal
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.CenterStart)
                                        .size((52.dp * scale).coerceIn(40.dp, 64.dp))
                                        .clip(previewShape)
                                        .background(if (leftIsBrake) Color(0xFFE8002D) else Color(0xFF00E676))
                                        .border(2.dp, Color.White.copy(alpha = 0.5f), previewShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = if (leftIsBrake) "BRAKE" else "ACCL",
                                        color = Color.White,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                // Center Mini Cockpit (Rev lights + Mini Wheel + Degree Badge)
                                Column(
                                    modifier = Modifier.align(Alignment.Center),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    if (showRevLights) {
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(3.dp),
                                            modifier = Modifier.padding(bottom = 4.dp)
                                        ) {
                                            repeat(8) { idx ->
                                                Box(
                                                    modifier = Modifier
                                                        .size(width = 12.dp, height = 4.dp)
                                                        .clip(RoundedCornerShape(1.dp))
                                                        .background(
                                                            when {
                                                                idx < 3 -> Color(0xFF00E676)
                                                                idx < 6 -> Color(0xFFFF9100)
                                                                else -> Color(0xFFFF1744)
                                                            }
                                                        )
                                                )
                                            }
                                        }
                                    }

                                    if (showSteeringDegree) {
                                        Text(
                                            text = "STEER +0.0°",
                                            color = TextSecondary,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(2.dp))

                                    // Mini Wheel
                                    Box(
                                        modifier = Modifier
                                            .size(44.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFF1F232D))
                                            .border(2.dp, Color(0xFF4A5568), CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(12.dp)
                                                .clip(CircleShape)
                                                .background(Color(0xFFE8002D))
                                        )
                                    }
                                }

                                // Right Mini Pedal
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.CenterEnd)
                                        .size((52.dp * scale).coerceIn(40.dp, 64.dp))
                                        .clip(previewShape)
                                        .background(if (!leftIsBrake) Color(0xFFE8002D) else Color(0xFF00E676))
                                        .border(2.dp, Color.White.copy(alpha = 0.5f), previewShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = if (!leftIsBrake) "BRAKE" else "ACCL",
                                        color = Color.White,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // 2. Button Shapes Grid Selector
                    Text(
                        text = "BUTTON & PEDAL SHAPE",
                        color = TextSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        ButtonShape.entries.forEach { shape ->
                            val isSelected = selectedShape == shape
                            val shapeGeom = shape.toComposeShape(12)

                            Card(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable { selectedShape = shape },
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isSelected) SurfaceControl else SurfaceDark
                                ),
                                border = BorderStroke(
                                    width = if (isSelected) 2.dp else 1.dp,
                                    color = if (isSelected) AccentGreen else ControlBorderDark
                                )
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(10.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    // Shape Glyph Preview
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(shapeGeom)
                                            .background(
                                                if (isSelected) AccentGreen.copy(alpha = 0.2f) else Color(0xFF262C38)
                                            )
                                            .border(
                                                1.5.dp,
                                                if (isSelected) AccentGreen else Color(0xFF4A5568),
                                                shapeGeom
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (isSelected) {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = "Selected",
                                                tint = AccentGreen,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))

                                    Text(
                                        text = shape.displayName,
                                        color = if (isSelected) Color.White else TextPrimary,
                                        fontSize = 11.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // 3. Button Size Scale Slider
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Button & Touch Scale",
                                    color = TextPrimary,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "${(scale * 100).roundToInt()}%",
                                    color = AccentGreen,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Slider(
                                value = scale,
                                onValueChange = { scale = it },
                                valueRange = 0.80f..1.25f,
                                steps = 9,
                                colors = SliderDefaults.colors(
                                    thumbColor = AccentGreen,
                                    activeTrackColor = AccentGreen,
                                    inactiveTrackColor = SurfaceControl
                                )
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("80% Compact", color = TextSecondary, fontSize = 10.sp)
                                Text("100% Standard", color = TextSecondary, fontSize = 10.sp)
                                Text("125% Large Thumbs", color = TextSecondary, fontSize = 10.sp)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // 4. Ergonomics & Layout Toggles
                    Text(
                        text = "ERGONOMICS & VISIBILITY",
                        color = TextSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            // Swap Pedals
                            HudToggleRow(
                                title = "🔄 Swap Left/Right Pedals (Left-Handed Mode)",
                                subtitle = "Positions Throttle on Left and Brake on Right",
                                checked = swapPedals,
                                onCheckedChange = { swapPedals = it }
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            // Rev Lights Bar
                            HudToggleRow(
                                title = "💡 Shift Rev Lights Bar",
                                subtitle = "Shows RPM throttle indicator above steering wheel",
                                checked = showRevLights,
                                onCheckedChange = { showRevLights = it }
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            // Steering Angle Badge
                            HudToggleRow(
                                title = "📐 Steering Turn Degree Badge",
                                subtitle = "Displays dedicated digital degrees (+0.0°) outside wheel",
                                checked = showSteeringDegree,
                                onCheckedChange = { showSteeringDegree = it }
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            // Clutch Pedal
                            HudToggleRow(
                                title = "🏎️ Clutch Pedal",
                                subtitle = "Shows clutch pedal button under the brake",
                                checked = showClutch,
                                onCheckedChange = { showClutch = it }
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            // Handbrake
                            HudToggleRow(
                                title = "🅿️ E-Brake / Handbrake Button",
                                subtitle = "Shows emergency handbrake button in the cockpit",
                                checked = showHandbrake,
                                onCheckedChange = { showHandbrake = it }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }

                // Bottom Action Bar: Reset & Apply
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = {
                            selectedShape = ButtonShape.CIRCLE
                            scale = 1.0f
                            swapPedals = false
                            showRevLights = true
                            showSteeringDegree = true
                            showClutch = true
                            showHandbrake = true
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = SurfaceControl),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f).height(46.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Reset",
                            tint = AccentRed,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Reset HUD", color = AccentRed, fontSize = 13.sp)
                    }

                    Button(
                        onClick = {
                            val updated = currentSettings.copy(
                                buttonShape = selectedShape.id,
                                buttonScale = scale,
                                swapThrottleBrake = swapPedals,
                                showRevLights = showRevLights,
                                showSteeringDegree = showSteeringDegree,
                                showClutch = showClutch,
                                showHandbrake = showHandbrake
                            )
                            onSaveSettings(updated)
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = AccentGreen),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(2f).height(46.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Save",
                            tint = Color.Black,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            "Save HUD Customization",
                            color = Color.Black,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun HudToggleRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                color = TextPrimary,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = subtitle,
                color = TextSecondary,
                fontSize = 11.sp
            )
        }
        Spacer(modifier = Modifier.width(10.dp))
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = AccentGreen,
                uncheckedThumbColor = TextSecondary,
                uncheckedTrackColor = SurfaceControl
            )
        )
    }
}
