package com.darvader.scoreboard.matrix.livescore

import org.json.JSONObject

interface ILiveScoreWebSocketManager {
    interface WebSocketListener {
        fun onMatchUpdate(payload: JSONObject)
        fun onWebSocketConnected()
        fun onWebSocketDisconnected()
        fun onWebSocketError(error: String)
    }

    fun connect(region: String)
    fun disconnect()
    fun setListener(listener: WebSocketListener)
    fun isConnected(): Boolean
}
