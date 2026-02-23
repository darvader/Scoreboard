package com.darvader.scoreboard.matrix

import android.widget.Button
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.Constraints
import com.darvader.scoreboard.EchoClient
import com.darvader.scoreboard.matrix.activity.ScoreboardActivity

class LedMatrix {
    companion object {
        var matrixAddress = ""
        val echoClient = EchoClient()
        var broadcast = false
    }

    var invert = true
    var switch = false
    var pointsLeft: Byte = 0
    var pointsRight: Byte = 0
    var setsLeft: Byte = 0
    var setsRight: Byte = 0
    var leftTeamServes: Byte = 1
    var scoreboardActivity: ScoreboardActivity? = null

    private val discoveredAddresses = ArrayList<String>()
    private val matrixButtons = ArrayList<Button>()
    private var buttonCounter = 1

    fun detect() {
        echoClient.sendBroadCast("Detect")
    }

    fun onMessage(hostAddress: String, received: String) {
        if (received.startsWith("LedMatrix")) {
            if (!discoveredAddresses.contains(hostAddress)) {
                discoveredAddresses.add(hostAddress)
                if (discoveredAddresses.size == 1) {
                    matrixAddress = hostAddress
                }
                scoreboardActivity?.runOnUiThread { addButton(hostAddress) }
            }
        }
    }

    private fun updateText() {
        scoreboardActivity?.let { activity ->
            activity.binding.points.text = "${pointsLeft.toString().padStart(2, '0')}:${pointsRight.toString().padStart(2, '0')}"
            activity.binding.sets.text = "$setsLeft:$setsRight"
            if (leftTeamServes == 1.toByte()) {
                activity.binding.ballLeft.setBackgroundColor(0xFFFF9800.toInt())
                activity.binding.ballRight.setBackgroundColor(0xFF2C2C2C.toInt())
            } else {
                activity.binding.ballRight.setBackgroundColor(0xFFFF9800.toInt())
                activity.binding.ballLeft.setBackgroundColor(0xFF2C2C2C.toInt())
            }
        }
    }

    fun updateScore() {
        updateText()
        if (invert)
            send("updateScore=${pointsRight.toChar()}${pointsLeft.toChar()}${setsRight.toChar()}${setsLeft.toChar()}${(1 - leftTeamServes).toChar()}")
        else
            send("updateScore=${pointsLeft.toChar()}${pointsRight.toChar()}${setsLeft.toChar()}${setsRight.toChar()}${leftTeamServes.toChar()}")
    }

    fun pointsLeftUp() {
        if (pointsLeft >= 99) return
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
        if (pointsRight >= 99) return
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
            echoClient.sendBroadCast(message)
        else
            echoClient.send(message, matrixAddress)
    }

    fun send(message: ByteArray) {
        if (broadcast)
            echoClient.sendBroadCast(message)
        else
            echoClient.send(message, matrixAddress)
    }

    fun off() { send("off") }

    fun setsLeftUp() {
        if (setsLeft >= 9) return
        setsLeft++
        updateScore()
    }

    fun setsLeftDown() {
        if (setsLeft <= 0) return
        setsLeft--
        updateScore()
    }

    fun setsRightUp() {
        if (setsRight >= 9) return
        setsRight++
        updateScore()
    }

    fun setsRightDown() {
        if (setsRight <= 0) return
        setsRight--
        updateScore()
    }

    fun switch() {
        this.switch = !this.switch
        pointsLeft = pointsRight.also { pointsRight = pointsLeft }
        setsLeft = setsRight.also { setsRight = setsLeft }
        leftTeamServes = if (leftTeamServes == 0.toByte()) 1 else 0
        updateScore()
    }

    fun timeout() {
        send("timeout")
        scoreboardActivity?.runOnUiThread {
            scoreboardActivity?.binding?.timeoutBar?.visibility = android.view.View.VISIBLE
            scoreboardActivity?.binding?.timeoutText?.visibility = android.view.View.VISIBLE
        }
        Thread {
            val start = System.currentTimeMillis()
            var elapsed = System.currentTimeMillis() - start
            while (elapsed < 30000) {
                elapsed = System.currentTimeMillis() - start
                val remainingSeconds = 30 - (elapsed / 1000).toInt()
                scoreboardActivity?.runOnUiThread {
                    scoreboardActivity?.binding?.timeoutBar?.progress = (elapsed / 30000.0 * 100).toInt()
                    scoreboardActivity?.binding?.timeoutText?.text = "Timeout: ${remainingSeconds}s"
                }
                Thread.sleep(100)
            }
            scoreboardActivity?.runOnUiThread {
                scoreboardActivity?.binding?.timeoutBar?.progress = 0
                scoreboardActivity?.binding?.timeoutText?.text = "Timeout: 0s"
                scoreboardActivity?.binding?.timeoutBar?.visibility = android.view.View.GONE
                scoreboardActivity?.binding?.timeoutText?.visibility = android.view.View.GONE
            }
        }.start()
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

    private fun addButton(address: String) {
        val activity = scoreboardActivity ?: return
        val button = Button(activity)
        button.layoutParams = Constraints.LayoutParams(
            Constraints.LayoutParams.WRAP_CONTENT,
            Constraints.LayoutParams.WRAP_CONTENT
        )
        button.text = address.substring(address.length - 3)
        button.id = buttonCounter++
        button.setOnClickListener {
            matrixAddress = address
            // highlight selected button
            matrixButtons.forEach { it.alpha = 0.5f }
            button.alpha = 1.0f
        }

        val layoutParams = button.layoutParams as ConstraintLayout.LayoutParams
        val size = matrixButtons.size
        if (size == 0) {
            layoutParams.topToBottom = activity.binding.matrixButtonsAnchor.id
            layoutParams.startToStart = ConstraintLayout.LayoutParams.PARENT_ID
        } else {
            if (size % 4 == 0) {
                layoutParams.topToBottom = matrixButtons[size - 4].id
                layoutParams.startToStart = matrixButtons[size - 4].id
            } else {
                layoutParams.topToBottom = matrixButtons[size - (size % 4)].id
                layoutParams.startToEnd = matrixButtons[size - 1].id
            }
        }
        button.layoutParams = layoutParams
        matrixButtons.add(button)
        activity.binding.matrixSelectionLayout.addView(button)
        button.alpha = if (address == matrixAddress) 1.0f else 0.5f
    }
}
