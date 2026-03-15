package com.darvader.scoreboard

import android.app.Application
import com.darvader.scoreboard.matrix.LedMatrix
import com.darvader.scoreboard.matrix.livescore.Match

class ScoreboardApp : Application() {
    lateinit var ledMatrix: LedMatrix
    lateinit var udpDiscoveryServer: UdpDiscoveryServer

    // Cross-activity communication
    var currentMatch: Match? = null
    var scoreUpdater: ((Match) -> Unit)? = null
    var reconnector: (() -> Unit)? = null
}
