package com.darvader.scoreboard.matrix.livescore

import android.util.Log
import org.json.JSONArray
import org.json.JSONObject
import java.time.LocalDate
import java.time.ZonedDateTime

class MatchManager {
    companion object {
        private const val TAG = "MatchManager"
    }

    interface MatchManagerListener {
        fun onMatchesParsed(leagues: List<League>)
        fun onMatchUpdated(match: Match)
        fun onParsingError(error: String)
    }

    private var listener: MatchManagerListener? = null
    private var matchSeries: JSONObject = JSONObject()
    private var matchesPayload: JSONObject = JSONObject()

    private val _matches = ArrayList<Match>()
    private val _matchesMap = HashMap<String, Match>()
    private val _leagues = ArrayList<League>()
    private val _leaguesMap = HashMap<String, League>()

    val matches: List<Match> get() = _matches
    val matchesMap: Map<String, Match> get() = _matchesMap
    val leagues: List<League> get() = _leagues
    val leaguesMap: Map<String, League> get() = _leaguesMap

    fun setListener(listener: MatchManagerListener) { this.listener = listener }

    fun parseMatches(payload: JSONObject) {
        try {
            clearData()
            matchesPayload = payload
            matchSeries = payload.getJSONObject("matchSeries")
            val matchDays = payload.getJSONArray("matchDays")
            (0 until matchDays.length()).forEach { dayIndex ->
                val matchDay = matchDays.getJSONObject(dayIndex)
                val dateTime = ZonedDateTime.parse(matchDay.getString("date"))
                if (dateTime.toLocalDate().equals(LocalDate.now())) {
                    val matchesArray = matchDay.getJSONArray("matches")
                    (0 until matchesArray.length()).forEach { parseMatch(matchesArray, it) }
                }
            }
            listener?.onMatchesParsed(_leagues.toList())
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing matches: ${e.message}")
            listener?.onParsingError(e.message ?: "Unknown parsing error")
        }
    }

    private fun parseMatch(matchesArray: JSONArray, index: Int) {
        val matchJSON = matchesArray.getJSONObject(index)
        val match = Match.fromJson(matchJSON)
        match.update(matchesPayload)
        _matchesMap[match.id] = match

        var league: League? = _leaguesMap[match.league]
        if (league == null) {
            val leagueJSON = matchSeries.getJSONObject(match.league)
            league = League.fromJson(leagueJSON)
            _leaguesMap[match.league] = league
            _leagues.add(league)
        }
        league.addMatch(match)
        _matches.add(match)
    }

    fun updateMatch(payload: JSONObject) {
        try {
            val uuid = payload.getString("matchUuid")
            _matchesMap[uuid]?.let {
                it.updateMatch(payload)
                listener?.onMatchUpdated(it)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error updating match: ${e.message}")
        }
    }

    private fun clearData() {
        _matches.clear(); _matchesMap.clear(); _leagues.clear(); _leaguesMap.clear()
        matchSeries = JSONObject(); matchesPayload = JSONObject()
    }
}
