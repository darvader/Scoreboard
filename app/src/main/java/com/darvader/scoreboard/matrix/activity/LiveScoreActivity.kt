package com.darvader.scoreboard.matrix.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import com.darvader.scoreboard.NetworkConstants
import com.darvader.scoreboard.R
import com.darvader.scoreboard.ScoreboardApp
import com.darvader.scoreboard.databinding.ActivityLiveScoreBinding
import com.darvader.scoreboard.matrix.livescore.AssetMatchDataSource
import com.darvader.scoreboard.matrix.livescore.HttpMatchDataSource
import com.darvader.scoreboard.matrix.livescore.ILiveScoreWebSocketManager
import com.darvader.scoreboard.matrix.livescore.League
import com.darvader.scoreboard.matrix.livescore.LiveScoreWebSocketManager
import com.darvader.scoreboard.matrix.livescore.Match
import com.darvader.scoreboard.matrix.livescore.MatchDataService
import com.darvader.scoreboard.matrix.livescore.MatchManager
import org.json.JSONObject

class LiveScoreActivity : AppCompatActivity(),
    MatchDataService.MatchDataListener,
    MatchManager.MatchManagerListener,
    ILiveScoreWebSocketManager.WebSocketListener {

    companion object {
        const val TAG = "LiveScoreActivity"
    }

    private lateinit var binding: ActivityLiveScoreBinding
    private lateinit var matchDataService: MatchDataService
    private lateinit var matchManager: MatchManager
    lateinit var webSocketManager: ILiveScoreWebSocketManager
    var selectedUrl: String = NetworkConstants.URL_TVV
    private var match: Match? = null
    private var selectedLeague: League? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLiveScoreBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initializeServices()
        setupUI()
        val app = application as ScoreboardApp
        app.reconnector = {
            webSocketManager.disconnect()
            webSocketManager.connect(selectedUrl)
        }
    }

    private fun initializeServices() {
        matchDataService = MatchDataService()
        matchManager = MatchManager()
        webSocketManager = LiveScoreWebSocketManager()
        matchManager.setListener(this)
        webSocketManager.setListener(this)
    }

    private fun setupUI() {
        val regions = arrayOf(NetworkConstants.URL_TVV, NetworkConstants.URL_DVV, NetworkConstants.TEST_MODE)
        val regionNames = arrayOf("TVV", "DVV", "TEST MODE")
        val adapter = ArrayAdapter(this, R.layout.spinner_item_dark, regionNames)
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item_dark)
        binding.region.adapter = adapter

        binding.region.onItemSelectedListener = object : OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                selectedUrl = regions[position]
                loadMatches()
                webSocketManager.disconnect()
                webSocketManager.connect(selectedUrl)
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
                (application as ScoreboardApp).currentMatch = match
                showSets()
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        binding.startScoreboard.setOnClickListener {
            startActivity(Intent(this, ScoreboardActivity::class.java))
        }
    }

    private fun loadMatches() {
        val source = if (selectedUrl == NetworkConstants.TEST_MODE) {
            AssetMatchDataSource(this)
        } else {
            HttpMatchDataSource(selectedUrl)
        }
        matchDataService.fetchMatchData(source, this)
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
        if (this.match?.id == match.id) {
            val app = application as ScoreboardApp
            app.currentMatch = match
            runOnUiThread { showSets() }
            app.scoreUpdater?.invoke(match)
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
