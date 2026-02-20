package com.darvader.scoreboard

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.darvader.scoreboard.matrix.LedMatrix
import com.darvader.scoreboard.matrix.activity.LiveScoreActivity

class MainActivity : AppCompatActivity() {

    companion object {
        lateinit var ledMatrix: LedMatrix
        lateinit var echoServer: EchoServer
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ledMatrix = LedMatrix()
        echoServer = EchoServer()
        echoServer.isDaemon = true
        echoServer.start()
        val intent = Intent(this, LiveScoreActivity::class.java)
        startActivity(intent)
        finish()
    }
}
