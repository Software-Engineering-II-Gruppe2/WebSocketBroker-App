package at.aau.serg.websocketbrokerdemo.data
import at.aau.serg.websocketbrokerdemo.ui.formatBrightness
import org.junit.Assert.assertEquals
import org.junit.Test


class FormatBrightnessTest {
    @Test
    fun testFormatBrightness_50Percent() {
        val brightness = 0.5f
        val result = formatBrightness(brightness)
        assertEquals("Helligkeit: 50%", result)
    }

    @Test
    fun testFormatBrightness_100Percent() {
        val brightness = 1.0f
        val result = formatBrightness(brightness)
        assertEquals("Helligkeit: 100%", result)
    }

    @Test
    fun testFormatBrightness_0Percent() {
        val brightness = 0.0f
        val result = formatBrightness(brightness)
        assertEquals("Helligkeit: 0%", result)
    }
}