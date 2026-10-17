package com.racing.controller.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.racing.controller.ui.theme.WheelCenterDotColor
import com.racing.controller.ui.theme.WheelCrosshairColor
import com.racing.controller.ui.theme.WheelStrokeColor
import kotlin.math.PI
import kotlin.math.atan2

@Composable
fun SteeringWheelView(
    rotationAngleDegrees: Float,
    modifier: Modifier = Modifier,
    diameter: Dp = 250.dp,
    touchSteeringEnabled: Boolean = false,
    onTouchSteer: (Float) -> Unit = {}
) {
    Box(
        modifier = modifier
            .size(diameter)
            .pointerInput(touchSteeringEnabled) {
                if (touchSteeringEnabled) {
                    detectDragGestures(
                        onDragStart = { offset ->
                            val center = Offset(size.width / 2f, size.height / 2f)
                            val dx = offset.x - center.x
                            val dy = offset.y - center.y
                            val angleRad = atan2(dx, -dy)
                            val normalized = (angleRad / (PI.toFloat() / 2f)).coerceIn(-1f, 1f)
                            onTouchSteer(normalized)
                        },
                        onDragEnd = {
                            onTouchSteer(0f)
                        },
                        onDragCancel = {
                            onTouchSteer(0f)
                        },
                        onDrag = { change, _ ->
                            val center = Offset(size.width / 2f, size.height / 2f)
                            val dx = change.position.x - center.x
                            val dy = change.position.y - center.y
                            val angleRad = atan2(dx, -dy)
                            val normalized = (angleRad / (PI.toFloat() / 2f)).coerceIn(-1f, 1f)
                            onTouchSteer(normalized)
                        }
                    )
                }
            },
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val radius = (size.minDimension / 2f) - 6.dp.toPx()

            // Subtle background fill
            drawCircle(
                color = Color(0x18FFFFFF),
                radius = radius,
                center = center
            )

            // Outer ring stroke
            drawCircle(
                color = WheelStrokeColor,
                radius = radius,
                center = center,
                style = Stroke(width = 2.dp.toPx())
            )

            // Rotate crosshairs with steering angle
            rotate(degrees = rotationAngleDegrees, pivot = center) {
                // Horizontal crosshair line
                drawLine(
                    color = WheelCrosshairColor,
                    start = Offset(center.x - radius, center.y),
                    end = Offset(center.x + radius, center.y),
                    strokeWidth = 2.dp.toPx(),
                    cap = StrokeCap.Round
                )

                // Vertical crosshair line
                drawLine(
                    color = WheelCrosshairColor,
                    start = Offset(center.x, center.y - radius),
                    end = Offset(center.x, center.y + radius),
                    strokeWidth = 2.dp.toPx(),
                    cap = StrokeCap.Round
                )

                // Center hub indicator dot
                drawCircle(
                    color = WheelCenterDotColor,
                    radius = 5.dp.toPx(),
                    center = center
                )
            }
        }
    }
}
