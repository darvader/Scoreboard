package com.darvader.scoreboard.matrix.livescore

import org.json.JSONObject

class Match(
    val teamDescription1: String,
    val team1: String,
    val teamDescription2: String,
    val team2: String,
    val id: String,
    val league: String
) {
    companion object {
        fun fromJson(json: JSONObject): Match {
            return Match(
                teamDescription1 = json.getString("teamDescription1").normalizeUmlauts(),
                team1 = json.getString("team1"),
                teamDescription2 = json.getString("teamDescription2").normalizeUmlauts(),
                team2 = json.getString("team2"),
                id = json.getString("id"),
                league = json.getString("matchSeries")
            )
        }

        private fun String.normalizeUmlauts(): String =
            replace("ü", "ue").replace("ö", "oe").replace("ä", "ae").replace("ß", "ss")
    }

    var setPointsTeam1: Int = 0
    var setPointsTeam2: Int = 0
    var started: Boolean = false
    var finished: Boolean = false
    var matchSets = ArrayList<MatchSet>()
    var leftTeamServes: Boolean = true

    fun update(matchesPayload: JSONObject) {
        val matchStates = matchesPayload.getJSONObject("matchStates")
        val match = matchStates.optJSONObject(id) ?: return
        started = match.getBoolean("started")
        finished = match.getBoolean("finished")
        if (started) parseSets(match)
    }

    private fun parseSets(match: JSONObject) {
        val setPoints = match.getJSONObject("setPoints")
        setPointsTeam1 = setPoints.getInt("team1")
        setPointsTeam2 = setPoints.getInt("team2")
        val matchSetsJson = match.getJSONArray("matchSets")
        matchSets.clear()
        leftTeamServes = match.getString("serving") == "team1"
        (0 until matchSetsJson.length()).forEach {
            matchSets.add(MatchSet.fromJson(matchSetsJson.getJSONObject(it)))
        }
    }

    fun updateMatch(payload: JSONObject) {
        parseSets(payload)
    }
}
