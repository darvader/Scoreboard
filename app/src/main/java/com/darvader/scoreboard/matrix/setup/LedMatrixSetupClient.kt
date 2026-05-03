package com.darvader.scoreboard.matrix.setup

import com.darvader.scoreboard.NetworkConstants.LED_MATRIX_SETUP_BASE_URL
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject

data class MatrixSetupStatus(
    val device: String,
    val setupMode: Boolean,
    val configured: Boolean,
    val networkCount: Int,
    val ip: String,
    val ssid: String
)

data class ScannedWifiNetwork(
    val ssid: String,
    val rssi: Int
)

data class SavedWifiNetwork(
    val index: Int,
    val ssid: String,
    val active: Boolean
)

data class MatrixWifiNetworks(
    val scanned: List<ScannedWifiNetwork>,
    val saved: List<SavedWifiNetwork>
)

data class MatrixSetupResult(
    val success: Boolean,
    val restarting: Boolean = false,
    val error: String? = null
)

class LedMatrixSetupClient(
    private val httpClient: OkHttpClient = OkHttpClient(),
    private val baseUrl: String = LED_MATRIX_SETUP_BASE_URL
) {
    fun getStatus(): MatrixSetupStatus {
        val json = getJson("$baseUrl/api/status")
        return MatrixSetupStatus(
            device = json.optString("device"),
            setupMode = json.optBoolean("setupMode"),
            configured = json.optBoolean("configured"),
            networkCount = json.optInt("networkCount"),
            ip = json.optString("ip"),
            ssid = json.optString("ssid")
        )
    }

    fun getNetworks(): MatrixWifiNetworks {
        val json = getJson("$baseUrl/api/networks")
        val scanned = json.optJSONArray("scanned")
        val saved = json.optJSONArray("saved")
        val scannedNetworks = ArrayList<ScannedWifiNetwork>()
        val savedNetworks = ArrayList<SavedWifiNetwork>()

        if (scanned != null) {
            for (i in 0 until scanned.length()) {
                val item = scanned.getJSONObject(i)
                scannedNetworks.add(
                    ScannedWifiNetwork(
                        ssid = item.optString("ssid"),
                        rssi = item.optInt("rssi")
                    )
                )
            }
        }

        if (saved != null) {
            for (i in 0 until saved.length()) {
                val item = saved.getJSONObject(i)
                savedNetworks.add(
                    SavedWifiNetwork(
                        index = item.optInt("index"),
                        ssid = item.optString("ssid"),
                        active = item.optBoolean("active")
                    )
                )
            }
        }

        return MatrixWifiNetworks(scannedNetworks, savedNetworks)
    }

    fun saveWifi(ssid: String, password: String, replace: Boolean): MatrixSetupResult {
        val body = FormBody.Builder()
            .add("ssid", ssid)
            .add("password", password)
            .add("replace", replace.toString())
            .build()
        val request = Request.Builder()
            .url("$baseUrl/api/wifi")
            .post(body)
            .build()

        httpClient.newCall(request).execute().use { response ->
            val bodyText = response.body?.string().orEmpty()
            val json = if (bodyText.isBlank()) JSONObject() else JSONObject(bodyText)
            return MatrixSetupResult(
                success = response.isSuccessful && json.optBoolean("success"),
                restarting = json.optBoolean("restarting"),
                error = json.optString("error").ifBlank { null }
            )
        }
    }

    private fun getJson(url: String): JSONObject {
        val request = Request.Builder().url(url).get().build()
        httpClient.newCall(request).execute().use { response ->
            val bodyText = response.body?.string().orEmpty()
            if (!response.isSuccessful) {
                throw IllegalStateException("HTTP ${response.code}: $bodyText")
            }
            return JSONObject(bodyText)
        }
    }
}
