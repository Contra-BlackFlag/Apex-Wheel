package com.racing.controller.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.racing.controller.ui.theme.AccentRed
import com.racing.controller.ui.theme.SurfaceControl

/**
 * Expressively reactive tactile E-Brake / Handbrake button.
 * Features spring-physics compression, fierce neon crimson radiance bloom,
 * and high-contrast illuminated automotive brake glyph on press.
 */
@Composable
fun TactileEBrakeButton(
    modifier: Modifier = Modifier,
    size: Dp = 64.dp,
    onPressChange: (Boolean) -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }

    val activeGlow = Color(0xFFFF1744)
    val activeBgStart = Color(0xFF6B0818)
    val activeBgEnd = Color(0xFF2E030A)

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.88f else 1.0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "eBrakeScale"
    )

    val borderColor by animateColorAsState(
        targetValue = if (isPressed) activeGlow else Color(0xFF454D5E),
        animationSpec = tween(durationMillis = 80),
        label = "eBrakeBorder"
    )

    val borderWidth by animateFloatAsState(
        targetValue = if (isPressed) 3.0f else 1.5f,
        animationSpec = tween(durationMillis = 80),
        label = "eBrakeBorderWidth"
    )

    Box(
        modifier = modifier
            .size(size)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .shadow(
                elevation = if (isPressed) 14.dp else 5.dp,
                shape = CircleShape,
                ambientColor = if (isPressed) activeGlow else Color.Black,
                spotColor = if (isPressed) activeGlow else Color.Black
            )
            .clip(CircleShape)
            .background(
                if (isPressed) {
                    Brush.radialGradient(
                        colors = listOf(activeBgStart, activeBgEnd),
                        radius = size.value * 1.5f
                    )
                } else {
                    Brush.radialGradient(
                        colors = listOf(Color(0xFF2C3240), SurfaceControl),
                        radius = size.value * 1.5f
                    )
                }
            )
            .border(
                width = borderWidth.dp,
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
                        width = if (isPressed) 2.dp else 1.5.dp,
                        color = if (isPressed) Color.White else AccentRed,
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "!",
                    color = if (isPressed) Color.White else AccentRed,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Black
                )
            }

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = "EBRAKE",
                color = if (isPressed) Color.White else Color(0xFFC0C6D4),
                fontSize = 8.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp
            )
        }
    }
}
