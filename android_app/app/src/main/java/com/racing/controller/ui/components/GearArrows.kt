package com.racing.controller.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.racing.controller.ui.theme.SurfaceControl

/**
 * Expressively reactive gear shifter arrows (Gear Up & Down).
 * Features spring-physics compression, directional physical shift nudge,
 * and high-contrast illuminated neon chevron borders on press.
 */
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

    val activeGlow = if (isPointingUp) Color(0xFFFFD700) else Color(0xFFFF6D00)
    val activeFill = if (isPointingUp) Color(0xFF423508) else Color(0xFF401B05)

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.88f else 1.0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "chevronScale"
    )

    val translationY by animateFloatAsState(
        targetValue = if (isPressed) (if (isPointingUp) -12f else 12f) else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "chevronTranslationY"
    )

    val fillColor by animateColorAsState(
        targetValue = if (isPressed) activeFill else SurfaceControl,
        animationSpec = tween(durationMillis = 80),
        label = "chevronFill"
    )

    val strokeColor by animateColorAsState(
        targetValue = if (isPressed) activeGlow else Color(0xFF454D5E),
        animationSpec = tween(durationMillis = 80),
        label = "chevronStroke"
    )

    val strokeWidth by animateFloatAsState(
        targetValue = if (isPressed) 3.5f else 1.5f,
        animationSpec = tween(durationMillis = 80),
        label = "chevronStrokeWidth"
    )

    Box(
        modifier = Modifier
            .size(width = width, height = height)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
                this.translationY = translationY
            }
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
            drawPath(path = path, color = strokeColor, style = Stroke(width = strokeWidth.dp.toPx()))

            // Inner chevron highlight ridge
            val innerPath = Path()
            if (isPointingUp) {
                innerPath.moveTo(w / 2f, depth * 0.4f)
                innerPath.lineTo(w * 0.78f, depth * 0.9f)
                innerPath.moveTo(w / 2f, depth * 0.4f)
                innerPath.lineTo(w * 0.22f, depth * 0.9f)
            } else {
                innerPath.moveTo(w / 2f, h - depth * 0.4f)
                innerPath.lineTo(w * 0.78f, h - depth * 0.9f)
                innerPath.moveTo(w / 2f, h - depth * 0.4f)
                innerPath.lineTo(w * 0.22f, h - depth * 0.9f)
            }
            drawPath(
                path = innerPath,
                color = if (isPressed) Color.White else Color(0x33FFFFFF),
                style = Stroke(width = 2.dp.toPx())
            )
        }
    }
}
