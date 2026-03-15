package com.darvader.scoreboard

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.darvader.scoreboard.matrix.LedMatrix
import com.darvader.scoreboard.matrix.activity.LiveScoreActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val app = application as ScoreboardApp
        app.ledMatrix = LedMatrix()
        app.udpDiscoveryServer = UdpDiscoveryServer()
        app.udpDiscoveryServer.isDaemon = true
        app.udpDiscoveryServer.start()
        startActivity(Intent(this, LiveScoreActivity::class.java))
        finish()
    }
}
