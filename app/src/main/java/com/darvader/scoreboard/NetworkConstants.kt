package com.darvader.scoreboard

object NetworkConstants {
    const val UDP_COMMAND_PORT = 4210
    const val UDP_DISCOVERY_PORT = 4445
    const val MAX_POINTS = 99
    const val MAX_SETS = 9
    const val TIMEOUT_DURATION_MS = 30_000L
    const val URL_DVV = "https://backend.sams-ticker.de/live/indoor/tickers/dvv"
    const val URL_TVV = "https://backend.sams-ticker.de/live/indoor/tickers/tvv"
    const val WEB_SOCKET_URL_DVV = "wss://backend.sams-ticker.de/indoor/dvv"
    const val WEB_SOCKET_URL_TVV = "wss://backend.sams-ticker.de/indoor/tvv"
    const val TEST_MODE = "TEST_MODE"
    const val BALL_COLOR_ACTIVE = 0xFFFF9800.toInt()
    const val BALL_COLOR_INACTIVE = 0xFF2C2C2C.toInt()
}
