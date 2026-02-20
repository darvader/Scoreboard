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

    val matches = ArrayList<Match>()
    val matchesMap = HashMap<String, Match>()
    val leagues = ArrayList<League>()
    val leaguesMap = HashMap<String, League>()

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
            listener?.onMatchesParsed(leagues.toList())
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing matches: ${e.message}")
            listener?.onParsingError(e.message ?: "Unknown parsing error")
        }
    }

    private fun parseMatch(matchesArray: JSONArray, index: Int) {
        val matchJSON = matchesArray.getJSONObject(index)
        val match = Match(matchJSON)
        match.update(matchesPayload)
        matchesMap[match.id] = match

        var league: League? = leaguesMap[match.league]
        if (league == null) {
            val leagueJSON = matchSeries.getJSONObject(match.league)
            league = League(leagueJSON)
            leaguesMap[match.league] = league
            leagues.add(league)
        }
        league.matchesMap[match.id] = match
        league.matches.add(match)
        matches.add(match)
    }

    fun updateMatch(payload: JSONObject) {
        try {
            val uuid = payload.getString("matchUuid")
            matchesMap[uuid]?.let {
                it.updateMatch(payload)
                listener?.onMatchUpdated(it)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error updating match: ${e.message}")
        }
    }

    private fun clearData() {
        matches.clear(); matchesMap.clear(); leagues.clear(); leaguesMap.clear()
        matchSeries = JSONObject(); matchesPayload = JSONObject()
    }
}
