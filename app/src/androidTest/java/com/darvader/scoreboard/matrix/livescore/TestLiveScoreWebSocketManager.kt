package com.darvader.scoreboard.matrix.livescore

import org.json.JSONArray
import org.json.JSONObject

class TestLiveScoreWebSocketManager : ILiveScoreWebSocketManager {
    private var listener: ILiveScoreWebSocketManager.WebSocketListener? = null
    private var connected = false
    var lastConnectedRegion: String? = null

    override fun connect(region: String) {
        lastConnectedRegion = region
        connected = true
        listener?.onWebSocketConnected()
    }

    override fun disconnect() {
        connected = false
        listener?.onWebSocketDisconnected()
    }

    override fun setListener(listener: ILiveScoreWebSocketManager.WebSocketListener) {
        this.listener = listener
    }

    override fun isConnected(): Boolean = connected

    fun simulateScoreUpdate(team1: Int, team2: Int) {
        val payload = JSONObject().apply {
            put("matchUuid", "test-match-id")
            put("setPoints", JSONObject().apply {
                put("team1", 0)
                put("team2", 0)
            })
            put("serving", "team1")
            put("matchSets", JSONArray().apply {
                put(JSONObject().apply {
                    put("setScore", JSONObject().apply {
                        put("team1", team1)
                        put("team2", team2)
                    })
                    put("setNumber", 1)
                })
            })
        }
        listener?.onMatchUpdate(payload)
    }
}
