package com.darvader.scoreboard.matrix.livescore

import android.content.Context
import android.util.Log
import okhttp3.OkHttpClient
import okhttp3.Request

interface MatchDataSource {
    fun fetchData(): String?
}

class HttpMatchDataSource(private val url: String) : MatchDataSource {
    override fun fetchData(): String? {
        val client = OkHttpClient()
        val request = Request.Builder().url(url).build()
        return try {
            val response = client.newCall(request).execute()
            if (response.isSuccessful) response.body?.string() else null
        } catch (e: Exception) {
            Log.e("HttpMatchDataSource", "Error fetching JSON: ${e.message}")
            null
        }
    }
}

class AssetMatchDataSource(private val context: Context) : MatchDataSource {
    override fun fetchData(): String? {
        return try {
            val inputStream = context.assets.open("json_test_files/sample_match_data.json")
            val buffer = ByteArray(inputStream.available())
            inputStream.read(buffer)
            inputStream.close()
            String(buffer, Charsets.UTF_8)
        } catch (e: Exception) {
            Log.e("AssetMatchDataSource", "Error reading test data: ${e.message}")
            null
        }
    }
}
