package com.racing.controller

import com.racing.controller.network.QrPayloadParser
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class QrParseTest {

    @Test
    fun testDeepLinkWithAndWithoutSlash() {
        // Without slash
        val target1 = QrPayloadParser.parse("apexwheel://connect?ip=192.168.1.50&port=9876")
        assertNotNull(target1)
        assertEquals("192.168.1.50", target1?.ip)
        assertEquals(9876, target1?.port)

        // With slash
        val target2 = QrPayloadParser.parse("apexwheel://connect/?ip=10.0.0.12&port=9000")
        assertNotNull(target2)
        assertEquals("10.0.0.12", target2?.ip)
        assertEquals(9000, target2?.port)

        // Reversed parameter order
        val target3 = QrPayloadParser.parse("apexwheel://connect?port=8888&ip=172.16.0.4")
        assertNotNull(target3)
        assertEquals("172.16.0.4", target3?.ip)
        assertEquals(8888, target3?.port)
    }

    @Test
    fun testJsonPayload() {
        val target = QrPayloadParser.parse("""{"ip": "192.168.1.100", "port": 9876}""")
        assertNotNull(target)
        assertEquals("192.168.1.100", target?.ip)
        assertEquals(9876, target?.port)
    }

    @Test
    fun testRawIpPort() {
        val target1 = QrPayloadParser.parse("192.168.1.200:9876")
        assertNotNull(target1)
        assertEquals("192.168.1.200", target1?.ip)
        assertEquals(9876, target1?.port)

        val target2 = QrPayloadParser.parse("http://192.168.1.200:9876/")
        assertNotNull(target2)
        assertEquals("192.168.1.200", target2?.ip)
        assertEquals(9876, target2?.port)
    }

    @Test
    fun testInvalidPayload() {
        assertNull(QrPayloadParser.parse(""))
        assertNull(QrPayloadParser.parse("random string without ip"))
        assertNull(QrPayloadParser.parse("apexwheel://invalid"))
    }
}
