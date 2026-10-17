package com.racing.controller.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.racing.controller.ui.theme.ControlBorderDark
import com.racing.controller.ui.theme.ControlBorderLight
import com.racing.controller.ui.theme.SurfaceControl
import com.racing.controller.ui.theme.SurfaceControlPressed

@Composable
fun GearArrows(
    modifier: Modifier = Modifier,
    width: Dp = 100.dp,
    height: Dp = 75.dp,
    onGearUpChange: (Boolean) -> Unit,
    onGearDownChange: (Boolean) -> Unit
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Up Chevron
        ChevronButton(
            isPointingUp = true,
            width = width,
            height = height,
            onPressChange = onGearUpChange
        )

        // Down Chevron
        ChevronButton(
            isPointingUp = false,
            width = width,
            height = height,
            onPressChange = onGearDownChange
        )
    }
}

@Composable
private fun ChevronButton(
    isPointingUp: Boolean,
    width: Dp,
    height: Dp,
    onPressChange: (Boolean) -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }

    val fillColor by animateColorAsState(
        targetValue = if (isPressed) SurfaceControlPressed else SurfaceControl,
        animationSpec = tween(durationMillis = 60),
        label = "chevronFill"
    )

    val strokeColor by animateColorAsState(
        targetValue = if (isPressed) ControlBorderLight else ControlBorderDark,
        animationSpec = tween(durationMillis = 60),
        label = "chevronStroke"
    )

    Box(
        modifier = Modifier
            .size(width = width, height = height)
            .pointerInput(Unit) {
                awaitEachGesture {
                    awaitFirstDown(requireUnconsumed = false)
                    isPressed = true
                    onPressChange(true)

                    waitForUpOrCancellation()
                    isPressed = false
                    onPressChange(false)
                }
            }
    ) {
        Canvas(modifier = Modifier.size(width = width, height = height)) {
            val w = size.width
            val h = size.height
            val depth = h * 0.40f

            val path = Path()
            if (isPointingUp) {
                // Peak at top center
                path.moveTo(w / 2f, 0f)
                path.lineTo(w, depth)
                path.lineTo(w, h)
                path.lineTo(w / 2f, h - depth)
                path.lineTo(0f, h)
                path.lineTo(0f, depth)
                path.close()
            } else {
                // Peak at bottom center
                path.moveTo(0f, 0f)
                path.lineTo(w / 2f, depth)
                path.lineTo(w, 0f)
                path.lineTo(w, h - depth)
                path.lineTo(w / 2f, h)
                path.lineTo(0f, h - depth)
                path.close()
            }

            drawPath(path = path, color = fillColor, style = Fill)
            drawPath(path = path, color = strokeColor, style = Stroke(width = 1.5.dp.toPx()))
        }
    }
}
