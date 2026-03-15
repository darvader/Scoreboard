package com.darvader.scoreboard

import com.darvader.scoreboard.NetworkConstants.UDP_COMMAND_PORT
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.IOException
import java.io.OutputStream
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.net.NetworkInterface
import java.net.Socket
import java.net.SocketException
import java.net.UnknownHostException

interface IUdpClient {
    fun send(message: String, address: String)
    fun sendBroadcast(message: String)
    fun send(payload: ByteArray, address: String)
    fun sendBroadcast(payload: ByteArray)
}

class UdpClient @Throws(SocketException::class, UnknownHostException::class)
constructor() : IUdpClient {
    private val socketBroadcast = DatagramSocket()
    private val socket = DatagramSocket()
    private val addresses: List<InetAddress>
    private val scope = CoroutineScope(Dispatchers.IO)

    init {
        socketBroadcast.broadcast = true
        addresses = listAllBroadcastAddresses()
    }

    override fun sendBroadcast(msg: String) {
        for (a in addresses) {
            val buf = msg.toByteArray()
            scope.launch { sendBroadcastInternal(a, buf) }
        }
    }

    @Synchronized
    fun sendBroadcastInternal(address: InetAddress, buf: ByteArray) {
        val packet = DatagramPacket(buf, buf.size, address, UDP_COMMAND_PORT)
        try {
            socketBroadcast.send(packet)
        } catch (e: IOException) {
            throw IllegalStateException(e)
        }
    }

    @Throws(SocketException::class)
    internal fun listAllBroadcastAddresses(): List<InetAddress> {
        val broadcastList = ArrayList<InetAddress>()
        val interfaces = NetworkInterface.getNetworkInterfaces()
        while (interfaces.hasMoreElements()) {
            val networkInterface = interfaces.nextElement()
            if (networkInterface.isLoopback || !networkInterface.isUp) continue
            for (a in networkInterface.interfaceAddresses) {
                val broadcast = a.broadcast
                if (broadcast != null) broadcastList.add(broadcast)
            }
        }
        return broadcastList
    }

    @Throws(UnknownHostException::class)
    override fun send(msg: String, address: String) {
        if (address == "") return
        val buf = msg.toByteArray()
        scope.launch { sendInternal(InetAddress.getByName(address), buf) }
    }

    @Throws(UnknownHostException::class)
    override fun send(payload: ByteArray, address: String) {
        scope.launch { sendInternal(InetAddress.getByName(address), payload) }
    }

    @Synchronized
    private fun sendInternal(address: InetAddress, buf: ByteArray) {
        val packet = DatagramPacket(buf, buf.size, address, UDP_COMMAND_PORT)
        try {
            socket.send(packet)
        } catch (e: IOException) {
            throw IllegalStateException(e)
        }
    }

    override fun sendBroadcast(payload: ByteArray) {
        for (a in addresses) scope.launch { sendBroadcastInternal(a, payload) }
    }

    fun sendTcp(byteArray: ByteArray, matrixAddress: String) {
        try {
            val clientSocket = Socket(matrixAddress, 80)
            val outToServer: OutputStream = clientSocket.getOutputStream()
            outToServer.write(byteArray)
            clientSocket.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
