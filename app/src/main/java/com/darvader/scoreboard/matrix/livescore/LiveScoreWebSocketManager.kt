package com.darvader.scoreboard.matrix.livescore

import android.util.Log
import org.java_websocket.client.WebSocketClient
import org.java_websocket.handshake.ServerHandshake
import org.json.JSONObject
import java.net.URI
import javax.net.ssl.SSLSocketFactory

class LiveScoreWebSocketManager {
    companion object {
        private const val TAG = "LiveScoreWebSocket"
        const val WEB_SOCKET_URL_DVV = "wss://backend.sams-ticker.de/indoor/dvv"
        const val WEB_SOCKET_URL_TVV = "wss://backend.sams-ticker.de/indoor/tvv"
    }

    interface WebSocketListener {
        fun onMatchUpdate(payload: JSONObject)
        fun onWebSocketConnected()
        fun onWebSocketDisconnected()
        fun onWebSocketError(error: String)
    }

    private var webSocketClient: WebSocketClient? = null
    private var listener: WebSocketListener? = null
    private var currentWebSocketUrl: String? = null

    fun setListener(listener: WebSocketListener) { this.listener = listener }

    fun connect(region: String) {
        currentWebSocketUrl = when {
            region.contains("tvv") -> WEB_SOCKET_URL_TVV
            region.contains("dvv") -> WEB_SOCKET_URL_DVV
            else -> null
        }
        currentWebSocketUrl?.let { url ->
            if (webSocketClient?.isOpen == true) return
            disconnect()
            initializeWebSocket(url)
        }
    }

    private fun initializeWebSocket(url: String) {
        try {
            val uri = URI(url)
            webSocketClient = object : WebSocketClient(uri) {
                override fun onOpen(handshakedata: ServerHandshake?) {
                    listener?.onWebSocketConnected()
                }
                override fun onMessage(message: String?) {
                    message?.let {
                        try {
                            val json = JSONObject(it)
                            if (json.getString("type") == "MATCH_UPDATE") {
                                listener?.onMatchUpdate(json.getJSONObject("payload"))
                            }
                        } catch (e: Exception) {
                            Log.e(TAG, "Error parsing message: ${e.message}")
                        }
                    }
                }
                override fun onClose(code: Int, reason: String?, remote: Boolean) {
                    listener?.onWebSocketDisconnected()
                    if (!remote) reconnect()
                }
                override fun onError(ex: Exception?) {
                    listener?.onWebSocketError(ex?.message ?: "Unknown WebSocket error")
                }
            }
            val socketFactory = SSLSocketFactory.getDefault() as SSLSocketFactory
            webSocketClient?.setSocketFactory(socketFactory)
            webSocketClient?.connect()
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing WebSocket: ${e.message}")
            listener?.onWebSocketError(e.message ?: "Unknown WebSocket error")
        }
    }

    private fun reconnect() {
        currentWebSocketUrl?.let { url ->
            Thread.sleep(5000)
            initializeWebSocket(url)
        }
    }

    fun disconnect() {
        webSocketClient?.close()
        webSocketClient = null
    }

    fun isConnected(): Boolean = webSocketClient?.isOpen == true
}
