package com.darvader.scoreboard

import com.darvader.scoreboard.NetworkConstants.UDP_DISCOVERY_PORT
import java.io.IOException
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.net.SocketException
import java.util.concurrent.CopyOnWriteArrayList

open class UdpDiscoveryServer @Throws(SocketException::class)
constructor(private val bindSocket: Boolean = true) : Thread() {

    private val socket: DatagramSocket? = if (bindSocket) DatagramSocket(UDP_DISCOVERY_PORT) else null
    @Volatile
    private var running = false
    private val buf = ByteArray(256)

    interface MessageListener {
        fun onMessage(address: InetAddress, received: String)
    }

    private val listeners = CopyOnWriteArrayList<MessageListener>()

    open fun register(listener: MessageListener) {
        listeners.add(listener)
    }

    override fun run() {
        if (!bindSocket) return
        running = true
        while (running) {
            try {
                val packet = DatagramPacket(buf, buf.size)
                socket!!.receive(packet)
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

    open fun stopServer() {
        running = false
        socket?.close()
    }
}
