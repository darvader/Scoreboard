package com.darvader.scoreboard

import java.io.IOException
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.net.SocketException

class EchoServer @Throws(SocketException::class)
constructor() : Thread() {

    private val socket = DatagramSocket(4445)
    private var running = false
    private val buf = ByteArray(256)

    interface MessageListener {
        fun onMessage(address: InetAddress, received: String)
    }

    private val listeners = ArrayList<MessageListener>()

    fun register(listener: MessageListener) {
        listeners.add(listener)
    }

    override fun run() {
        running = true
        while (running) {
            try {
                val packet = DatagramPacket(buf, buf.size)
                socket.receive(packet)
                val address = packet.address
                val received = String(packet.data, 0, packet.length)
                listeners.forEach { it.onMessage(address, received) }
                if (received == "end") {
                    running = false
                }
            } catch (e: IOException) {
                if (running) throw IllegalStateException(e)
            }
        }
    }

    fun stopServer() {
        running = false
        socket.close()
    }
}

