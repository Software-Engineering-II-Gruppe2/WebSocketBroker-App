package at.aau.serg.websocketbrokerdemo.data

import com.google.firebase.Timestamp

data class GameData(
    val timestamp: Timestamp = Timestamp.now(),
    val won: Boolean = false,
    val endMoney: Int = 0,
    val durationMinutes: Int = 0,
    val playersCount: Int = 4,
    val levelGained: Int = 0
)
