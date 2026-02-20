package com.darvader.scoreboard.matrix.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import com.darvader.scoreboard.R
import com.darvader.scoreboard.databinding.ActivityLiveScoreBinding
import com.darvader.scoreboard.matrix.livescore.League
import com.darvader.scoreboard.matrix.livescore.Match
import com.darvader.scoreboard.matrix.livescore.MatchDataService
import com.darvader.scoreboard.matrix.livescore.MatchManager
import com.darvader.scoreboard.matrix.livescore.LiveScoreWebSocketManager
import org.json.JSONObject

class LiveScoreActivity : AppCompatActivity(),
    MatchDataService.MatchDataListener,
    MatchManager.MatchManagerListener,
    LiveScoreWebSocketManager.WebSocketListener {

    companion object {
        const val URL_DVV = "https://backend.sams-ticker.de/live/indoor/tickers/dvv"
        const val URL_TVV = "https://backend.sams-ticker.de/live/indoor/tickers/tvv"
        const val TEST_MODE = "TEST_MODE"
        const val TAG = "LiveScoreActivity"

        var scoreboardActivity: ScoreboardActivity? = null
        var livescoreActivity: LiveScoreActivity? = null
        var match: Match? = null
        var selectedLeague: League? = null
    }

    private lateinit var binding: ActivityLiveScoreBinding
    private lateinit var matchDataService: MatchDataService
    private lateinit var matchManager: MatchManager
    lateinit var webSocketManager: LiveScoreWebSocketManager
    var selectedRegion: String = URL_TVV

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLiveScoreBinding.inflate(layoutInflater)
        livescoreActivity = this
        setContentView(binding.root)
        initializeServices()
        setupUI()
    }

    private fun initializeServices() {
        matchDataService = MatchDataService(this)
        matchManager = MatchManager()
        webSocketManager = LiveScoreWebSocketManager()
        matchManager.setListener(this)
        webSocketManager.setListener(this)
    }

    private fun setupUI() {
        val regions = arrayOf(URL_TVV, URL_DVV, TEST_MODE)
        val regionNames = arrayOf("TVV", "DVV", "TEST MODE")
        val adapter = ArrayAdapter(this, R.layout.spinner_item_dark, regionNames)
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item_dark)
        binding.region.adapter = adapter

        binding.region.onItemSelectedListener = object : OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                selectedRegion = regions[position]
                loadMatches()
                webSocketManager.disconnect()
                webSocketManager.connect(selectedRegion)
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        binding.leagues.onItemSelectedListener = object : OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val league = matchManager.leagues[position]
                selectedLeague = league
                updateMatchesSpinner(league)
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        binding.matches.onItemSelectedListener = object : OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                match = selectedLeague?.matches?.get(position)
                showSets()
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        binding.startScoreboard.setOnClickListener {
            val intent = Intent(this, ScoreboardActivity::class.java)
            startActivity(intent)
        }
    }

    private fun loadMatches() {
        matchDataService.fetchMatchData(selectedRegion, this)
    }

    private fun updateMatchesSpinner(league: League) {
        val matchNames = league.matches.map { "${it.teamDescription1}:${it.teamDescription2}" }
        runOnUiThread {
            val adapter = ArrayAdapter(this, R.layout.spinner_item_dark, matchNames)
            adapter.setDropDownViewResource(R.layout.spinner_dropdown_item_dark)
            binding.matches.adapter = adapter
        }
    }

    private fun showSets() {
        var result = ""
        match?.matchSets?.forEach { result += "${it.team1}:${it.team2}(${it.setNumber}) " }
        runOnUiThread { binding.result.text = result }
    }

    override fun onMatchDataReceived(matchData: JSONObject) {
        matchManager.parseMatches(matchData)
    }

    override fun onMatchDataError(error: String) {
        Log.e(TAG, "Match data error: $error")
    }

    override fun onMatchesParsed(leagues: List<League>) {
        val leagueNames = leagues.map { it.name }
        runOnUiThread {
            val adapter = ArrayAdapter(this, R.layout.spinner_item_dark, leagueNames)
            adapter.setDropDownViewResource(R.layout.spinner_dropdown_item_dark)
            binding.leagues.adapter = adapter
        }
    }

    override fun onMatchUpdated(match: Match) {
        if (Companion.match?.id == match.id) {
            runOnUiThread { showSets() }
            scoreboardActivity?.inform()
        }
    }

    override fun onParsingError(error: String) {
        Log.e(TAG, "Parsing error: $error")
    }

    override fun onMatchUpdate(payload: JSONObject) {
        matchManager.updateMatch(payload)
    }

    override fun onWebSocketConnected() { Log.d(TAG, "WebSocket connected") }
    override fun onWebSocketDisconnected() { Log.d(TAG, "WebSocket disconnected") }
    override fun onWebSocketError(error: String) { Log.e(TAG, "WebSocket error: $error") }

    override fun onDestroy() {
        super.onDestroy()
        webSocketManager.disconnect()
    }
}
