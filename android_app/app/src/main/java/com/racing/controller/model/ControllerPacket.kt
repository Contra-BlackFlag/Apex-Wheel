package com.racing.controller.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ControllerPacket(
    @SerialName("v")
    val version: Int = 1,
    @SerialName("seq")
    val sequenceNumber: Long = 0,
    @SerialName("t")
    val timestamp: Long = System.currentTimeMillis(),
    @SerialName("type")
    val type: String = "input",
    @SerialName("inputs")
    val inputs: ControllerInputs = ControllerInputs()
)

@Serializable
data class ControllerInputs(
    @SerialName("steering")
    val steering: Float = 0f, // -1.0 (full left) to +1.0 (full right)
    @SerialName("throttle")
    val throttle: Float = 0f, // 0.0 to 1.0
    @SerialName("brake")
    val brake: Float = 0f, // 0.0 to 1.0
    @SerialName("clutch")
    val clutch: Float = 0f, // 0.0 to 1.0
    @SerialName("handbrake")
    val handbrake: Boolean = false,
    @SerialName("gear_up")
    val gearUp: Boolean = false,
    @SerialName("gear_down")
    val gearDown: Boolean = false,
    @SerialName("select")
    val select: Boolean = false,
    @SerialName("start")
    val start: Boolean = false
)

@Serializable
data class HeartbeatPacket(
    @SerialName("v")
    val version: Int = 1,
    @SerialName("type")
    val type: String = "ping",
    @SerialName("t")
    val timestamp: Long = System.currentTimeMillis()
)
