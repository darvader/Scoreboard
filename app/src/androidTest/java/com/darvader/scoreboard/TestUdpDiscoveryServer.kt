package com.darvader.scoreboard

import java.net.InetAddress

class TestUdpDiscoveryServer : UdpDiscoveryServer(false) {
    private var testListener: UdpDiscoveryServer.MessageListener? = null
    override fun register(listener: UdpDiscoveryServer.MessageListener) {
        this.testListener = listener
    }
    fun simulateMatrixResponse(address: String) {
        val inet = InetAddress.getByName(address)
        testListener?.onMessage(inet, "LedMatrix")
    }
}
