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
import androidx.compose.foundation.shape.RoundedCornerShape
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
 * Expressively reactive tactile clutch pedal button.
 * Features spring-physics compression, electric cyan illumination,
 * and high-contrast tactile feedback on press.
 */
@Composable
fun TactileClutchButton(
    modifier: Modifier = Modifier,
    width: Dp = 120.dp,
    height: Dp = 80.dp,
    buttonShape: com.racing.controller.model.ButtonShape = com.racing.controller.model.ButtonShape.CIRCLE,
    onValueChange: (Float) -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }

    val clutchShape = remember(buttonShape) {
        if (buttonShape == com.racing.controller.model.ButtonShape.CIRCLE) {
            RoundedCornerShape(
                topStart = 8.dp,
                topEnd = 8.dp,
                bottomStart = 36.dp,
                bottomEnd = 36.dp
            )
        } else {
            buttonShape.toComposeShape(20)
        }
    }

    val activeGlow = Color(0xFF00D2BE)
    val activeBgStart = Color(0xFF0A3E38)
    val activeBgEnd = Color(0xFF041917)

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.88f else 1.0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "clutchScale"
    )

    val borderColor by animateColorAsState(
        targetValue = if (isPressed) activeGlow else Color(0xFF454D5E),
        animationSpec = tween(durationMillis = 80),
        label = "clutchBorder"
    )

    val borderWidth by animateFloatAsState(
        targetValue = if (isPressed) 3.0f else 1.5f,
        animationSpec = tween(durationMillis = 80),
        label = "clutchBorderWidth"
    )

    Box(
        modifier = modifier
            .size(width = width, height = height)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .shadow(
                elevation = if (isPressed) 16.dp else 6.dp,
                shape = clutchShape,
                ambientColor = if (isPressed) activeGlow else Color.Black,
                spotColor = if (isPressed) activeGlow else Color.Black
            )
            .clip(clutchShape)
            .background(
                if (isPressed) {
                    Brush.radialGradient(
                        colors = listOf(activeBgStart, activeBgEnd),
                        radius = width.value * 1.5f
                    )
                } else {
                    Brush.radialGradient(
                        colors = listOf(Color(0xFF2E3544), SurfaceControl),
                        radius = width.value * 1.5f
                    )
                }
            )
            .border(
                width = borderWidth.dp,
                color = borderColor,
                shape = clutchShape
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
        // Horizontal tread grip ridges
        Box(
            modifier = Modifier
                .size(width = width * 0.72f, height = height * 0.65f)
                .border(
                    width = 1.dp,
                    color = if (isPressed) activeGlow.copy(alpha = 0.5f) else Color(0x22FFFFFF),
                    shape = RoundedCornerShape(
                        topStart = 6.dp,
                        topEnd = 6.dp,
                        bottomStart = 26.dp,
                        bottomEnd = 26.dp
                    )
                )
        )

        Text(
            text = "Clutch",
            color = if (isPressed) Color.White else Color(0xFFE2E6F0),
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.5.sp
        )
    }
}
