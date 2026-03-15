package com.darvader.scoreboard.matrix

import com.darvader.scoreboard.IUdpClient
import com.darvader.scoreboard.NetworkConstants.MAX_POINTS
import com.darvader.scoreboard.NetworkConstants.MAX_SETS
import com.darvader.scoreboard.UdpClient

class LedMatrix(
    private val udpClient: IUdpClient = defaultUdpClient
) {
    companion object {
        val defaultUdpClient: IUdpClient by lazy { UdpClient() }
    }

    var matrixAddress = ""
    var broadcast = false
    var invert = true
    var isTeamsSwitched = false
    var pointsLeft: Byte = 0
    var pointsRight: Byte = 0
    var setsLeft: Byte = 0
    var setsRight: Byte = 0
    var leftTeamServes: Byte = 1
    var scoreDisplay: ScoreDisplay? = null

    private val discoveredAddresses = ArrayList<String>()

    fun detect() {
        udpClient.sendBroadcast("Detect")
    }

    fun onMessage(hostAddress: String, received: String) {
        if (received.startsWith("LedMatrix")) {
            if (!discoveredAddresses.contains(hostAddress)) {
                discoveredAddresses.add(hostAddress)
                if (discoveredAddresses.size == 1) {
                    matrixAddress = hostAddress
                }
                scoreDisplay?.addMatrixButton(hostAddress, hostAddress == matrixAddress) { addr ->
                    matrixAddress = addr
                }
            }
        }
    }

    fun updateScore() {
        scoreDisplay?.updateScoreText(pointsLeft, pointsRight, setsLeft, setsRight, leftTeamServes)
        if (invert)
            send("updateScore=${pointsRight.toChar()}${pointsLeft.toChar()}${setsRight.toChar()}${setsLeft.toChar()}${(1 - leftTeamServes).toChar()}")
        else
            send("updateScore=${pointsLeft.toChar()}${pointsRight.toChar()}${setsLeft.toChar()}${setsRight.toChar()}${leftTeamServes.toChar()}")
    }

    fun pointsLeftUp() {
        if (pointsLeft >= MAX_POINTS) return
        pointsLeft++
        leftTeamServes = 1
        updateScore()
    }

    fun pointsLeftDown() {
        if (pointsLeft <= 0) return
        pointsLeft--
        leftTeamServes = 0
        updateScore()
    }

    fun pointsRightUp() {
        if (pointsRight >= MAX_POINTS) return
        pointsRight++
        leftTeamServes = 0
        updateScore()
    }

    fun pointsRightDown() {
        if (pointsRight <= 0) return
        pointsRight--
        leftTeamServes = 1
        updateScore()
    }

    fun ballLeft() {
        leftTeamServes = 1
        updateScore()
    }

    fun ballRight() {
        leftTeamServes = 0
        updateScore()
    }

    fun clearPoints() {
        pointsLeft = 0
        pointsRight = 0
        leftTeamServes = 1
        updateScore()
    }

    fun reset() {
        pointsLeft = 0
        pointsRight = 0
        setsLeft = 0
        setsRight = 0
        leftTeamServes = 0
        updateScore()
    }

    fun send(message: String) {
        if (broadcast)
            udpClient.sendBroadcast(message)
        else
            udpClient.send(message, matrixAddress)
    }

    fun send(message: ByteArray) {
        if (broadcast)
            udpClient.sendBroadcast(message)
        else
            udpClient.send(message, matrixAddress)
    }

    fun off() { send("off") }

    fun setsLeftUp() {
        if (setsLeft >= MAX_SETS) return
        setsLeft++
        updateScore()
    }

    fun setsLeftDown() {
        if (setsLeft <= 0) return
        setsLeft--
        updateScore()
    }

    fun setsRightUp() {
        if (setsRight >= MAX_SETS) return
        setsRight++
        updateScore()
    }

    fun setsRightDown() {
        if (setsRight <= 0) return
        setsRight--
        updateScore()
    }

    fun switchSides() {
        this.isTeamsSwitched = !this.isTeamsSwitched
        pointsLeft = pointsRight.also { pointsRight = pointsLeft }
        setsLeft = setsRight.also { setsRight = setsLeft }
        leftTeamServes = if (leftTeamServes == 0.toByte()) 1 else 0
        updateScore()
    }

    fun timeout() {
        send("timeout")
    }

    fun setScrollText(s: CharSequence?) {
        if (s == null || s.length <= 1) return
        send("scrollText=$s")
    }

    fun startScoreboard() { send("scoreboard") }

    fun changeBrightness(brightness: Int) {
        send("brightness=${brightness.toChar()}")
    }

    fun invert() {
        invert = !invert
        updateScore()
    }
}
