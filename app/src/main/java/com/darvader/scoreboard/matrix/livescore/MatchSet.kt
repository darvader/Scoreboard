package com.darvader.scoreboard.matrix.livescore

import org.json.JSONObject

class MatchSet(
    val team1: Int,
    val team2: Int,
    val setNumber: Int
) {
    companion object {
        fun fromJson(json: JSONObject): MatchSet {
            val score = json.getJSONObject("setScore")
            return MatchSet(
                team1 = score.getInt("team1"),
                team2 = score.getInt("team2"),
                setNumber = json.getInt("setNumber")
            )
        }
    }
}
