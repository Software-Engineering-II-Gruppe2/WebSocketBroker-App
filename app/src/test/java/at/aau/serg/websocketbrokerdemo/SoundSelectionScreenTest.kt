package at.aau.serg.websocketbrokerdemo
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
class SoundSelectionScreenTest {
    private var selectedSound: String? = null
    private var message: String = ""
    private fun selectSound(sound: String) {
        selectedSound = sound
        message = "✅ Sound gespeichert: $sound"
    }
    @Test
    fun testSelectSound_Classic() {
        selectSound("Classic")
        assertEquals("Classic", selectedSound)
        assertEquals("✅ Sound gespeichert: Classic", message)
    }
    @Test
    fun testSelectSound_Chime() {
        selectSound("Chime")
        assertEquals("Chime", selectedSound)
        assertEquals("✅ Sound gespeichert: Chime", message)
    }
    @Test
    fun testSelectSound_Nature() {
        selectSound("Nature")
        assertEquals("Nature", selectedSound)
        assertEquals("✅ Sound gespeichert: Nature", message)
    }
    @Test
    fun testSelectSound_Beep() {
        selectSound("Beep")
        assertEquals("Beep", selectedSound)
        assertEquals("✅ Sound gespeichert: Beep", message)
    }
}