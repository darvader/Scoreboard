package com.darvader.scoreboard.matrix

interface ScoreDisplay {
    fun updateScoreText(pointsLeft: Byte, pointsRight: Byte, setsLeft: Byte, setsRight: Byte, leftTeamServes: Byte)
    fun addMatrixButton(address: String, isSelected: Boolean, onSelect: (String) -> Unit)
}
