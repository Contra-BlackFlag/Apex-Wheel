package com.racing.controller.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.racing.controller.model.RacingTeam
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * Official F1 / Racing Team Logos rendered with sharp, high-precision vector paths
 * for steering wheel hubs, HUD badges, and theme selection cards.
 */
@Composable
fun TeamLogoBadge(
    team: RacingTeam,
    modifier: Modifier = Modifier,
    size: Dp = 48.dp
) {
    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        when (team) {
            RacingTeam.RED_BULL -> RedBullLogo(size = size)
            RacingTeam.FERRARI -> FerrariLogo(size = size)
            RacingTeam.MERCEDES -> MercedesLogo(size = size)
            RacingTeam.MCLAREN -> McLarenLogo(size = size)
            RacingTeam.ASTON_MARTIN -> AstonMartinLogo(size = size)
            RacingTeam.CLASSIC -> ClassicSimLogo(size = size)
        }
    }
}

/**
 * Oracle Red Bull Racing: Dual Charging Red Bulls & Golden Sun Disc.
 */
@Composable
fun RedBullLogo(
    modifier: Modifier = Modifier,
    size: Dp = 48.dp
) {
    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height
        val cx = w / 2f
        val cy = h / 2f

        // Central Golden Sun Disc
        val sunRadius = w * 0.22f
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color(0xFFFFEA00), Color(0xFFFF9100)),
                center = Offset(cx, cy),
                radius = sunRadius
            ),
            radius = sunRadius,
            center = Offset(cx, cy)
        )

        // Subtle Sun rays ring
        drawCircle(
            color = Color(0x66FFD700),
            radius = sunRadius + 2.dp.toPx(),
            center = Offset(cx, cy),
            style = Stroke(width = 1.5.dp.toPx())
        )

        val bullRed = Color(0xFFE10600)
        val bullDarkRed = Color(0xFF9E0400)

        // Left Charging Bull
        val leftBull = Path().apply {
            moveTo(cx - w * 0.05f, cy + h * 0.02f) // head butt center
            // Horn pointing right/up
            lineTo(cx - w * 0.02f, cy - h * 0.18f)
            lineTo(cx - w * 0.08f, cy - h * 0.12f)
            // Head & Neck
            lineTo(cx - w * 0.15f, cy - h * 0.16f)
            // Muscular Hump
            lineTo(cx - w * 0.28f, cy - h * 0.26f)
            // Back
            lineTo(cx - w * 0.42f, cy - h * 0.12f)
            // Rump & Tail arch
            lineTo(cx - w * 0.48f, cy - h * 0.02f)
            lineTo(cx - w * 0.45f, cy + h * 0.10f)
            // Hind leg
            lineTo(cx - w * 0.38f, cy + h * 0.28f)
            lineTo(cx - w * 0.32f, cy + h * 0.25f)
            lineTo(cx - w * 0.28f, cy + h * 0.10f)
            // Foreleg charging forward
            lineTo(cx - w * 0.18f, cy + h * 0.28f)
            lineTo(cx - w * 0.12f, cy + h * 0.24f)
            // Chest
            lineTo(cx - w * 0.12f, cy + h * 0.06f)
            close()
        }
        drawPath(leftBull, color = bullRed, style = Fill)
        drawPath(leftBull, color = bullDarkRed, style = Stroke(width = 1.dp.toPx()))

        // Right Charging Bull (Mirrored silhouette)
        val rightBull = Path().apply {
            moveTo(cx + w * 0.05f, cy + h * 0.02f)
            // Horn pointing left/up
            lineTo(cx + w * 0.02f, cy - h * 0.18f)
            lineTo(cx + w * 0.08f, cy - h * 0.12f)
            // Head & Neck
            lineTo(cx + w * 0.15f, cy - h * 0.16f)
            // Muscular Hump
            lineTo(cx + w * 0.28f, cy - h * 0.26f)
            // Back
            lineTo(cx + w * 0.42f, cy - h * 0.12f)
            // Rump & Tail arch
            lineTo(cx + w * 0.48f, cy - h * 0.02f)
            lineTo(cx + w * 0.45f, cy + h * 0.10f)
            // Hind leg
            lineTo(cx + w * 0.38f, cy + h * 0.28f)
            lineTo(cx + w * 0.32f, cy + h * 0.25f)
            lineTo(cx + w * 0.28f, cy + h * 0.10f)
            // Foreleg charging forward
            lineTo(cx + w * 0.18f, cy + h * 0.28f)
            lineTo(cx + w * 0.12f, cy + h * 0.24f)
            // Chest
            lineTo(cx + w * 0.12f, cy + h * 0.06f)
            close()
        }
        drawPath(rightBull, color = bullRed, style = Fill)
        drawPath(rightBull, color = bullDarkRed, style = Stroke(width = 1.dp.toPx()))

        // Bottom Oracle Blue accent bar
        drawRoundRect(
            color = Color(0xFF001A30),
            topLeft = Offset(cx - w * 0.38f, cy + h * 0.32f),
            size = Size(w * 0.76f, h * 0.10f),
            cornerRadius = CornerRadius(3.dp.toPx(), 3.dp.toPx())
        )
        // Red Bull Racing stripe in the bar
        drawRoundRect(
            color = Color(0xFFE10600),
            topLeft = Offset(cx - w * 0.36f, cy + h * 0.35f),
            size = Size(w * 0.72f, h * 0.04f),
            cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx())
        )
    }
}

/**
 * Scuderia Ferrari: The Modena Yellow Shield, Italian Tricolore, and Cavallino Rampante.
 */
@Composable
fun FerrariLogo(
    modifier: Modifier = Modifier,
    size: Dp = 48.dp
) {
    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height
        val cx = w / 2f

        // Ferrari Shield Contour
        val shield = Path().apply {
            moveTo(cx - w * 0.36f, h * 0.08f)
            lineTo(cx + w * 0.36f, h * 0.08f)
            // Straight side upper
            lineTo(cx + w * 0.36f, h * 0.52f)
            // Lower shield curve to bottom point
            cubicTo(
                cx + w * 0.36f, h * 0.76f,
                cx + w * 0.18f, h * 0.92f,
                cx, h * 0.98f
            )
            cubicTo(
                cx - w * 0.18f, h * 0.92f,
                cx - w * 0.36f, h * 0.76f,
                cx - w * 0.36f, h * 0.52f
            )
            close()
        }

        // Shield Body: Giallo Modena Canary Yellow
        drawPath(shield, color = Color(0xFFFFEB00), style = Fill)
        // Black outline border
        drawPath(shield, color = Color(0xFF111111), style = Stroke(width = 2.dp.toPx()))

        // Italian Tricolore stripes across top (Green, White, Red)
        val stripeH = h * 0.10f
        val stripeW = (w * 0.72f) / 3f
        val topY = h * 0.08f
        // Green
        drawRect(
            color = Color(0xFF009246),
            topLeft = Offset(cx - w * 0.36f, topY),
            size = Size(stripeW, stripeH)
        )
        // White
        drawRect(
            color = Color(0xFFFFFFFF),
            topLeft = Offset(cx - w * 0.36f + stripeW, topY),
            size = Size(stripeW, stripeH)
        )
        // Red
        drawRect(
            color = Color(0xFFCE2B37),
            topLeft = Offset(cx - w * 0.36f + stripeW * 2f, topY),
            size = Size(stripeW, stripeH)
        )
        // Separator line below flag
        drawLine(
            color = Color(0xFF111111),
            start = Offset(cx - w * 0.36f, topY + stripeH),
            end = Offset(cx + w * 0.36f, topY + stripeH),
            strokeWidth = 1.dp.toPx()
        )

        // Cavallino Rampante (Prancing Horse) Silhouette in Jet Black
        val horse = Path().apply {
            // Mane & Ears
            moveTo(cx + w * 0.04f, h * 0.24f)
            lineTo(cx + w * 0.08f, h * 0.20f)
            // Snout pointing left
            lineTo(cx - w * 0.08f, h * 0.22f)
            lineTo(cx - w * 0.04f, h * 0.27f)
            // Neck arch
            lineTo(cx - w * 0.02f, h * 0.32f)
            // Raised front left hoof
            lineTo(cx - w * 0.16f, h * 0.28f)
            lineTo(cx - w * 0.14f, h * 0.33f)
            // Raised front right hoof
            lineTo(cx - w * 0.12f, h * 0.40f)
            lineTo(cx - w * 0.18f, h * 0.43f)
            // Chest & belly
            lineTo(cx - w * 0.02f, h * 0.46f)
            lineTo(cx, h * 0.54f)
            // Left standing hind leg
            lineTo(cx - w * 0.06f, h * 0.68f)
            lineTo(cx - w * 0.02f, h * 0.72f)
            lineTo(cx + w * 0.02f, h * 0.62f)
            // Right standing hind leg
            lineTo(cx + w * 0.08f, h * 0.74f)
            lineTo(cx + w * 0.12f, h * 0.70f)
            // Flank & Rump
            lineTo(cx + w * 0.08f, h * 0.48f)
            // Swishing horse tail arching high
            lineTo(cx + w * 0.20f, h * 0.36f)
            lineTo(cx + w * 0.16f, h * 0.42f)
            lineTo(cx + w * 0.10f, h * 0.44f)
            // Back arch to neck
            lineTo(cx + w * 0.06f, h * 0.36f)
            close()
        }
        drawPath(horse, color = Color(0xFF111111), style = Fill)

        // "S F" letters indication at bottom
        drawCircle(
            color = Color(0xFF111111),
            radius = 1.8.dp.toPx(),
            center = Offset(cx - w * 0.14f, h * 0.82f)
        )
        drawCircle(
            color = Color(0xFF111111),
            radius = 1.8.dp.toPx(),
            center = Offset(cx + w * 0.14f, h * 0.82f)
        )
    }
}

/**
 * Mercedes-AMG Petronas: Precision 3-Pointed Star & Petronas Turquoise Ring.
 */
@Composable
fun MercedesLogo(
    modifier: Modifier = Modifier,
    size: Dp = 48.dp
) {
    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height
        val cx = w / 2f
        val cy = h / 2f
        val outerRadius = (w.coerceAtMost(h) / 2f) - 3.dp.toPx()

        // Outer Dark Obsidian Glass Base
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color(0xFF1A2326), Color(0xFF0A0F11)),
                center = Offset(cx, cy),
                radius = outerRadius
            ),
            radius = outerRadius,
            center = Offset(cx, cy)
        )

        // Petronas Cyan Glow Ring
        drawCircle(
            color = Color(0xFF00D2BE),
            radius = outerRadius,
            center = Offset(cx, cy),
            style = Stroke(width = 2.dp.toPx())
        )

        // Inner Brushed Chrome Ring
        val ringRadius = outerRadius * 0.84f
        drawCircle(
            color = Color(0xFFE0E5E8),
            radius = ringRadius,
            center = Offset(cx, cy),
            style = Stroke(width = 2.5.dp.toPx())
        )

        // 3-Pointed Star Points (At 90 deg, 210 deg, 330 deg)
        val starLength = ringRadius - 1.5.dp.toPx()
        val hubWidth = ringRadius * 0.16f

        val angles = listOf(-90.0, 30.0, 150.0)
        for (baseAngle in angles) {
            val rad = Math.toRadians(baseAngle)
            val radL = Math.toRadians(baseAngle - 120.0)
            val radR = Math.toRadians(baseAngle + 120.0)

            val tipX = cx + (starLength * cos(rad)).toFloat()
            val tipY = cy + (starLength * sin(rad)).toFloat()

            val leftBaseX = cx + (hubWidth * cos(radL)).toFloat()
            val leftBaseY = cy + (hubWidth * sin(radL)).toFloat()

            val rightBaseX = cx + (hubWidth * cos(radR)).toFloat()
            val rightBaseY = cy + (hubWidth * sin(radR)).toFloat()

            // Left facet (bright chrome)
            val facetLeft = Path().apply {
                moveTo(cx, cy)
                lineTo(leftBaseX, leftBaseY)
                lineTo(tipX, tipY)
                close()
            }
            drawPath(facetLeft, color = Color(0xFFFFFFFF), style = Fill)

            // Right facet (shaded chrome)
            val facetRight = Path().apply {
                moveTo(cx, cy)
                lineTo(rightBaseX, rightBaseY)
                lineTo(tipX, tipY)
                close()
            }
            drawPath(facetRight, color = Color(0xFF9AA0A6), style = Fill)
        }

        // Center hub cap
        drawCircle(
            color = Color(0xFFFFFFFF),
            radius = hubWidth * 0.8f,
            center = Offset(cx, cy)
        )
        drawCircle(
            color = Color(0xFF00D2BE),
            radius = hubWidth * 0.4f,
            center = Offset(cx, cy)
        )
    }
}

/**
 * McLaren Racing: Papaya Orange Dynamic Speedmark Swoosh & Carbon Bezel.
 */
@Composable
fun McLarenLogo(
    modifier: Modifier = Modifier,
    size: Dp = 48.dp
) {
    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height
        val cx = w / 2f
        val cy = h / 2f

        // Carbon backing pill
        drawRoundRect(
            brush = Brush.linearGradient(
                colors = listOf(Color(0xFF26190B), Color(0xFF140D05)),
                start = Offset(0f, 0f),
                end = Offset(w, h)
            ),
            topLeft = Offset(w * 0.06f, h * 0.18f),
            size = Size(w * 0.88f, h * 0.64f),
            cornerRadius = CornerRadius(10.dp.toPx(), 10.dp.toPx())
        )

        // McLaren Speedmark Kiwi Swoosh (Iconic aerodynamic crescent boomerang)
        val speedmark = Path().apply {
            moveTo(cx + w * 0.32f, cy - h * 0.16f) // sharp top point
            // Outer sweeping curve to bottom left
            cubicTo(
                cx + w * 0.22f, cy - h * 0.28f,
                cx - w * 0.24f, cy - h * 0.22f,
                cx - w * 0.34f, cy + h * 0.08f
            )
            // Bottom hook point
            lineTo(cx - w * 0.32f, cy + h * 0.16f)
            // Inner aerodynamic scoop curve returning to tip
            cubicTo(
                cx - w * 0.18f, cy - h * 0.06f,
                cx + w * 0.12f, cy - h * 0.08f,
                cx + w * 0.32f, cy - h * 0.16f
            )
            close()
        }

        // Fill with McLaren Signature Papaya Glow
        drawPath(
            speedmark,
            brush = Brush.linearGradient(
                colors = listOf(Color(0xFFFF8000), Color(0xFFFF5500)),
                start = Offset(cx - w * 0.3f, cy),
                end = Offset(cx + w * 0.3f, cy)
            ),
            style = Fill
        )

        // Electric Blue Accent Line
        drawLine(
            color = Color(0xFF47C7FC),
            start = Offset(cx - w * 0.28f, cy + h * 0.22f),
            end = Offset(cx + w * 0.28f, cy + h * 0.22f),
            strokeWidth = 2.dp.toPx(),
            cap = StrokeCap.Round
        )
    }
}

/**
 * Aston Martin: British Racing Green Wings Emblem.
 */
@Composable
fun AstonMartinLogo(
    modifier: Modifier = Modifier,
    size: Dp = 48.dp
) {
    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height
        val cx = w / 2f
        val cy = h / 2f

        // Wide Aerodynamic Spread Wings
        val wings = Path().apply {
            moveTo(cx - w * 0.46f, cy - h * 0.08f)
            // Upper feathered wing line
            lineTo(cx - w * 0.18f, cy - h * 0.14f)
            lineTo(cx, cy - h * 0.08f)
            lineTo(cx + w * 0.18f, cy - h * 0.14f)
            lineTo(cx + w * 0.46f, cy - h * 0.08f)
            // Wing tip drops
            lineTo(cx + w * 0.44f, cy + h * 0.04f)
            lineTo(cx + w * 0.22f, cy + h * 0.16f)
            // Lower center curve
            lineTo(cx, cy + h * 0.22f)
            lineTo(cx - w * 0.22f, cy + h * 0.16f)
            lineTo(cx - w * 0.44f, cy + h * 0.04f)
            close()
        }

        // Silver / White Wing Background
        drawPath(wings, color = Color(0xFFE8ECEB), style = Fill)
        drawPath(wings, color = Color(0xFFCEDC00), style = Stroke(width = 1.dp.toPx()))

        // Feather striations (horizontal motorsport lines)
        for (i in -3..3) {
            if (i == 0) continue
            val offsetRatio = i / 4f
            val x = cx + (w * 0.32f * offsetRatio)
            drawLine(
                color = Color(0xFF6B7280),
                start = Offset(x, cy - h * 0.04f),
                end = Offset(x, cy + h * 0.10f),
                strokeWidth = 1.dp.toPx()
            )
        }

        // Center British Racing Green Badge Shield
        val centerBadge = Path().apply {
            moveTo(cx - w * 0.22f, cy - h * 0.06f)
            lineTo(cx + w * 0.22f, cy - h * 0.06f)
            lineTo(cx + w * 0.20f, cy + h * 0.10f)
            lineTo(cx, cy + h * 0.16f)
            lineTo(cx - w * 0.20f, cy + h * 0.10f)
            close()
        }
        drawPath(centerBadge, color = Color(0xFF00594F), style = Fill)
        drawPath(centerBadge, color = Color(0xFFCEDC00), style = Stroke(width = 1.2.dp.toPx()))

        // Lime horizontal accent bar in badge
        drawLine(
            color = Color(0xFFCEDC00),
            start = Offset(cx - w * 0.14f, cy + h * 0.02f),
            end = Offset(cx + w * 0.14f, cy + h * 0.02f),
            strokeWidth = 1.5.dp.toPx()
        )
    }
}

/**
 * Classic Sim Pro: Carbon & Checkered Flag Telemetry Emblem.
 */
@Composable
fun ClassicSimLogo(
    modifier: Modifier = Modifier,
    size: Dp = 48.dp
) {
    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height
        val cx = w / 2f
        val cy = h / 2f
        val radius = (w.coerceAtMost(h) / 2f) - 3.dp.toPx()

        // Outer Dark Carbon Dial
        drawCircle(
            color = Color(0xFF1E2026),
            radius = radius,
            center = Offset(cx, cy)
        )
        drawCircle(
            color = Color(0xFFE53935),
            radius = radius,
            center = Offset(cx, cy),
            style = Stroke(width = 2.dp.toPx())
        )

        // Precision Crosshairs
        drawLine(
            color = Color(0xFF8F939E),
            start = Offset(cx - radius * 0.7f, cy),
            end = Offset(cx + radius * 0.7f, cy),
            strokeWidth = 1.5.dp.toPx(),
            cap = StrokeCap.Round
        )
        drawLine(
            color = Color(0xFF8F939E),
            start = Offset(cx, cy - radius * 0.7f),
            end = Offset(cx, cy + radius * 0.7f),
            strokeWidth = 1.5.dp.toPx(),
            cap = StrokeCap.Round
        )

        // Center Red Racing Bullseye
        drawCircle(
            color = Color(0xFFE53935),
            radius = radius * 0.28f,
            center = Offset(cx, cy)
        )
        drawCircle(
            color = Color(0xFFFFFFFF),
            radius = radius * 0.12f,
            center = Offset(cx, cy)
        )
    }
}
