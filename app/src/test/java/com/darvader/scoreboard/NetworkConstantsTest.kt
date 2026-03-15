package com.darvader.scoreboard

import org.junit.Assert.*
import org.junit.Test

class NetworkConstantsTest {
    @Test
    fun testPortValues() {
        assertEquals(4210, NetworkConstants.UDP_COMMAND_PORT)
        assertEquals(4445, NetworkConstants.UDP_DISCOVERY_PORT)
    }

    @Test
    fun testLimits() {
        assertEquals(99, NetworkConstants.MAX_POINTS)
        assertEquals(9, NetworkConstants.MAX_SETS)
        assertEquals(30_000L, NetworkConstants.TIMEOUT_DURATION_MS)
    }

    @Test
    fun testUrls() {
        assertTrue(NetworkConstants.URL_DVV.startsWith("https://"))
        assertTrue(NetworkConstants.URL_TVV.startsWith("https://"))
        assertTrue(NetworkConstants.WEB_SOCKET_URL_DVV.startsWith("wss://"))
        assertTrue(NetworkConstants.WEB_SOCKET_URL_TVV.startsWith("wss://"))
    }
}
