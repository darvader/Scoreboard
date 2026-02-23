package com.darvader.scoreboard.matrix.livescore

open class LiveScoreWebSocketManager {
    open fun connect(region: String) {
        // Default implementation
    }

    open fun disconnect() {
        // Default implementation
    }
}

class TestLiveScoreWebSocketManager : LiveScoreWebSocketManager() {
    var onScoreUpdate: ((team1: Int, team2: Int) -> Unit)? = null
    fun simulateScoreUpdate(team1: Int, team2: Int) {
        onScoreUpdate?.invoke(team1, team2)
    }
    override fun connect(region: String) {
        // Do nothing
    }
    override fun disconnect() {
        // Do nothing
    }
}
