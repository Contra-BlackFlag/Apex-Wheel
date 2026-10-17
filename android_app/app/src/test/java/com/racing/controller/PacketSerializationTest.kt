package com.racing.controller

import com.racing.controller.model.ControllerInputs
import com.racing.controller.model.ControllerPacket
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class PacketSerializationTest {

    private val json = Json { 
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    @Test
    fun testPacketSerializationAndSchema() {
        val packet = ControllerPacket(
            version = 1,
            sequenceNumber = 42,
            timestamp = 1713000000000L,
            inputs = ControllerInputs(
                steering = 0.75f,
                throttle = 1.0f,
                brake = 0.0f,
                clutch = 0.5f,
                handbrake = true,
                gearUp = true,
                gearDown = false,
                select = false,
                start = true
            )
        )

        val serialized = json.encodeToString(packet)
        assertTrue(serialized.contains("\"v\":1"))
        assertTrue(serialized.contains("\"seq\":42"))
        assertTrue(serialized.contains("\"steering\":0.75"))
        assertTrue(serialized.contains("\"throttle\":1.0"))
        assertTrue(serialized.contains("\"handbrake\":true"))
        assertTrue(serialized.contains("\"gear_up\":true"))
        assertTrue(serialized.contains("\"start\":true"))

        val element = json.parseToJsonElement(serialized).jsonObject
        assertEquals(1, element["v"]?.jsonPrimitive?.content?.toInt())
        assertEquals(42L, element["seq"]?.jsonPrimitive?.content?.toLong())

        val inputsObj = element["inputs"]?.jsonObject
        assertNotNull(inputsObj)
        assertEquals(0.75f, inputsObj!!["steering"]?.jsonPrimitive?.content?.toFloat() ?: 0f, 0.001f)
        assertEquals(true, inputsObj["handbrake"]?.jsonPrimitive?.content?.toBoolean())
    }
}
