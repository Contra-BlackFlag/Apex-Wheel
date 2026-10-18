package com.racing.controller.model

sealed interface ConnectionState {
    data object Disconnected : ConnectionState
    data object Connecting : ConnectionState
    data class Connected(val host: String, val port: Int, val pingMs: Long = 0) : ConnectionState
    data class Error(val message: String) : ConnectionState
}

data class ControllerSettings(
    val hostIp: String = "192.168.1.100",
    val port: Int = 9876,
    val steeringSensitivity: Float = 1.0f, // 0.5 to 2.5
    val maxSteeringAngleDegrees: Float = 45.0f, // 15 to 90
    val deadZonePercent: Float = 3.0f, // 0% to 15%
    val invertSteering: Boolean = false,
    val smoothingFactor: Float = 0.20f, // 0.0 (raw) to 0.8 (heavy lowpass)
    val useTouchSteering: Boolean = false,
    val updateRateHz: Int = 100, // 30, 60, 100, 120
    val hapticFeedbackEnabled: Boolean = true,
    val throttleRampEnabled: Boolean = false,
    val selectedTeam: String = "red_bull",
    // HUD Customization
    val buttonShape: String = "circle", // "circle", "square", "hexagon", "pill"
    val buttonScale: Float = 1.0f, // 0.8f to 1.25f
    val swapThrottleBrake: Boolean = false, // Swap pedals left/right
    val showRevLights: Boolean = true,
    val showSteeringDegree: Boolean = true,
    val showClutch: Boolean = true,
    val showHandbrake: Boolean = true,
    val hudLayoutJson: String = ""
)
