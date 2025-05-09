package at.aau.serg.websocketbrokerdemo.data

data class SoundEffectMessage (
    val type: String,
    val sound: String,
    val playerId: String
)