package com.darvader.scoreboard.matrix.setup

import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class LedMatrixSetupClientTest {
    private lateinit var server: MockWebServer
    private lateinit var client: LedMatrixSetupClient

    @Before
    fun setUp() {
        server = MockWebServer()
        server.start()
        client = LedMatrixSetupClient(baseUrl = server.url("/").toString().trimEnd('/'))
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    @Test
    fun getStatusParsesMatrixStatus() {
        server.enqueue(
            MockResponse().setBody(
                """{"device":"LedMatrix","setupMode":true,"configured":false,"networkCount":0,"ip":"192.168.4.1","ssid":""}"""
            )
        )

        val status = client.getStatus()

        assertEquals("LedMatrix", status.device)
        assertTrue(status.setupMode)
        assertEquals("192.168.4.1", status.ip)
        assertEquals("/api/status", server.takeRequest().path)
    }

    @Test
    fun getNetworksParsesScannedAndSavedNetworks() {
        server.enqueue(
            MockResponse().setBody(
                """{"scanned":[{"ssid":"Gym","rssi":-41}],"saved":[{"index":0,"ssid":"Old","active":true}]}"""
            )
        )

        val networks = client.getNetworks()

        assertEquals("Gym", networks.scanned.first().ssid)
        assertEquals(-41, networks.scanned.first().rssi)
        assertEquals("Old", networks.saved.first().ssid)
        assertEquals("/api/networks", server.takeRequest().path)
    }

    @Test
    fun saveWifiPostsCredentials() {
        server.enqueue(MockResponse().setBody("""{"success":true,"restarting":true}"""))

        val result = client.saveWifi("Gym", "secret", true)
        val request = server.takeRequest()

        assertTrue(result.success)
        assertTrue(result.restarting)
        assertEquals("/api/wifi", request.path)
        assertEquals("ssid=Gym&password=secret&replace=true", request.body.readUtf8())
    }
}
