package at.aau.serg.websocketbrokerdemo.data

data class PlayerProfile(
    val level: Int = 1,
    val gamesPlayed: Int = 0,
    val name: String = "",
    val wins: Int = 0,
    val averageMoney: Double = 0.0,
    val highestMoney: Int = 0
)
