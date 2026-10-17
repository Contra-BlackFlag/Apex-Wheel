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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.racing.controller.ui.theme.SurfaceControl

/**
 * Expressively reactive tactile pill button (Select & Start).
 * Features spring-physics compression, vibrant edge glow, and high-contrast tactile feedback.
 */
@Composable
fun TactilePillButton(
    text: String,
    modifier: Modifier = Modifier,
    width: Dp = 110.dp,
    height: Dp = 40.dp,
    onPressChange: (Boolean) -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }

    val isStart = text.equals("Start", ignoreCase = true)
    val activeGlow = if (isStart) Color(0xFFFFB300) else Color(0xFF47C7FC)
    val activeBgStart = if (isStart) Color(0xFF4A3406) else Color(0xFF0D324E)
    val activeBgEnd = if (isStart) Color(0xFF1E1502) else Color(0xFF04121F)

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.88f else 1.0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "pillScale"
    )

    val borderColor by animateColorAsState(
        targetValue = if (isPressed) activeGlow else Color(0xFF454D5E),
        animationSpec = tween(durationMillis = 80),
        label = "pillBorder"
    )

    val borderWidth by animateFloatAsState(
        targetValue = if (isPressed) 2.5f else 1.2f,
        animationSpec = tween(durationMillis = 80),
        label = "pillBorderWidth"
    )

    Box(
        modifier = modifier
            .size(width = width, height = height)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .shadow(
                elevation = if (isPressed) 12.dp else 4.dp,
                shape = CircleShape,
                ambientColor = if (isPressed) activeGlow else Color.Black,
                spotColor = if (isPressed) activeGlow else Color.Black
            )
            .clip(CircleShape)
            .background(
                if (isPressed) {
                    Brush.horizontalGradient(
                        colors = listOf(activeBgStart, activeBgEnd)
                    )
                } else {
                    Brush.horizontalGradient(
                        colors = listOf(Color(0xFF2C3240), SurfaceControl)
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
        Text(
            text = text,
            color = if (isPressed) Color.White else Color(0xFFE2E6F0),
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.5.sp
        )
    }
}
