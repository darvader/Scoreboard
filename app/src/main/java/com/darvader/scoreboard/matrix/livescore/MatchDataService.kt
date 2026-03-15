package com.darvader.scoreboard.matrix.livescore

import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject

class MatchDataService {
    companion object {
        private const val TAG = "MatchDataService"
    }

    interface MatchDataListener {
        fun onMatchDataReceived(matchData: JSONObject)
        fun onMatchDataError(error: String)
    }

    private val scope = CoroutineScope(Dispatchers.IO)

    fun fetchMatchData(source: MatchDataSource, listener: MatchDataListener) {
        scope.launch {
            try {
                val message = source.fetchData()
                message?.let {
                    listener.onMatchDataReceived(JSONObject(it))
                } ?: listener.onMatchDataError("Failed to load match data")
            } catch (e: Exception) {
                Log.e(TAG, "Error fetching match data: ${e.message}")
                listener.onMatchDataError(e.message ?: "Unknown error")
            }
        }
    }
}
