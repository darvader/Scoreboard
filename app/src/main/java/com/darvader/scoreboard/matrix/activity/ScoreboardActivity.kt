package com.darvader.scoreboard.matrix.activity

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.LinearLayout
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.darvader.scoreboard.NetworkConstants.BALL_COLOR_ACTIVE
import com.darvader.scoreboard.NetworkConstants.BALL_COLOR_INACTIVE
import com.darvader.scoreboard.NetworkConstants.TIMEOUT_DURATION_MS
import com.darvader.scoreboard.ScoreboardApp
import com.darvader.scoreboard.ScoreboardService
import com.darvader.scoreboard.UdpDiscoveryServer
import com.darvader.scoreboard.databinding.ActivityScoreboardBinding
import com.darvader.scoreboard.matrix.LedMatrix
import com.darvader.scoreboard.matrix.ScoreDisplay
import com.darvader.scoreboard.matrix.livescore.Match
import java.net.InetAddress
import java.util.*
import kotlin.concurrent.schedule

class ScoreboardActivity : AppCompatActivity(), ScoreDisplay {
    companion object {
        var disableInformerForTests: Boolean = false
    }

    lateinit var ledMatrix: LedMatrix
    private var timer: TimerTask? = null
    private val matrixButtons = ArrayList<Button>()

    abstract class ProgressChangedListener : SeekBar.OnSeekBarChangeListener {
        override fun onStartTrackingTouch(seekBar: SeekBar?) {}
        override fun onStopTrackingTouch(seekBar: SeekBar?) {}
    }

    lateinit var binding: ActivityScoreboardBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val app = application as ScoreboardApp
        this.ledMatrix = app.ledMatrix
        binding = ActivityScoreboardBinding.inflate(layoutInflater)
        setContentView(binding.root)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        startForegroundService(Intent(this, ScoreboardService::class.java))
        ledMatrix.scoreDisplay = this
        // Register with UdpDiscoveryServer to receive LedMatrix detect responses
        app.udpDiscoveryServer.register(object : UdpDiscoveryServer.MessageListener {
            override fun onMessage(address: InetAddress, received: String) {
                ledMatrix.onMessage(address.hostAddress ?: "", received)
            }
        })
        binding.pointsUpLeft.setOnClickListener { ledMatrix.pointsLeftUp() }
        binding.pointsDownLeft.setOnClickListener { ledMatrix.pointsLeftDown() }
        binding.pointsUpRight.setOnClickListener { ledMatrix.pointsRightUp() }
        binding.pointsDownRight.setOnClickListener { ledMatrix.pointsRightDown() }
        binding.ballLeft.setOnClickListener { ledMatrix.ballLeft() }
        binding.ballRight.setOnClickListener { ledMatrix.ballRight() }
        binding.resetPoints.setOnClickListener { ledMatrix.clearPoints() }
        binding.setsUpLeft.setOnClickListener { ledMatrix.setsLeftUp() }
        binding.setsDownLeft.setOnClickListener { ledMatrix.setsLeftDown() }
        binding.setsUpRight.setOnClickListener { ledMatrix.setsRightUp() }
        binding.setsDownRight.setOnClickListener { ledMatrix.setsRightDown() }
        binding.switchButton.setOnClickListener { ledMatrix.switchSides() }
        binding.reconnect.setOnClickListener {
            app.reconnector?.invoke()
            ledMatrix.updateScore()
        }
        binding.detect.setOnClickListener { ledMatrix.detect() }
        binding.timeout.setOnClickListener {
            ledMatrix.timeout()
            startTimeoutCountdown()
        }
        binding.invert.setOnClickListener {
            ledMatrix.invert()
            app.currentMatch?.let { inform(it) }
        }
        binding.reset.setOnClickListener { ledMatrix.reset() }
        binding.off.setOnClickListener { ledMatrix.off() }
        binding.brightness.setOnSeekBarChangeListener(object : ProgressChangedListener() {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                ledMatrix.changeBrightness(progress)
            }
        })
        binding.scrollText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                ledMatrix.setScrollText(s)
            }
            override fun afterTextChanged(s: Editable?) {}
        })
        app.scoreUpdater = { match -> runOnUiThread { inform(match) } }
        ledMatrix.startScoreboard()
        app.currentMatch?.let { inform(it) }
        if (!disableInformerForTests) {
            timer = Timer("informer", true).schedule(1000, 1000) {
                runOnUiThread { ledMatrix.updateScore() }
            }
        }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            val insetsController = WindowCompat.getInsetsController(window, window.decorView)
            insetsController.hide(WindowInsetsCompat.Type.systemBars())
            insetsController.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
    }

    // ScoreDisplay implementation
    override fun updateScoreText(pointsLeft: Byte, pointsRight: Byte, setsLeft: Byte, setsRight: Byte, leftTeamServes: Byte) {
        binding.points.text = "${pointsLeft.toString().padStart(2, '0')}:${pointsRight.toString().padStart(2, '0')}"
        binding.sets.text = "$setsLeft:$setsRight"
        if (leftTeamServes == 1.toByte()) {
            binding.ballLeft.setBackgroundColor(BALL_COLOR_ACTIVE)
            binding.ballRight.setBackgroundColor(BALL_COLOR_INACTIVE)
        } else {
            binding.ballRight.setBackgroundColor(BALL_COLOR_ACTIVE)
            binding.ballLeft.setBackgroundColor(BALL_COLOR_INACTIVE)
        }
    }

    override fun addMatrixButton(address: String, isSelected: Boolean, onSelect: (String) -> Unit) {
        runOnUiThread {
            val button = Button(this)
            button.layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            button.text = address.substring(address.length - 3)
            button.setOnClickListener {
                onSelect(address)
                matrixButtons.forEach { it.alpha = 0.5f }
                button.alpha = 1.0f
            }
            button.alpha = if (isSelected) 1.0f else 0.5f
            matrixButtons.add(button)
            binding.matrixSelectionLayout.addView(button)
        }
    }

    private fun startTimeoutCountdown() {
        binding.timeoutBar.visibility = View.VISIBLE
        binding.timeoutText.visibility = View.VISIBLE
        Thread {
            val start = System.currentTimeMillis()
            var elapsed = System.currentTimeMillis() - start
            while (elapsed < TIMEOUT_DURATION_MS) {
                elapsed = System.currentTimeMillis() - start
                val remainingSeconds = (TIMEOUT_DURATION_MS / 1000).toInt() - (elapsed / 1000).toInt()
                runOnUiThread {
                    binding.timeoutBar.progress = (elapsed.toDouble() / TIMEOUT_DURATION_MS * 100).toInt()
                    binding.timeoutText.text = "Timeout: ${remainingSeconds}s"
                }
                Thread.sleep(100)
            }
            runOnUiThread {
                binding.timeoutBar.progress = 0
                binding.timeoutText.text = "Timeout: 0s"
                binding.timeoutBar.visibility = View.GONE
                binding.timeoutText.visibility = View.GONE
            }
        }.start()
    }

    fun inform(match: Match) {
        val size = match.matchSets.size

        if (size > 0) {
            val lastSet = match.matchSets[size - 1]
            if ((ledMatrix.pointsLeft > 0 || ledMatrix.pointsRight > 0) && (lastSet.team1 == 0 && lastSet.team2 == 0))
                ledMatrix.isTeamsSwitched = !ledMatrix.isTeamsSwitched
            if ((lastSet.team1 == 8 || lastSet.team2 == 8) && size == 5 && (ledMatrix.pointsLeft < 8 && ledMatrix.pointsRight < 8))
                ledMatrix.isTeamsSwitched = !ledMatrix.isTeamsSwitched

            ledMatrix.pointsLeft = if (ledMatrix.isTeamsSwitched) lastSet.team2.toByte() else lastSet.team1.toByte()
            ledMatrix.pointsRight = if (ledMatrix.isTeamsSwitched) lastSet.team1.toByte() else lastSet.team2.toByte()

            ledMatrix.leftTeamServes = if (ledMatrix.isTeamsSwitched) {
                if (match.leftTeamServes) 0 else 1
            } else {
                if (match.leftTeamServes) 1 else 0
            }
        }
        ledMatrix.setsLeft = if (ledMatrix.isTeamsSwitched) match.setPointsTeam2.toByte() else match.setPointsTeam1.toByte()
        ledMatrix.setsRight = if (ledMatrix.isTeamsSwitched) match.setPointsTeam1.toByte() else match.setPointsTeam2.toByte()

        val text = if (!ledMatrix.invert xor ledMatrix.isTeamsSwitched)
            "${match.teamDescription1}:${match.teamDescription2}"
        else
            "${match.teamDescription2}:${match.teamDescription1}"
        runOnUiThread { binding.scrollText.setText(text) }
        ledMatrix.updateScore()
    }

    override fun onDestroy() {
        stopService(Intent(this, ScoreboardService::class.java))
        super.onDestroy()
        timer?.cancel()
    }
}
