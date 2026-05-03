package com.darvader.scoreboard.matrix.activity

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.darvader.scoreboard.NetworkConstants.LED_MATRIX_SETUP_SSID
import com.darvader.scoreboard.databinding.ActivityMatrixWifiSetupBinding
import com.darvader.scoreboard.matrix.setup.LedMatrixSetupClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MatrixWifiSetupActivity : AppCompatActivity() {
    companion object {
        const val PREFS_NAME = "matrix_wifi_setup"
        const val PREF_DETECT_ON_RESUME = "detect_on_resume"
    }

    private lateinit var binding: ActivityMatrixWifiSetupBinding
    private val setupClient = LedMatrixSetupClient()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMatrixWifiSetupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.openWifiSettings.setOnClickListener {
            startActivity(Intent(Settings.ACTION_WIFI_SETTINGS))
        }
        binding.refresh.setOnClickListener { refreshNetworks() }
        binding.saveWifi.setOnClickListener { saveWifi() }

        refreshNetworks()
    }

    private fun refreshNetworks() {
        setBusy(true, "Checking LedMatrix setup connection...")
        lifecycleScope.launch {
            try {
                val networks = withContext(Dispatchers.IO) {
                    setupClient.getStatus()
                    setupClient.getNetworks()
                }
                val ssids = networks.scanned
                    .map { it.ssid }
                    .filter { it.isNotBlank() }
                    .distinct()
                    .sorted()
                binding.ssid.setAdapter(
                    ArrayAdapter(
                        this@MatrixWifiSetupActivity,
                        android.R.layout.simple_dropdown_item_1line,
                        ssids
                    )
                )
                setBusy(false, "Connected to matrix setup. Choose the WiFi network the matrix should join.")
            } catch (e: Exception) {
                setBusy(false, "Connect this phone to $LED_MATRIX_SETUP_SSID, then refresh.")
            }
        }
    }

    private fun saveWifi() {
        val ssid = binding.ssid.text?.toString()?.trim().orEmpty()
        val password = binding.password.text?.toString().orEmpty()
        if (ssid.isBlank()) {
            binding.statusText.text = "Enter or choose a WiFi network name."
            return
        }

        setBusy(true, "Saving WiFi to matrix...")
        lifecycleScope.launch {
            try {
                val result = withContext(Dispatchers.IO) {
                    setupClient.saveWifi(ssid, password, binding.replaceNetworks.isChecked)
                }
                if (result.success) {
                    binding.statusText.text = "Saved. Matrix is restarting; reconnect this phone to $ssid, then return to the scoreboard."
                    getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
                        .edit()
                        .putBoolean(PREF_DETECT_ON_RESUME, true)
                        .apply()
                    binding.saveWifi.visibility = View.GONE
                } else {
                    setBusy(false, result.error ?: "Matrix rejected the WiFi settings.")
                }
            } catch (e: Exception) {
                setBusy(false, "Could not save WiFi. Check that this phone is connected to $LED_MATRIX_SETUP_SSID.")
            }
        }
    }

    private fun setBusy(isBusy: Boolean, message: String) {
        binding.statusText.text = message
        binding.refresh.isEnabled = !isBusy
        binding.saveWifi.isEnabled = !isBusy
        binding.openWifiSettings.isEnabled = !isBusy
    }
}
