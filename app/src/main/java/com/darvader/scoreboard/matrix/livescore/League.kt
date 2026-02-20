package com.darvader.scoreboard.matrix.livescore

import org.json.JSONObject

data class League(val leagueJSON: JSONObject) {
    val id = leagueJSON.getString("id")
    val name = leagueJSON.getString("name")
    val shortName = leagueJSON.getString("shortName")
    val matches = ArrayList<Match>()
    val matchesMap = HashMap<String, Match>()
}
