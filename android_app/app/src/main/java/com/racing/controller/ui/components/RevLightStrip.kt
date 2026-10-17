package com.racing.controller.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.racing.controller.ui.theme.ControlBorderDark
import com.racing.controller.ui.theme.SurfaceDark

@Composable
fun RevLightStrip(
    throttle: Float,
    modifier: Modifier = Modifier,
    width: Dp = 230.dp,
    height: Dp = 14.dp,
    segmentCount: Int = 16
) {
    // Ultra-low latency animation with tight spring for instantaneous feedback
    val animatedThrottle by animateFloatAsState(
        targetValue = throttle.coerceIn(0f, 1f),
        animationSpec = spring(dampingRatio = 0.85f, stiffness = 1200f),
        label = "throttleAnimation"
    )

    // Compute dynamic glow color transitioning from Red -> Amber -> Vivid Green
    val activeColor = computeThrottleColor(animatedThrottle)
    val hasThrottle = animatedThrottle > 0.03f

    Box(
        modifier = modifier
            .width(width)
            .height(height)
            .shadow(
                elevation = if (hasThrottle) 10.dp else 2.dp,
                shape = RoundedCornerShape(7.dp),
                spotColor = if (hasThrottle) activeColor else Color.Transparent,
                ambientColor = if (hasThrottle) activeColor.copy(alpha = 0.6f) else Color.Transparent
            )
            .clip(RoundedCornerShape(7.dp))
            .background(SurfaceDark)
            .border(
                width = 1.dp,
                color = if (hasThrottle) activeColor.copy(alpha = 0.5f) else ControlBorderDark,
                shape = RoundedCornerShape(7.dp)
            )
            .padding(horizontal = 4.dp, vertical = 2.5.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.matchParentSize()) {
            val totalWidth = size.width
            val totalHeight = size.height
            val spacing = 2.5.dp.toPx()
            val totalSpacing = spacing * (segmentCount - 1)
            val segmentWidth = (totalWidth - totalSpacing) / segmentCount

            for (i in 0 until segmentCount) {
                val segmentFraction = ((animatedThrottle * segmentCount) - i).coerceIn(0f, 1f)
                val isLit = segmentFraction > 0f
                val xOffset = i * (segmentWidth + spacing)

                if (isLit) {
                    val alpha = 0.35f + (0.65f * segmentFraction)

                    // Draw glowing LED segment with progressive color
                    drawRoundRect(
                        color = activeColor.copy(alpha = alpha),
                        topLeft = Offset(xOffset, 0f),
                        size = Size(segmentWidth, totalHeight),
                        cornerRadius = CornerRadius(2.5.dp.toPx(), 2.5.dp.toPx())
                    )

                    // Top tactile gloss reflection
                    drawRoundRect(
                        color = Color.White.copy(alpha = 0.45f * segmentFraction),
                        topLeft = Offset(xOffset + 0.8.dp.toPx(), 0.8.dp.toPx()),
                        size = Size(segmentWidth - 1.6.dp.toPx(), totalHeight * 0.35f),
                        cornerRadius = CornerRadius(1.5.dp.toPx(), 1.5.dp.toPx())
                    )
                } else {
                    // Unlit dark LED socket with subtle metallic bezel
                    drawRoundRect(
                        color = Color(0xFF16181D),
                        topLeft = Offset(xOffset, 0f),
                        size = Size(segmentWidth, totalHeight),
                        cornerRadius = CornerRadius(2.5.dp.toPx(), 2.5.dp.toPx())
                    )
                }
            }
        }
    }
}

/**
 * Computes progressive color:
 * Starts at pure Red (low throttle/click), blends smoothly to Amber, and turns to vibrant
 * Racing Green at high/full accelerator.
 */
private fun computeThrottleColor(throttle: Float): Color {
    val t = throttle.coerceIn(0f, 1f)
    return when {
        t < 0.40f -> {
            // 0.0 -> 0.40: Pure Bright Red to Red-Orange
            val ratio = t / 0.40f
            lerpColor(Color(0xFFFF2222), Color(0xFFFF6D00), ratio)
        }
        t < 0.70f -> {
            // 0.40 -> 0.70: Red-Orange to Amber-Yellow
            val ratio = (t - 0.40f) / 0.30f
            lerpColor(Color(0xFFFF6D00), Color(0xFFFFD600), ratio)
        }
        else -> {
            // 0.70 -> 1.00: Amber-Yellow to Radiant Neon Green
            val ratio = (t - 0.70f) / 0.30f
            lerpColor(Color(0xFFFFD600), Color(0xFF00E676), ratio)
        }
    }
}

private fun lerpColor(c1: Color, c2: Color, fraction: Float): Color {
    val f = fraction.coerceIn(0f, 1f)
    return Color(
        red = c1.red + (c2.red - c1.red) * f,
        green = c1.green + (c2.green - c1.green) * f,
        blue = c1.blue + (c2.blue - c1.blue) * f,
        alpha = c1.alpha + (c2.alpha - c1.alpha) * f
    )
}
