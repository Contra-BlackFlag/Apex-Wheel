package com.racing.controller.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.racing.controller.model.RacingTeam
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

/**
 * Circular Racing Steering Wheel View.
 * Renders an authentic, circular GT/F1 racing wheel with sculpted circular grip rim,
 * 3-spoke carbon fiber hub, rotary dials, top-dead-center 12 o'clock stripe,
 * rotating central official team emblem, and live digital angle readout.
 */
@Composable
fun SteeringWheelView(
    rotationAngleDegrees: Float,
    modifier: Modifier = Modifier,
    diameter: Dp = 250.dp,
    team: RacingTeam = RacingTeam.RED_BULL,
    touchSteeringEnabled: Boolean = false,
    showTelemetryReadout: Boolean = true,
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
            val outerRadius = (size.minDimension / 2f) - 6.dp.toPx()
            val rimThickness = 22.dp.toPx()
            val innerRadius = outerRadius - rimThickness
            val hubRadius = outerRadius * 0.36f

            rotate(degrees = rotationAngleDegrees, pivot = center) {
                // 1. Semi-transparent background cockpit disc
                drawCircle(
                    color = Color(0x1A000000),
                    radius = outerRadius,
                    center = center
                )

                // 2. Three Racing Spokes (Left, Right, Bottom) connecting Rim to Center Hub
                val spokeColor = Color(0xFF1B202A)
                val spokeHighlight = team.secondaryColor.copy(alpha = 0.5f)

                // Left Spoke (Horizontal to 9 o'clock)
                val leftSpoke = Path().apply {
                    moveTo(center.x - hubRadius * 0.8f, center.y - 18.dp.toPx())
                    lineTo(center.x - innerRadius + 2.dp.toPx(), center.y - 24.dp.toPx())
                    lineTo(center.x - innerRadius + 2.dp.toPx(), center.y + 24.dp.toPx())
                    lineTo(center.x - hubRadius * 0.8f, center.y + 18.dp.toPx())
                    close()
                }
                drawPath(leftSpoke, color = spokeColor, style = Fill)
                drawPath(leftSpoke, color = spokeHighlight, style = Stroke(width = 1.dp.toPx()))

                // Right Spoke (Horizontal to 3 o'clock)
                val rightSpoke = Path().apply {
                    moveTo(center.x + hubRadius * 0.8f, center.y - 18.dp.toPx())
                    lineTo(center.x + innerRadius - 2.dp.toPx(), center.y - 24.dp.toPx())
                    lineTo(center.x + innerRadius - 2.dp.toPx(), center.y + 24.dp.toPx())
                    lineTo(center.x + hubRadius * 0.8f, center.y + 18.dp.toPx())
                    close()
                }
                drawPath(rightSpoke, color = spokeColor, style = Fill)
                drawPath(rightSpoke, color = spokeHighlight, style = Stroke(width = 1.dp.toPx()))

                // Bottom Spoke (Vertical to 6 o'clock)
                val bottomSpoke = Path().apply {
                    moveTo(center.x - 18.dp.toPx(), center.y + hubRadius * 0.8f)
                    lineTo(center.x - 22.dp.toPx(), center.y + innerRadius - 2.dp.toPx())
                    lineTo(center.x + 22.dp.toPx(), center.y + innerRadius - 2.dp.toPx())
                    lineTo(center.x + 18.dp.toPx(), center.y + hubRadius * 0.8f)
                    close()
                }
                drawPath(bottomSpoke, color = spokeColor, style = Fill)
                drawPath(bottomSpoke, color = spokeHighlight, style = Stroke(width = 1.dp.toPx()))

                // Spoke Rotary Dials (on left and right spokes)
                // Left Rotary Knob
                drawCircle(
                    color = Color(0xFF10141C),
                    radius = 9.dp.toPx(),
                    center = Offset(center.x - (hubRadius + innerRadius) / 2f, center.y)
                )
                drawCircle(
                    color = team.secondaryColor,
                    radius = 7.dp.toPx(),
                    center = Offset(center.x - (hubRadius + innerRadius) / 2f, center.y),
                    style = Stroke(width = 2.dp.toPx())
                )
                drawCircle(
                    color = team.secondaryColor,
                    radius = 2.5.dp.toPx(),
                    center = Offset(center.x - (hubRadius + innerRadius) / 2f, center.y)
                )

                // Right Rotary Knob
                drawCircle(
                    color = Color(0xFF10141C),
                    radius = 9.dp.toPx(),
                    center = Offset(center.x + (hubRadius + innerRadius) / 2f, center.y)
                )
                drawCircle(
                    color = team.accentColor,
                    radius = 7.dp.toPx(),
                    center = Offset(center.x + (hubRadius + innerRadius) / 2f, center.y),
                    style = Stroke(width = 2.dp.toPx())
                )
                drawCircle(
                    color = team.accentColor,
                    radius = 2.5.dp.toPx(),
                    center = Offset(center.x + (hubRadius + innerRadius) / 2f, center.y)
                )

                // 3. Circular Outer Rim (Perforated Racing Suede / Leather)
                val rimCenterRadius = (outerRadius + innerRadius) / 2f
                // Main rim ring
                drawCircle(
                    color = team.gripColor,
                    radius = rimCenterRadius,
                    center = center,
                    style = Stroke(width = rimThickness)
                )
                // Outer chrome edge line
                drawCircle(
                    color = Color(0xFF384050),
                    radius = outerRadius,
                    center = center,
                    style = Stroke(width = 1.8.dp.toPx())
                )
                // Inner rim bevel
                drawCircle(
                    color = team.secondaryColor.copy(alpha = 0.5f),
                    radius = innerRadius,
                    center = center,
                    style = Stroke(width = 1.2.dp.toPx())
                )

                // Grip Seam Stitching marks (at 10, 2, 8, 4 o'clock positions)
                val stitchAngles = listOf(45.0, 135.0, 225.0, 315.0)
                for (angleDeg in stitchAngles) {
                    val rad = Math.toRadians(angleDeg)
                    val r1 = innerRadius + 2.dp.toPx()
                    val r2 = outerRadius - 2.dp.toPx()
                    drawLine(
                        color = team.rimStripeColor,
                        start = Offset(center.x + (r1 * cos(rad)).toFloat(), center.y + (r1 * sin(rad)).toFloat()),
                        end = Offset(center.x + (r2 * cos(rad)).toFloat(), center.y + (r2 * sin(rad)).toFloat()),
                        strokeWidth = 2.dp.toPx(),
                        cap = StrokeCap.Round
                    )
                }

                // 4. Top-Dead-Center 12 O'Clock Racing Stripe (on circular top rim)
                drawArc(
                    color = team.rimStripeColor,
                    startAngle = -98f,
                    sweepAngle = 16f,
                    useCenter = false,
                    topLeft = Offset(center.x - rimCenterRadius, center.y - rimCenterRadius),
                    size = Size(rimCenterRadius * 2f, rimCenterRadius * 2f),
                    style = Stroke(width = rimThickness)
                )

                // 5. Central Hub Plate (Circular)
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0xFF262C3A), team.surfaceColor),
                        center = center,
                        radius = hubRadius
                    ),
                    radius = hubRadius,
                    center = center
                )
                // Hub metallic bezel ring
                drawCircle(
                    brush = Brush.sweepGradient(
                        colors = listOf(team.secondaryColor, team.accentColor, team.secondaryColor),
                        center = center
                    ),
                    radius = hubRadius,
                    center = center,
                    style = Stroke(width = 2.5.dp.toPx())
                )
                drawCircle(
                    color = Color(0x33000000),
                    radius = hubRadius - 2.dp.toPx(),
                    center = center,
                    style = Stroke(width = 1.dp.toPx())
                )
            }
        }

        // 6. Central Hub Official Team Logo
        // Positioned dead center and spins together with the circular wheel
        val logoSize = diameter * 0.30f
        Box(
            modifier = Modifier.size(logoSize),
            contentAlignment = Alignment.Center
        ) {
            TeamLogoBadge(
                team = team,
                size = logoSize
            )
        }
    }
}

/**
 * Dedicated Cockpit Steering Degree Telemetry Badge.
 * Positioned cleanly outside the rotating wheel so it never interferes with steering visual.
 */
@Composable
fun SteeringTelemetryBadge(
    steeringAngle: Float,
    team: RacingTeam,
    modifier: Modifier = Modifier
) {
    val isCentered = abs(steeringAngle) < 1.5f
    val accentColor = if (isCentered) Color(0xFF00FF66) else team.secondaryColor
    val angleFormatted = "${if (steeringAngle >= 0) "+" else ""}${String.format("%.1f", steeringAngle)}°"

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xD90D1118))
            .border(
                1.dp,
                accentColor.copy(alpha = 0.6f),
                RoundedCornerShape(12.dp)
            )
            .padding(horizontal = 10.dp, vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Glowing status indicator dot (vivid green when centered/deadzone, team accent when turning)
        Box(
            modifier = Modifier
                .size(6.dp)
                .clip(CircleShape)
                .background(accentColor)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = "STEER",
            color = Color(0xFF8B949E),
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = angleFormatted,
            color = if (isCentered) Color(0xFF00FF66) else Color.White,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
        )
    }
}


