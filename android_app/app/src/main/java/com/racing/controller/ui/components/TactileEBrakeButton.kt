package com.racing.controller.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.racing.controller.ui.theme.AccentRed
import com.racing.controller.ui.theme.ControlBorderDark
import com.racing.controller.ui.theme.ControlBorderLight
import com.racing.controller.ui.theme.SurfaceControl
import com.racing.controller.ui.theme.SurfaceControlPressed
import com.racing.controller.ui.theme.TextPrimary
import com.racing.controller.ui.theme.TextSecondary

@Composable
fun TactileEBrakeButton(
    modifier: Modifier = Modifier,
    size: Dp = 64.dp,
    onPressChange: (Boolean) -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }

    val bgColor by animateColorAsState(
        targetValue = if (isPressed) SurfaceControlPressed else SurfaceControl,
        animationSpec = tween(durationMillis = 60),
        label = "eBrakeBg"
    )

    val borderColor by animateColorAsState(
        targetValue = if (isPressed) AccentRed else ControlBorderDark,
        animationSpec = tween(durationMillis = 60),
        label = "eBrakeBorder"
    )

    Box(
        modifier = modifier
            .size(size)
            .shadow(
                elevation = if (isPressed) 1.dp else 6.dp,
                shape = CircleShape,
                ambientColor = Color.Black,
                spotColor = Color.Black
            )
            .clip(CircleShape)
            .background(bgColor)
            .border(
                width = 1.5.dp,
                color = borderColor,
                shape = CircleShape
            )
            .pointerInput(Unit) {
                awaitEachGesture {
                    awaitFirstDown(requireUnconsumed = false)
                    isPressed = true
                    onPressChange(true)

                    waitForUpOrCancellation()
                    isPressed = false
                    onPressChange(false)
                }
            },
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Automotive Handbrake Icon: Circle with exclamation point (!)
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .border(
                        width = 1.5.dp,
                        color = if (isPressed) AccentRed else TextPrimary,
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "!",
                    color = if (isPressed) AccentRed else TextPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Text(
                text = "E-BRAKE",
                color = if (isPressed) AccentRed else TextSecondary,
                fontSize = 8.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp
            )
        }
    }
}
