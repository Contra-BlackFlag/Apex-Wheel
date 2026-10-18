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
 * Expressively reactive tactile circular button (Brake & Accelerator).
 * Features spring-physics scale compression, intense neon radiance glow,
 * radial color bloom, and illuminated borders on press.
 */
@Composable
fun TactileCircleButton(
    text: String,
    modifier: Modifier = Modifier,
    size: Dp = 100.dp,
    buttonShape: com.racing.controller.model.ButtonShape = com.racing.controller.model.ButtonShape.CIRCLE,
    onValueChange: (Float) -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }
    val composeShape = remember(buttonShape) { buttonShape.toComposeShape(24) }

    val isBrake = text.equals("Brake", ignoreCase = true)
    val activeGlowColor = if (isBrake) Color(0xFFE8002D) else Color(0xFF00E676)
    val activeBgStart = if (isBrake) Color(0xFF5A0814) else Color(0xFF064E24)
    val activeBgEnd = if (isBrake) Color(0xFF260408) else Color(0xFF021C0D)

    // Bouncy spring scale compression
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.88f else 1.0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "circleScale"
    )

    val borderColor by animateColorAsState(
        targetValue = if (isPressed) activeGlowColor else Color(0xFF454D5E),
        animationSpec = tween(durationMillis = 80),
        label = "circleBorder"
    )

    val borderWidth by animateFloatAsState(
        targetValue = if (isPressed) 3.5f else 1.5f,
        animationSpec = tween(durationMillis = 80),
        label = "circleBorderWidth"
    )

    Box(
        modifier = modifier
            .size(size)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .shadow(
                elevation = if (isPressed) 16.dp else 6.dp,
                shape = composeShape,
                ambientColor = if (isPressed) activeGlowColor else Color.Black,
                spotColor = if (isPressed) activeGlowColor else Color.Black
            )
            .clip(composeShape)
            .background(
                if (isPressed) {
                    Brush.radialGradient(
                        colors = listOf(activeBgStart, activeBgEnd),
                        radius = size.value * 1.5f
                    )
                } else {
                    Brush.radialGradient(
                        colors = listOf(Color(0xFF2E3544), SurfaceControl),
                        radius = size.value * 1.5f
                    )
                }
            )
            .border(
                width = borderWidth.dp,
                color = borderColor,
                shape = composeShape
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
        // Inner concentric highlight ring
        Box(
            modifier = Modifier
                .size(size * 0.76f)
                .border(
                    width = 1.dp,
                    color = if (isPressed) activeGlowColor.copy(alpha = 0.6f) else Color(0x22FFFFFF),
                    shape = CircleShape
                )
        )

        Text(
            text = text,
            color = if (isPressed) Color.White else Color(0xFFE2E6F0),
            fontSize = (size.value * 0.17f).sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.5.sp
        )
    }
}
