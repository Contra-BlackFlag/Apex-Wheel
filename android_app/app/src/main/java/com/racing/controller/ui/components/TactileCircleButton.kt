package com.racing.controller.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.layout.Box
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
import com.racing.controller.ui.theme.ControlBorderDark
import com.racing.controller.ui.theme.ControlBorderLight
import com.racing.controller.ui.theme.SurfaceControl
import com.racing.controller.ui.theme.SurfaceControlPressed
import com.racing.controller.ui.theme.TextPrimary

@Composable
fun TactileCircleButton(
    text: String,
    modifier: Modifier = Modifier,
    size: Dp = 100.dp,
    onValueChange: (Float) -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }

    val bgColor by animateColorAsState(
        targetValue = if (isPressed) SurfaceControlPressed else SurfaceControl,
        animationSpec = tween(durationMillis = 60),
        label = "bgColor"
    )

    val borderColor by animateColorAsState(
        targetValue = if (isPressed) ControlBorderLight else ControlBorderDark,
        animationSpec = tween(durationMillis = 60),
        label = "borderColor"
    )

    Box(
        modifier = modifier
            .size(size)
            .shadow(
                elevation = if (isPressed) 2.dp else 8.dp,
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
                    onValueChange(1.0f)

                    waitForUpOrCancellation()
                    isPressed = false
                    onValueChange(0.0f)
                }
            },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = if (isPressed) Color.White else TextPrimary,
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium
        )
    }
}
