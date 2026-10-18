package com.racing.controller

import com.racing.controller.model.ButtonShape
import com.racing.controller.model.ControllerSettings
import com.racing.controller.model.HudElementId
import com.racing.controller.model.HudLayoutProfile
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class HudCustomizationTest {

    @Test
    fun testButtonShapeFromId() {
        assertEquals(ButtonShape.CIRCLE, ButtonShape.fromId("circle"))
        assertEquals(ButtonShape.SQUARE, ButtonShape.fromId("square"))
        assertEquals(ButtonShape.HEXAGON, ButtonShape.fromId("hexagon"))
        assertEquals(ButtonShape.PILL, ButtonShape.fromId("pill"))
        assertEquals(ButtonShape.CIRCLE, ButtonShape.fromId("unknown_shape"))
    }

    @Test
    fun testDefaultSettingsIncludeHudCustomization() {
        val settings = ControllerSettings()
        assertEquals(ButtonShape.CIRCLE.id, settings.buttonShape)
        assertEquals(1.0f, settings.buttonScale, 0.001f)
        assertFalse(settings.swapThrottleBrake)
        assertTrue(settings.showRevLights)
        assertTrue(settings.showSteeringDegree)
        assertTrue(settings.showClutch)
        assertTrue(settings.showHandbrake)
        assertEquals("", settings.hudLayoutJson)
    }

    @Test
    fun testSettingsHudCustomizationModifications() {
        val custom = ControllerSettings(
            buttonShape = ButtonShape.HEXAGON.id,
            buttonScale = 1.15f,
            swapThrottleBrake = true,
            showRevLights = false,
            showSteeringDegree = true,
            showClutch = false,
            showHandbrake = true
        )
        assertEquals("hexagon", custom.buttonShape)
        assertEquals(1.15f, custom.buttonScale, 0.001f)
        assertTrue(custom.swapThrottleBrake)
        assertFalse(custom.showRevLights)
        assertTrue(custom.showSteeringDegree)
        assertFalse(custom.showClutch)
        assertTrue(custom.showHandbrake)
    }

    @Test
    fun testDefaultHudLayoutProfile() {
        val profile = HudLayoutProfile.defaultProfile()
        assertEquals("Default 2-Thumb", profile.presetName)
        assertEquals(10, profile.elements.size)

        val brake = profile.getConfig(HudElementId.BRAKE)
        assertNotNull(brake)
        assertEquals(0.14f, brake.xNormalized, 0.01f)
        assertEquals(0.38f, brake.yNormalized, 0.01f)
        assertEquals(1.0f, brake.scale, 0.01f)
        assertTrue(brake.visible)

        val throttle = profile.getConfig(HudElementId.THROTTLE)
        assertNotNull(throttle)
        assertEquals(0.86f, throttle.xNormalized, 0.01f)
    }

    @Test
    fun testClaw4FingerProfile() {
        val claw = HudLayoutProfile.claw4FingerProfile()
        assertEquals("4-Finger Claw", claw.presetName)

        val brake = claw.getConfig(HudElementId.BRAKE)
        // In 4-finger claw, brake is raised up for index finger
        assertEquals(0.24f, brake.yNormalized, 0.01f)
        assertEquals(1.15f, brake.scale, 0.01f)

        val throttle = claw.getConfig(HudElementId.THROTTLE)
        assertEquals(0.24f, throttle.yNormalized, 0.01f)
    }

    @Test
    fun testHudLayoutProfileSerializationRoundTrip() {
        val original = HudLayoutProfile.defaultProfile().updateElement(HudElementId.BRAKE) {
            it.copy(xNormalized = 0.25f, yNormalized = 0.50f, scale = 1.35f, opacity = 0.8f, visible = true)
        }

        val jsonString = original.toJson()
        assertTrue(jsonString.isNotBlank())
        assertTrue(jsonString.contains("\"scale\":1.35"))

        val deserialized = HudLayoutProfile.fromJson(jsonString)
        val brake = deserialized.getConfig(HudElementId.BRAKE)
        assertEquals(0.25f, brake.xNormalized, 0.001f)
        assertEquals(0.50f, brake.yNormalized, 0.001f)
        assertEquals(1.35f, brake.scale, 0.001f)
        assertEquals(0.8f, brake.opacity, 0.001f)
        assertTrue(brake.visible)
    }
}
