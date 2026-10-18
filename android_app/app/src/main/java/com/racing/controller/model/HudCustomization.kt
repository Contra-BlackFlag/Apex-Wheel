package com.racing.controller.model

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * Customizable button shapes for the racing cockpit interface.
 */
enum class ButtonShape(val id: String, val displayName: String, val description: String) {
    CIRCLE("circle", "Round Wheel", "Classic circular racing pedal buttons"),
    SQUARE("square", "Boxy GT3", "Rugged rounded rectangular endurance pedals"),
    HEXAGON("hexagon", "Stealth Diamond", "Futuristic cut-corner hypercar geometry"),
    PILL("pill", "Modern Oval", "Aerodynamic high-speed rounded pill contours");

    fun toComposeShape(cornerPercentOrDp: Int = 18): Shape = when (this) {
        CIRCLE -> CircleShape
        SQUARE -> RoundedCornerShape(cornerPercentOrDp.dp)
        HEXAGON -> CutCornerShape(cornerPercentOrDp.dp)
        PILL -> RoundedCornerShape(32.dp)
    }

    companion object {
        fun fromId(id: String): ButtonShape = entries.find { it.id == id } ?: CIRCLE
    }
}

/**
 * Unique identifiers for all customizable HUD widgets.
 */
object HudElementId {
    const val BRAKE = "brake"
    const val THROTTLE = "throttle"
    const val CLUTCH = "clutch"
    const val GEAR_SHIFTER = "gear_shifter"
    const val STEERING_WHEEL = "steering_wheel"
    const val HANDBRAKE = "handbrake"
    const val REV_LIGHTS = "rev_lights"
    const val TELEMETRY_BADGE = "telemetry_badge"
    const val SELECT_BTN = "select_btn"
    const val START_BTN = "start_btn"
}

/**
 * Configuration for an individual HUD element (like in PUBG Mobile / COD Mobile).
 * Stores normalized screen coordinates (0.0 .. 1.0), individual scale, opacity and visibility.
 */
@Serializable
data class HudElementConfig(
    val id: String,
    val name: String,
    val xNormalized: Float, // Center X position as fraction of screen width (0.0 .. 1.0)
    val yNormalized: Float, // Center Y position as fraction of screen height (0.0 .. 1.0)
    val scale: Float = 1.0f, // 0.5f .. 1.8f
    val opacity: Float = 1.0f, // 0.2f .. 1.0f
    val visible: Boolean = true
)

/**
 * Complete layout profile holding the positions and states of all HUD elements.
 */
@Serializable
data class HudLayoutProfile(
    val version: Int = 1,
    val presetName: String = "Custom",
    val elements: Map<String, HudElementConfig> = emptyMap()
) {
    fun getConfig(id: String): HudElementConfig {
        return elements[id] ?: defaultElement(id)
    }

    fun updateElement(id: String, transform: (HudElementConfig) -> HudElementConfig): HudLayoutProfile {
        val current = getConfig(id)
        val updated = transform(current)
        return copy(elements = elements + (id to updated))
    }

    fun toJson(): String {
        return try {
            jsonSerializer.encodeToString(this)
        } catch (e: Exception) {
            ""
        }
    }

    companion object {
        private val jsonSerializer = Json {
            ignoreUnknownKeys = true
            encodeDefaults = true
        }

        fun fromJson(jsonStr: String): HudLayoutProfile {
            if (jsonStr.isBlank()) return defaultProfile()
            return try {
                jsonSerializer.decodeFromString<HudLayoutProfile>(jsonStr)
            } catch (e: Exception) {
                defaultProfile()
            }
        }

        fun defaultElement(id: String): HudElementConfig {
            return when (id) {
                HudElementId.BRAKE -> HudElementConfig(id, "Brake Pedal", 0.14f, 0.38f)
                HudElementId.CLUTCH -> HudElementConfig(id, "Clutch Pedal", 0.14f, 0.76f)
                HudElementId.THROTTLE -> HudElementConfig(id, "Throttle (Accl)", 0.86f, 0.38f)
                HudElementId.GEAR_SHIFTER -> HudElementConfig(id, "Gear Shifter", 0.86f, 0.74f)
                HudElementId.STEERING_WHEEL -> HudElementConfig(id, "Steering Wheel", 0.50f, 0.56f)
                HudElementId.HANDBRAKE -> HudElementConfig(id, "E-Brake / Handbrake", 0.72f, 0.62f)
                HudElementId.REV_LIGHTS -> HudElementConfig(id, "Rev Light Strip", 0.50f, 0.16f)
                HudElementId.TELEMETRY_BADGE -> HudElementConfig(id, "Telemetry Badge", 0.50f, 0.23f)
                HudElementId.SELECT_BTN -> HudElementConfig(id, "Select Button", 0.44f, 0.90f)
                HudElementId.START_BTN -> HudElementConfig(id, "Start Button", 0.56f, 0.90f)
                else -> HudElementConfig(id, id, 0.5f, 0.5f)
            }
        }

        fun defaultProfile(): HudLayoutProfile {
            val list = listOf(
                defaultElement(HudElementId.BRAKE),
                defaultElement(HudElementId.CLUTCH),
                defaultElement(HudElementId.THROTTLE),
                defaultElement(HudElementId.GEAR_SHIFTER),
                defaultElement(HudElementId.STEERING_WHEEL),
                defaultElement(HudElementId.HANDBRAKE),
                defaultElement(HudElementId.REV_LIGHTS),
                defaultElement(HudElementId.TELEMETRY_BADGE),
                defaultElement(HudElementId.SELECT_BTN),
                defaultElement(HudElementId.START_BTN)
            )
            return HudLayoutProfile(
                version = 1,
                presetName = "Default 2-Thumb",
                elements = list.associateBy { it.id }
            )
        }

        fun claw4FingerProfile(): HudLayoutProfile {
            val map = defaultProfile().elements.toMutableMap()
            map[HudElementId.BRAKE] = HudElementConfig(HudElementId.BRAKE, "Brake Pedal", 0.14f, 0.24f, scale = 1.15f)
            map[HudElementId.CLUTCH] = HudElementConfig(HudElementId.CLUTCH, "Clutch Pedal", 0.14f, 0.76f, scale = 1.0f)
            map[HudElementId.THROTTLE] = HudElementConfig(HudElementId.THROTTLE, "Throttle (Accl)", 0.86f, 0.24f, scale = 1.15f)
            map[HudElementId.GEAR_SHIFTER] = HudElementConfig(HudElementId.GEAR_SHIFTER, "Gear Shifter", 0.86f, 0.74f, scale = 1.05f)
            map[HudElementId.HANDBRAKE] = HudElementConfig(HudElementId.HANDBRAKE, "E-Brake / Handbrake", 0.72f, 0.54f, scale = 1.1f)
            return HudLayoutProfile(version = 1, presetName = "4-Finger Claw", elements = map)
        }

        fun leftHandedProfile(): HudLayoutProfile {
            val map = defaultProfile().elements.toMutableMap()
            map[HudElementId.THROTTLE] = HudElementConfig(HudElementId.THROTTLE, "Throttle (Accl)", 0.14f, 0.38f)
            map[HudElementId.GEAR_SHIFTER] = HudElementConfig(HudElementId.GEAR_SHIFTER, "Gear Shifter", 0.14f, 0.74f)
            map[HudElementId.BRAKE] = HudElementConfig(HudElementId.BRAKE, "Brake Pedal", 0.86f, 0.38f)
            map[HudElementId.CLUTCH] = HudElementConfig(HudElementId.CLUTCH, "Clutch Pedal", 0.86f, 0.76f)
            map[HudElementId.HANDBRAKE] = HudElementConfig(HudElementId.HANDBRAKE, "E-Brake / Handbrake", 0.28f, 0.62f)
            return HudLayoutProfile(version = 1, presetName = "Left-Handed", elements = map)
        }
    }
}
