package com.racing.controller.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Usb
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.racing.controller.model.ConnectionState
import com.racing.controller.ui.theme.AccentAmber
import com.racing.controller.ui.theme.AccentGreen
import com.racing.controller.ui.theme.AccentRed
import com.racing.controller.ui.theme.TextPrimary
import com.racing.controller.ui.theme.TextSecondary

import androidx.compose.foundation.border
import androidx.compose.ui.text.font.FontWeight
import com.racing.controller.model.RacingTeam

@Composable
fun TopControlBar(
    connectionState: ConnectionState,
    team: RacingTeam = RacingTeam.RED_BULL,
    onTeamClick: (() -> Unit)? = null,
    onCalibrateClick: () -> Unit,
    onConnectionClick: () -> Unit,
    onSettingsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        // Left side: Connection status pill and Active F1 Team Badge
        Row(
            modifier = Modifier.align(Alignment.CenterStart),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val isUsb = if (connectionState is ConnectionState.Connected) {
                val h = connectionState.host.lowercase()
                h == "127.0.0.1" || h == "localhost" || h.startsWith("192.168.42.")
            } else false

            val (statusColor, statusText) = when (connectionState) {
                is ConnectionState.Connected -> {
                    val pingStr = if (isUsb && connectionState.pingMs <= 1) "<1ms" else "${connectionState.pingMs}ms"
                    val label = if (isUsb) "USB $pingStr" else "Wi-Fi $pingStr"
                    Pair(AccentGreen, label)
                }
                is ConnectionState.Connecting -> Pair(AccentAmber, "Connecting...")
                is ConnectionState.Disconnected -> Pair(AccentRed, "Disconnected")
                is ConnectionState.Error -> Pair(AccentRed, "Error")
            }

            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0x33000000))
                    .clickable { onConnectionClick() }
                    .padding(horizontal = 10.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(statusColor)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = statusText,
                    color = TextSecondary,
                    fontSize = 11.sp
                )
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    imageVector = when {
                        connectionState !is ConnectionState.Connected -> Icons.Default.WifiOff
                        isUsb -> Icons.Default.Usb
                        else -> Icons.Default.Wifi
                    },
                    contentDescription = "Connection",
                    tint = statusColor,
                    modifier = Modifier.size(13.dp)
                )
            }

            // Active F1 Team Badge Pill with real logo
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(team.primaryColor.copy(alpha = 0.85f))
                    .border(1.dp, team.accentColor.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                    .then(if (onTeamClick != null) Modifier.clickable { onTeamClick() } else Modifier)
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TeamLogoBadge(team = team, size = 16.dp)
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = team.shortName,
                    color = Color.White,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Horizontally Center: Calibrate Button
        Row(
            modifier = Modifier
                .align(Alignment.Center)
                .clip(RoundedCornerShape(14.dp))
                .background(Color(0x22FFFFFF))
                .clickable { onCalibrateClick() }
                .padding(horizontal = 12.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = "Calibrate",
                tint = TextPrimary,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(5.dp))
            Text(
                text = "Calibrate",
                color = TextPrimary,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold
            )
        }

        // Right side: Settings Button
        IconButton(
            onClick = onSettingsClick,
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .size(32.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = "Settings",
                tint = TextSecondary,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}
