package com.darvader.scoreboard.matrix.livescore

import org.json.JSONObject

data class MatchSet(val matchSet: JSONObject) {
    val team1: Int
    var team2: Int
    var setNumber: Int
    init {
        val score = matchSet.getJSONObject("setScore")
        team1 = score.getInt("team1")
        team2 = score.getInt("team2")
        setNumber = matchSet.getInt("setNumber")
    }
}
