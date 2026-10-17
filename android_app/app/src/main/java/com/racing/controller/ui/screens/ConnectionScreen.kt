package com.racing.controller.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Laptop
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.racing.controller.model.ConnectionState
import com.racing.controller.ui.theme.AccentAmber
import com.racing.controller.ui.theme.AccentGreen
import com.racing.controller.ui.theme.AccentRed
import com.racing.controller.ui.theme.CockpitBackground
import com.racing.controller.ui.theme.ControlBorderDark
import com.racing.controller.ui.theme.SurfaceControl
import com.racing.controller.ui.theme.SurfaceDark
import com.racing.controller.ui.theme.TextPrimary
import com.racing.controller.ui.theme.TextSecondary
import androidx.activity.compose.rememberLauncherForActivityResult
import com.racing.controller.viewmodel.ControllerViewModel
import androidx.compose.material.icons.filled.QrCodeScanner
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions

@Composable
fun ConnectionScreen(
    viewModel: ControllerViewModel,
    onBack: () -> Unit
) {
    val connectionState by viewModel.connectionState.collectAsState()
    val settings by viewModel.settingsState.collectAsState()
    val packetsSent by viewModel.networkClient.packetsSentCount.collectAsState()

    var hostIp by remember(settings.hostIp) { mutableStateOf(settings.hostIp) }
    var portStr by remember(settings.port) { mutableStateOf(settings.port.toString()) }

    val scanLauncher = rememberLauncherForActivityResult(ScanContract()) { result ->
        val contents = result.contents
        if (!contents.isNullOrBlank()) {
            val target = viewModel.handleScannedQr(contents)
            if (target != null) {
                hostIp = target.ip
                portStr = target.port.toString()
                onBack()
            }
        }
    }

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
                    text = "PC Connection",
                    color = TextPrimary,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Two-column layout for landscape
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // Connection Form Card
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            text = "Instant Setup",
                            color = TextPrimary,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Button(
                            onClick = {
                                val options = ScanOptions().apply {
                                    setPrompt("Align the QR Code from your PC in the viewfinder")
                                    setBeepEnabled(true)
                                    setOrientationLocked(false)
                                    setBarcodeImageEnabled(false)
                                }
                                scanLauncher.launch(options)
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = SurfaceControl
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth().height(46.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.QrCodeScanner,
                                contentDescription = "Scan QR",
                                tint = TextPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Scan PC QR Code",
                                color = TextPrimary,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Divider with text "OR MANUAL IP"
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Box(modifier = Modifier.weight(1f).height(1.dp).background(ControlBorderDark))
                            Text(
                                text = "  OR MANUAL IP  ",
                                color = TextSecondary,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Box(modifier = Modifier.weight(1f).height(1.dp).background(ControlBorderDark))
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = hostIp,
                            onValueChange = { hostIp = it },
                            label = { Text("PC IP Address (e.g., 192.168.1.5)") },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = TextPrimary,
                                unfocusedBorderColor = ControlBorderDark,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = portStr,
                            onValueChange = { portStr = it },
                            label = { Text("Port (Default: 9876)") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = TextPrimary,
                                unfocusedBorderColor = ControlBorderDark,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        val isConnected = connectionState is ConnectionState.Connected
                        val isConnecting = connectionState is ConnectionState.Connecting

                        Button(
                            onClick = {
                                if (isConnected || isConnecting) {
                                    viewModel.disconnect()
                                } else {
                                    val port = portStr.toIntOrNull() ?: 9876
                                    viewModel.connect(hostIp.trim(), port)
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isConnected) AccentRed else SurfaceControl
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth().height(48.dp)
                        ) {
                            Text(
                                text = when {
                                    isConnected -> "Disconnect"
                                    isConnecting -> "Connecting..."
                                    else -> "Connect to PC"
                                },
                                color = TextPrimary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                // Status & Instructions Card
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            text = "Status & Diagnostics",
                            color = TextPrimary,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Live Status Indicator
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            val statusColor = when (connectionState) {
                                is ConnectionState.Connected -> AccentGreen
                                is ConnectionState.Connecting -> AccentAmber
                                is ConnectionState.Disconnected -> AccentRed
                                is ConnectionState.Error -> AccentRed
                            }
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(statusColor)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = when (val s = connectionState) {
                                    is ConnectionState.Connected -> "Connected to ${s.host}:${s.port} (${s.pingMs} ms)"
                                    is ConnectionState.Connecting -> "Connecting to server..."
                                    is ConnectionState.Disconnected -> "Not connected"
                                    is ConnectionState.Error -> s.message
                                },
                                color = TextSecondary,
                                fontSize = 13.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Packets Transmitted: $packetsSent",
                            color = TextSecondary,
                            fontSize = 12.sp
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "Quick Instructions:",
                            color = TextPrimary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "1. Run the PC Companion on your PC (python run_pc_companion.py)\n" +
                                   "2. Make sure phone and PC are connected to the same Wi-Fi.\n" +
                                   "3. Enter the IP displayed in the PC Companion terminal/dashboard.\n" +
                                   "4. Press Connect and return to the controller screen.",
                            color = TextSecondary,
                            fontSize = 11.sp,
                            lineHeight = 16.sp
                        )
                    }
                }
            }
        }
    }
}
