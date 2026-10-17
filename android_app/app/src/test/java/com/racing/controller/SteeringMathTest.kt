package com.racing.controller

import com.racing.controller.sensors.SteeringSensorManager
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SteeringMathTest {

    @Test
    fun testDeadZoneProducesZero() {
        val deadZoneDeg = 3.0f
        val maxAngleDeg = 45.0f

        // Within dead zone
        val resultCenter = SteeringSensorManager.computeNormalizedSteering(
            angleDeg = 0.0f,
            deadZoneDegrees = deadZoneDeg,
            maxAngleDegrees = maxAngleDeg,
            sensitivity = 1.0f,
            invert = false
        )
        assertEquals(0.0f, resultCenter, 0.0001f)

        val resultSmallTurn = SteeringSensorManager.computeNormalizedSteering(
            angleDeg = 2.5f,
            deadZoneDegrees = deadZoneDeg,
            maxAngleDegrees = maxAngleDeg,
            sensitivity = 1.0f,
            invert = false
        )
        assertEquals(0.0f, resultSmallTurn, 0.0001f)

        val resultNegativeSmall = SteeringSensorManager.computeNormalizedSteering(
            angleDeg = -2.5f,
            deadZoneDegrees = deadZoneDeg,
            maxAngleDegrees = maxAngleDeg,
            sensitivity = 1.0f,
            invert = false
        )
        assertEquals(0.0f, resultNegativeSmall, 0.0001f)
    }

    @Test
    fun testFullLockClamping() {
        val deadZoneDeg = 0.0f
        val maxAngleDeg = 45.0f

        // Exact full lock right
        val resultMaxRight = SteeringSensorManager.computeNormalizedSteering(
            angleDeg = 45.0f,
            deadZoneDegrees = deadZoneDeg,
            maxAngleDegrees = maxAngleDeg,
            sensitivity = 1.0f,
            invert = false
        )
        assertEquals(1.0f, resultMaxRight, 0.0001f)

        // Beyond full lock right (e.g. 60 deg)
        val resultOverRight = SteeringSensorManager.computeNormalizedSteering(
            angleDeg = 60.0f,
            deadZoneDegrees = deadZoneDeg,
            maxAngleDegrees = maxAngleDeg,
            sensitivity = 1.0f,
            invert = false
        )
        assertEquals(1.0f, resultOverRight, 0.0001f)

        // Exact full lock left
        val resultMaxLeft = SteeringSensorManager.computeNormalizedSteering(
            angleDeg = -45.0f,
            deadZoneDegrees = deadZoneDeg,
            maxAngleDegrees = maxAngleDeg,
            sensitivity = 1.0f,
            invert = false
        )
        assertEquals(-1.0f, resultMaxLeft, 0.0001f)
    }

    @Test
    fun testInversion() {
        val deadZoneDeg = 0.0f
        val maxAngleDeg = 45.0f

        val normalRight = SteeringSensorManager.computeNormalizedSteering(
            angleDeg = 22.5f,
            deadZoneDegrees = deadZoneDeg,
            maxAngleDegrees = maxAngleDeg,
            sensitivity = 1.0f,
            invert = false
        )
        assertTrue(normalRight > 0f)

        val invertedRight = SteeringSensorManager.computeNormalizedSteering(
            angleDeg = 22.5f,
            deadZoneDegrees = deadZoneDeg,
            maxAngleDegrees = maxAngleDeg,
            sensitivity = 1.0f,
            invert = true
        )
        assertEquals(-normalRight, invertedRight, 0.0001f)
    }

    @Test
    fun testSensitivityScaling() {
        val deadZoneDeg = 0.0f
        val maxAngleDeg = 50.0f

        val atHalfAngleNormal = SteeringSensorManager.computeNormalizedSteering(
            angleDeg = 25.0f,
            deadZoneDegrees = deadZoneDeg,
            maxAngleDegrees = maxAngleDeg,
            sensitivity = 1.0f,
            invert = false
        )
        assertEquals(0.5f, atHalfAngleNormal, 0.0001f)

        val atHalfAngleDoubleSens = SteeringSensorManager.computeNormalizedSteering(
            angleDeg = 25.0f,
            deadZoneDegrees = deadZoneDeg,
            maxAngleDegrees = maxAngleDeg,
            sensitivity = 2.0f,
            invert = false
        )
        assertEquals(1.0f, atHalfAngleDoubleSens, 0.0001f)
    }
}
