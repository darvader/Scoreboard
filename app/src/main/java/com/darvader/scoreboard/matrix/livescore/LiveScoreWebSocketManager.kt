package com.darvader.scoreboard.matrix.livescore

import android.util.Log
import com.darvader.scoreboard.NetworkConstants
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.java_websocket.client.WebSocketClient
import org.java_websocket.handshake.ServerHandshake
import org.json.JSONObject
import java.net.URI
import javax.net.ssl.SSLSocketFactory

class LiveScoreWebSocketManager : ILiveScoreWebSocketManager {
    companion object {
        private const val TAG = "LiveScoreWebSocket"
    }

    private var webSocketClient: WebSocketClient? = null
    private var listener: ILiveScoreWebSocketManager.WebSocketListener? = null
    private var currentWebSocketUrl: String? = null
    private var reconnecting = false
    private var reconnectJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.IO)

    override fun setListener(listener: ILiveScoreWebSocketManager.WebSocketListener) {
        this.listener = listener
    }

    override fun connect(region: String) {
        currentWebSocketUrl = when {
            region.contains("tvv") -> NetworkConstants.WEB_SOCKET_URL_TVV
            region.contains("dvv") -> NetworkConstants.WEB_SOCKET_URL_DVV
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
                    reconnecting = false
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
                    if (!remote) scheduleReconnect()
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

    private fun scheduleReconnect() {
        if (reconnecting) return
        reconnecting = true
        reconnectJob = scope.launch {
            delay(5000)
            currentWebSocketUrl?.let { url ->
                initializeWebSocket(url)
            }
        }
    }

    override fun disconnect() {
        reconnecting = false
        reconnectJob?.cancel()
        reconnectJob = null
        webSocketClient?.close()
        webSocketClient = null
    }

    override fun isConnected(): Boolean = webSocketClient?.isOpen == true
}
