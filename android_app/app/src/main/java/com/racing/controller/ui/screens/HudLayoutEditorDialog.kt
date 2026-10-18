package com.racing.controller.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CenterFocusStrong
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FormatShapes
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.racing.controller.model.ButtonShape
import com.racing.controller.model.ControllerSettings
import com.racing.controller.model.HudElementConfig
import com.racing.controller.model.HudElementId
import com.racing.controller.model.HudLayoutProfile
import com.racing.controller.model.RacingTeam
import com.racing.controller.ui.components.GearArrows
import com.racing.controller.ui.components.RevLightStrip
import com.racing.controller.ui.components.SteeringTelemetryBadge
import com.racing.controller.ui.components.SteeringWheelView
import com.racing.controller.ui.components.TactileCircleButton
import com.racing.controller.ui.components.TactileClutchButton
import com.racing.controller.ui.components.TactileEBrakeButton
import com.racing.controller.ui.components.TactilePillButton
import com.racing.controller.ui.theme.AccentGreen
import com.racing.controller.ui.theme.AccentRed
import com.racing.controller.ui.theme.SurfaceControl
import com.racing.controller.ui.theme.SurfaceDark
import com.racing.controller.ui.theme.TextPrimary
import com.racing.controller.ui.theme.TextSecondary
import kotlin.math.roundToInt

/**
 * Isolated mutable editor state for each element.
 * Modifying x or y only invalidates the layout offset lambda of this single widget,
 * causing ZERO recompositions of the rest of the UI during drag gestures.
 */
private class ElementEditorState(
    val id: String,
    val name: String,
    initialX: Float,
    initialY: Float,
    initialScale: Float,
    initialOpacity: Float,
    initialVisible: Boolean
) {
    var x by mutableFloatStateOf(initialX)
    var y by mutableFloatStateOf(initialY)
    var scale by mutableFloatStateOf(initialScale)
    var opacity by mutableFloatStateOf(initialOpacity)
    var visible by mutableStateOf(initialVisible)

    fun applyConfig(config: HudElementConfig) {
        x = config.xNormalized
        y = config.yNormalized
        scale = config.scale
        opacity = config.opacity
        visible = config.visible
    }

    fun toConfig(): HudElementConfig = HudElementConfig(
        id = id,
        name = name,
        xNormalized = x,
        yNormalized = y,
        scale = scale,
        opacity = opacity,
        visible = visible
    )
}

/**
 * Standard base dimensions (width, height) for each HUD control.
 */
private fun getElementBaseSize(id: String): Pair<Dp, Dp> = when (id) {
    HudElementId.BRAKE -> 110.dp to 110.dp
    HudElementId.THROTTLE -> 110.dp to 110.dp
    HudElementId.CLUTCH -> 130.dp to 84.dp
    HudElementId.GEAR_SHIFTER -> 100.dp to 60.dp
    HudElementId.STEERING_WHEEL -> 220.dp to 220.dp
    HudElementId.HANDBRAKE -> 62.dp to 62.dp
    HudElementId.REV_LIGHTS -> 240.dp to 16.dp
    HudElementId.TELEMETRY_BADGE -> 110.dp to 30.dp
    HudElementId.SELECT_BTN -> 118.dp to 38.dp
    HudElementId.START_BTN -> 118.dp to 38.dp
    else -> 100.dp to 100.dp
}

/**
 * Fast hit testing to identify which control was touched.
 */
private fun findHitElement(
    touchX: Float,
    touchY: Float,
    states: Collection<ElementEditorState>,
    selectedId: String?,
    canvasW: Float,
    canvasH: Float,
    density: Density
): String? {
    // Check currently selected element first
    if (selectedId != null) {
        val selected = states.find { it.id == selectedId }
        if (selected != null) {
            val (wDp, hDp) = getElementBaseSize(selected.id)
            val halfW = with(density) { (wDp * selected.scale).toPx() / 2f + 16.dp.toPx() }
            val halfH = with(density) { (hDp * selected.scale).toPx() / 2f + 16.dp.toPx() }
            val cx = selected.x * canvasW
            val cy = selected.y * canvasH
            if (touchX in (cx - halfW)..(cx + halfW) && touchY in (cy - halfH)..(cy + halfH)) {
                return selectedId
            }
        }
    }

    // Check all elements (topmost first)
    for (state in states.reversed()) {
        val (wDp, hDp) = getElementBaseSize(state.id)
        val halfW = with(density) { (wDp * state.scale).toPx() / 2f + 12.dp.toPx() }
        val halfH = with(density) { (hDp * state.scale).toPx() / 2f + 12.dp.toPx() }
        val cx = state.x * canvasW
        val cy = state.y * canvasH
        if (touchX in (cx - halfW)..(cx + halfW) && touchY in (cy - halfH)..(cy + halfH)) {
            return state.id
        }
    }
    return null
}

/**
 * Minimalist, Hardware-Accelerated 120Hz HUD Layout Editor.
 * Eliminates recomposition during dragging via layout-phase offset mutations.
 */
@Composable
fun HudLayoutEditorDialog(
    currentSettings: ControllerSettings,
    team: RacingTeam = RacingTeam.fromId(currentSettings.selectedTeam),
    onSaveLayout: (updatedSettings: ControllerSettings) -> Unit,
    onDismiss: () -> Unit
) {
    // Initial profile
    val initialProfile = remember {
        HudLayoutProfile.fromJson(currentSettings.hudLayoutJson)
    }

    // Create persistent per-element states
    val elementIds = remember {
        listOf(
            HudElementId.STEERING_WHEEL,
            HudElementId.REV_LIGHTS,
            HudElementId.TELEMETRY_BADGE,
            HudElementId.SELECT_BTN,
            HudElementId.START_BTN,
            HudElementId.BRAKE,
            HudElementId.THROTTLE,
            HudElementId.CLUTCH,
            HudElementId.GEAR_SHIFTER,
            HudElementId.HANDBRAKE
        )
    }

    val statesMap = remember {
        elementIds.associateWith { id ->
            val config = initialProfile.getConfig(id)
            ElementEditorState(
                id = id,
                name = config.name,
                initialX = config.xNormalized,
                initialY = config.yNormalized,
                initialScale = config.scale,
                initialOpacity = config.opacity,
                initialVisible = config.visible
            )
        }
    }

    var selectedShape by remember { mutableStateOf(ButtonShape.fromId(currentSettings.buttonShape)) }
    var selectedElementId by remember { mutableStateOf<String?>(null) }
    var showShapePicker by remember { mutableStateOf(false) }

    val haptic = LocalHapticFeedback.current

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Color(0xFF090D14)
        ) {
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxSize()
                    // Subtle minimal dot/line grid
                    .drawBehind {
                        val spacing = 48.dp.toPx()
                        val gridColor = Color(0x0C00E5FF)
                        var x = 0f
                        while (x <= size.width) {
                            drawLine(gridColor, Offset(x, 0f), Offset(x, size.height), 1f)
                            x += spacing
                        }
                        var y = 0f
                        while (y <= size.height) {
                            drawLine(gridColor, Offset(0f, y), Offset(size.width, y), 1f)
                            y += spacing
                        }
                    }
            ) {
                val canvasWidthPx = constraints.maxWidth.toFloat()
                val canvasHeightPx = constraints.maxHeight.toFloat()
                val density = LocalDensity.current

                // 1. SILKY-SMOOTH CANVAS DRAG GESTURE LISTENER
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(Unit) {
                            awaitEachGesture {
                                val down = awaitFirstDown(requireUnconsumed = false)
                                val touchPos = down.position

                                // Ignore touch if in top capsule or bottom dock
                                val inTopBar = touchPos.y < 48.dp.toPx()
                                val inBottomDock = selectedElementId != null && touchPos.y > (canvasHeightPx - 80.dp.toPx())

                                if (inTopBar || inBottomDock) return@awaitEachGesture

                                val hitId = findHitElement(
                                    touchX = touchPos.x,
                                    touchY = touchPos.y,
                                    states = statesMap.values,
                                    selectedId = selectedElementId,
                                    canvasW = canvasWidthPx,
                                    canvasH = canvasHeightPx,
                                    density = density
                                )

                                if (hitId != null) {
                                    down.consume()
                                    selectedElementId = hitId
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)

                                    val targetState = statesMap[hitId]!!
                                    val elCenterX = targetState.x * canvasWidthPx
                                    val elCenterY = targetState.y * canvasHeightPx
                                    val offsetX = touchPos.x - elCenterX
                                    val offsetY = touchPos.y - elCenterY

                                    while (true) {
                                        val event = awaitPointerEvent()
                                        val change = event.changes.firstOrNull { it.id == down.id } ?: break
                                        if (!change.pressed) break
                                        change.consume()

                                        val newCx = change.position.x - offsetX
                                        val newCy = change.position.y - offsetY

                                        // Mutates float directly; triggers ONLY the offset lambda!
                                        targetState.x = (newCx / canvasWidthPx).coerceIn(0.04f, 0.96f)
                                        targetState.y = (newCy / canvasHeightPx).coerceIn(0.06f, 0.94f)
                                    }
                                } else {
                                    selectedElementId = null
                                }
                            }
                        }
                ) {
                    // 2. RENDER WIDGETS WITH ZERO RECOMPOSITION OFFSET BINDINGS
                    for (id in elementIds) {
                        val state = statesMap[id] ?: continue
                        DraggableHudElement(
                            state = state,
                            buttonShape = selectedShape,
                            team = team,
                            canvasWidthPx = canvasWidthPx,
                            canvasHeightPx = canvasHeightPx,
                            isSelected = selectedElementId == id
                        )
                    }
                }

                // 3. MINIMALIST TOP CONTROLS CAPSULE
                Row(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 10.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color(0xE0101726))
                        .border(1.dp, Color(0x3300E5FF), RoundedCornerShape(20.dp))
                        .padding(horizontal = 12.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Title badge
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF00E5FF))
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "HUD",
                            color = Color(0xFF00E5FF),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black
                        )
                    }

                    // Minimal Preset buttons
                    MinimalPillButton(text = "Default") {
                        val def = HudLayoutProfile.defaultProfile()
                        statesMap.forEach { (k, v) -> v.applyConfig(def.getConfig(k)) }
                        selectedElementId = null
                    }
                    MinimalPillButton(text = "4-Claw") {
                        val claw = HudLayoutProfile.claw4FingerProfile()
                        statesMap.forEach { (k, v) -> v.applyConfig(claw.getConfig(k)) }
                        selectedElementId = null
                    }
                    MinimalPillButton(text = "Invert") {
                        val left = HudLayoutProfile.leftHandedProfile()
                        statesMap.forEach { (k, v) -> v.applyConfig(left.getConfig(k)) }
                        selectedElementId = null
                    }

                    // Shape button
                    IconButton(
                        onClick = { showShapePicker = !showShapePicker },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.FormatShapes,
                            contentDescription = "Shape",
                            tint = if (showShapePicker) Color(0xFF00E5FF) else TextSecondary,
                            modifier = Modifier.size(14.dp)
                        )
                    }

                    Box(modifier = Modifier.width(1.dp).height(14.dp).background(Color(0x33FFFFFF)))

                    // Reset all
                    IconButton(
                        onClick = {
                            val def = HudLayoutProfile.defaultProfile()
                            statesMap.forEach { (k, v) -> v.applyConfig(def.getConfig(k)) }
                            selectedElementId = null
                        },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.RestartAlt,
                            contentDescription = "Reset",
                            tint = AccentRed,
                            modifier = Modifier.size(15.dp)
                        )
                    }

                    // Exit
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Cancel",
                            tint = TextSecondary,
                            modifier = Modifier.size(14.dp)
                        )
                    }

                    // Save
                    IconButton(
                        onClick = {
                            val elements = statesMap.mapValues { it.value.toConfig() }
                            val profile = HudLayoutProfile(version = 1, elements = elements)
                            onSaveLayout(
                                currentSettings.copy(
                                    hudLayoutJson = profile.toJson(),
                                    buttonShape = selectedShape.id
                                )
                            )
                            onDismiss()
                        },
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(AccentGreen)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Save",
                            tint = Color.Black,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }

                // 4. SHAPE PICKER POPUP
                if (showShapePicker) {
                    Card(
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .offset(y = 52.dp),
                        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                        shape = RoundedCornerShape(10.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF00E5FF))
                    ) {
                        Row(
                            modifier = Modifier.padding(6.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            ButtonShape.entries.forEach { shape ->
                                val active = selectedShape == shape
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(if (active) Color(0x3300E5FF) else SurfaceControl)
                                        .clickable {
                                            selectedShape = shape
                                            showShapePicker = false
                                        }
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = shape.displayName.substringBefore(" "),
                                        color = if (active) Color(0xFF00E5FF) else TextSecondary,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }

                // 5. ULTRA-MINIMAL FLOATING INSPECTOR (Bottom Bar)
                selectedElementId?.let { id ->
                    val state = statesMap[id] ?: return@let

                    AnimatedVisibility(
                        visible = true,
                        enter = slideInVertically { it } + fadeIn(),
                        exit = slideOutVertically { it } + fadeOut(),
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 12.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(22.dp))
                                .background(Color(0xF00D1522))
                                .border(1.dp, Color(0x4400E5FF), RoundedCornerShape(22.dp))
                                .padding(horizontal = 14.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Element Label
                            Text(
                                text = state.name.uppercase(),
                                color = Color(0xFF00E5FF),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black
                            )

                            // Size Slider
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(text = "SIZE", color = TextSecondary, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                Slider(
                                    value = state.scale,
                                    onValueChange = { state.scale = it },
                                    valueRange = 0.5f..1.75f,
                                    colors = SliderDefaults.colors(
                                        thumbColor = Color(0xFFFFD600),
                                        activeTrackColor = Color(0xFFFFD600),
                                        inactiveTrackColor = SurfaceControl
                                    ),
                                    modifier = Modifier.width(100.dp).height(24.dp)
                                )
                                Text(
                                    text = "${(state.scale * 100).roundToInt()}%",
                                    color = Color(0xFFFFD600),
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            // Opacity Slider
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(text = "OPACITY", color = TextSecondary, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                Slider(
                                    value = state.opacity,
                                    onValueChange = { state.opacity = it },
                                    valueRange = 0.20f..1.0f,
                                    colors = SliderDefaults.colors(
                                        thumbColor = Color(0xFF00E5FF),
                                        activeTrackColor = Color(0xFF00E5FF),
                                        inactiveTrackColor = SurfaceControl
                                    ),
                                    modifier = Modifier.width(100.dp).height(24.dp)
                                )
                                Text(
                                    text = "${(state.opacity * 100).roundToInt()}%",
                                    color = Color(0xFF00E5FF),
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            // Visibility Toggle
                            IconButton(
                                onClick = { state.visible = !state.visible },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = if (state.visible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = "Visibility",
                                    tint = if (state.visible) Color(0xFF00E5FF) else AccentRed,
                                    modifier = Modifier.size(15.dp)
                                )
                            }

                            // Center Alignment
                            IconButton(
                                onClick = {
                                    state.x = 0.50f
                                },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CenterFocusStrong,
                                    contentDescription = "Center",
                                    tint = TextSecondary,
                                    modifier = Modifier.size(15.dp)
                                )
                            }

                            // Reset Item
                            IconButton(
                                onClick = {
                                    val def = HudLayoutProfile.defaultElement(id)
                                    state.applyConfig(def)
                                },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.RestartAlt,
                                    contentDescription = "Reset Item",
                                    tint = AccentRed,
                                    modifier = Modifier.size(15.dp)
                                )
                            }

                            // Close inspector
                            IconButton(
                                onClick = { selectedElementId = null },
                                modifier = Modifier.size(20.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Close",
                                    tint = TextSecondary,
                                    modifier = Modifier.size(13.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Draggable HUD element with layout-phase and draw-phase state reads to eliminate recomposition.
 */
@Composable
private fun DraggableHudElement(
    state: ElementEditorState,
    buttonShape: ButtonShape,
    team: RacingTeam,
    canvasWidthPx: Float,
    canvasHeightPx: Float,
    isSelected: Boolean
) {
    val (baseW, baseH) = getElementBaseSize(state.id)
    val density = LocalDensity.current
    val baseWPx = with(density) { baseW.toPx() }
    val baseHPx = with(density) { baseH.toPx() }

    Box(
        modifier = Modifier
            .offset {
                // Read in layout phase: zero recomposition of other nodes during drag!
                val cx = canvasWidthPx * state.x
                val cy = canvasHeightPx * state.y
                val currentW = baseWPx * state.scale
                val currentH = baseHPx * state.scale
                IntOffset(
                    (cx - currentW / 2f).roundToInt(),
                    (cy - currentH / 2f).roundToInt()
                )
            }
            .size(baseW, baseH)
            .graphicsLayer {
                // Read in draw phase: zero recomposition during scale & opacity changes!
                scaleX = state.scale
                scaleY = state.scale
                alpha = if (state.visible) state.opacity else 0.25f
            },
        contentAlignment = Alignment.Center
    ) {
        // Visual graphic
        RenderHudElementVisual(
            id = state.id,
            buttonShape = buttonShape,
            team = team
        )

        // Minimalist selection border
        if (isSelected) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .border(
                        width = 1.5.dp,
                        color = Color(0xFF00E5FF),
                        shape = RoundedCornerShape(8.dp)
                    )
            )
        } else if (!state.visible) {
            Text(
                text = "OFF",
                color = AccentRed,
                fontSize = 9.sp,
                fontWeight = FontWeight.Black,
                modifier = Modifier
                    .clip(RoundedCornerShape(3.dp))
                    .background(Color(0xDD000000))
                    .padding(horizontal = 4.dp, vertical = 1.dp)
            )
        }
    }
}

/**
 * Visual representations of individual HUD controls.
 */
@Composable
private fun RenderHudElementVisual(
    id: String,
    buttonShape: ButtonShape,
    team: RacingTeam
) {
    when (id) {
        HudElementId.BRAKE -> {
            TactileCircleButton(
                text = "Brake",
                size = 110.dp,
                buttonShape = buttonShape,
                onValueChange = {}
            )
        }
        HudElementId.THROTTLE -> {
            TactileCircleButton(
                text = "Accl",
                size = 110.dp,
                buttonShape = buttonShape,
                onValueChange = {}
            )
        }
        HudElementId.CLUTCH -> {
            TactileClutchButton(
                width = 130.dp,
                height = 84.dp,
                buttonShape = buttonShape,
                onValueChange = {}
            )
        }
        HudElementId.GEAR_SHIFTER -> {
            GearArrows(
                width = 100.dp,
                height = 60.dp,
                onGearUpChange = {},
                onGearDownChange = {}
            )
        }
        HudElementId.STEERING_WHEEL -> {
            SteeringWheelView(
                rotationAngleDegrees = 0f,
                diameter = 220.dp,
                team = team,
                touchSteeringEnabled = false,
                onTouchSteer = {}
            )
        }
        HudElementId.HANDBRAKE -> {
            TactileEBrakeButton(
                size = 62.dp,
                buttonShape = buttonShape,
                onPressChange = {}
            )
        }
        HudElementId.REV_LIGHTS -> {
            RevLightStrip(
                throttle = 0.75f,
                width = 240.dp,
                height = 14.dp
            )
        }
        HudElementId.TELEMETRY_BADGE -> {
            SteeringTelemetryBadge(
                steeringAngle = 0f,
                team = team
            )
        }
        HudElementId.SELECT_BTN -> {
            TactilePillButton(
                text = "Select",
                width = 118.dp,
                height = 38.dp,
                onPressChange = {}
            )
        }
        HudElementId.START_BTN -> {
            TactilePillButton(
                text = "Start",
                width = 118.dp,
                height = 38.dp,
                onPressChange = {}
            )
        }
    }
}

@Composable
private fun MinimalPillButton(
    text: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0x22FFFFFF))
            .clickable { onClick() }
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Text(
            text = text,
            color = Color.White,
            fontSize = 10.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}
