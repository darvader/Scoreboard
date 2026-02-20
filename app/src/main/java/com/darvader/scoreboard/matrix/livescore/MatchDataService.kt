package com.darvader.scoreboard.matrix.livescore

import android.content.Context
import android.util.Log
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject

class MatchDataService(private val context: Context) {
    companion object {
        private const val TAG = "MatchDataService"
    }

    interface MatchDataListener {
        fun onMatchDataReceived(matchData: JSONObject)
        fun onMatchDataError(error: String)
    }

    fun fetchMatchData(source: String, listener: MatchDataListener) {
        Thread {
            try {
                val message = if (source == "TEST_MODE") loadTestData() else fetchJsonFromUrl(source)
                message?.let {
                    listener.onMatchDataReceived(JSONObject(it))
                } ?: listener.onMatchDataError("Failed to load match data from $source")
            } catch (e: Exception) {
                Log.e(TAG, "Error fetching match data: ${e.message}")
                listener.onMatchDataError(e.message ?: "Unknown error")
            }
        }.start()
    }

    private fun fetchJsonFromUrl(url: String): String? {
        val client = OkHttpClient()
        val request = Request.Builder().url(url).build()
        return try {
            val response = client.newCall(request).execute()
            if (response.isSuccessful) response.body?.string() else null
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching JSON: ${e.message}")
            null
        }
    }

    private fun loadTestData(): String? {
        return try {
            val inputStream = context.assets.open("json_test_files/sample_match_data.json")
            val buffer = ByteArray(inputStream.available())
            inputStream.read(buffer)
            inputStream.close()
            String(buffer, Charsets.UTF_8)
        } catch (e: Exception) {
            Log.e(TAG, "Error reading test data: ${e.message}")
            null
        }
    }
}
