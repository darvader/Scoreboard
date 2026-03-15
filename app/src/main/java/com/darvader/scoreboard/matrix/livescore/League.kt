package com.darvader.scoreboard.matrix.livescore

import org.json.JSONObject

class League(
    val id: String,
    val name: String,
    val shortName: String
) {
    companion object {
        fun fromJson(json: JSONObject): League {
            return League(
                id = json.getString("id"),
                name = json.getString("name"),
                shortName = json.getString("shortName")
            )
        }
    }

    private val _matches = ArrayList<Match>()
    private val _matchesMap = HashMap<String, Match>()

    val matches: List<Match> get() = _matches
    val matchesMap: Map<String, Match> get() = _matchesMap

    fun addMatch(match: Match) {
        _matches.add(match)
        _matchesMap[match.id] = match
    }
}
