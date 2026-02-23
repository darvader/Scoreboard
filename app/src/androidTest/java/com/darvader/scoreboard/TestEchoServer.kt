package com.darvader.scoreboard

import java.net.InetAddress
import com.darvader.scoreboard.EchoServer

class TestEchoServer : EchoServer(false) {
    private var testListener: EchoServer.MessageListener? = null
    override fun register(listener: EchoServer.MessageListener) {
        this.testListener = listener
    }
    fun simulateMatrixResponse(address: String) {
        val inet = InetAddress.getByName(address)
        testListener?.onMessage(inet, "LedMatrix")
    }
    // run and stopServer are no-ops
}
