package com.darvader.scoreboard.matrix.livescore

import org.json.JSONObject
import org.junit.Assert.*
import org.junit.Test

class MatchTest {
    @Test
    fun testFromJson() {
        val json = JSONObject().apply {
            put("teamDescription1", "Teamüöäß")
            put("team1", "team1-id")
            put("teamDescription2", "Other Team")
            put("team2", "team2-id")
            put("id", "match-id")
            put("matchSeries", "league-id")
        }
        val match = Match.fromJson(json)
        assertEquals("Teamueoeaess", match.teamDescription1)
        assertEquals("team1-id", match.team1)
        assertEquals("Other Team", match.teamDescription2)
        assertEquals("team2-id", match.team2)
        assertEquals("match-id", match.id)
        assertEquals("league-id", match.league)
    }

    @Test
    fun testFromJsonNoUmlauts() {
        val json = JSONObject().apply {
            put("teamDescription1", "Team Alpha")
            put("team1", "t1")
            put("teamDescription2", "Team Beta")
            put("team2", "t2")
            put("id", "id-1")
            put("matchSeries", "series-1")
        }
        val match = Match.fromJson(json)
        assertEquals("Team Alpha", match.teamDescription1)
        assertEquals("Team Beta", match.teamDescription2)
    }

    @Test
    fun testInitialState() {
        val json = JSONObject().apply {
            put("teamDescription1", "A")
            put("team1", "t1")
            put("teamDescription2", "B")
            put("team2", "t2")
            put("id", "id-1")
            put("matchSeries", "s-1")
        }
        val match = Match.fromJson(json)
        assertEquals(0, match.setPointsTeam1)
        assertEquals(0, match.setPointsTeam2)
        assertFalse(match.started)
        assertFalse(match.finished)
        assertTrue(match.matchSets.isEmpty())
        assertTrue(match.leftTeamServes)
    }
}
