package com.darvader.scoreboard

import java.net.InetAddress
import com.darvader.scoreboard.EchoServer

class TestEchoServer : EchoServer() {
    private var testListener: MessageListener? = null
    fun setTestListener(listener: MessageListener) {
        this.testListener = listener
    }
    fun simulateMatrixResponse(address: String) {
        val inet = InetAddress.getByName(address)
        testListener?.onMessage(inet, "LedMatrix")
    }
    override fun register(listener: MessageListener) {
        this.testListener = listener
    }
}

