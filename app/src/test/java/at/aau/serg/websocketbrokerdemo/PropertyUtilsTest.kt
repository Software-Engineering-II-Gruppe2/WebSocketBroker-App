package at.aau.serg.websocketbrokerdemo

import at.aau.serg.websocketbrokerdemo.data.properties.DummyProperty
import at.aau.serg.websocketbrokerdemo.data.properties.PropertyColor
import at.aau.serg.websocketbrokerdemo.data.properties.copyWithOwner
import at.aau.serg.websocketbrokerdemo.ui.checkCompleteSet
import at.aau.serg.websocketbrokerdemo.ui.getColorForPosition
import org.junit.Assert.*
import org.junit.jupiter.api.Test


class PropertyUtilsTest {

    @Test
    fun getColorForPosition_returnsCorrectColor() {
        assertEquals(PropertyColor.BROWN, getColorForPosition(1))
        assertEquals(PropertyColor.LIGHT_BLUE, getColorForPosition(6))
        assertEquals(PropertyColor.DARK_BLUE, getColorForPosition(39))
        assertEquals(PropertyColor.NONE, getColorForPosition(99))
    }

    @Test
    fun checkCompleteSet_returnsTrueIfAllOwned() {
        val color = PropertyColor.RED
        val owned = listOf(
            DummyProperty(21, 21, color),
            DummyProperty(23, 23, color),
            DummyProperty(24, 24, color)
        )
        val all = owned
        assertTrue(checkCompleteSet(color, owned, all))
    }

    @Test
    fun checkCompleteSet_returnsFalseIfMissingProperties() {
        val color = PropertyColor.YELLOW
        val owned = listOf(
            DummyProperty(26, 26, color),
            DummyProperty(27, 27, color)
        )
        val all = listOf(
            DummyProperty(26, 26, color),
            DummyProperty(27, 27, color),
            DummyProperty(29, 29, color)
        )
        assertFalse(checkCompleteSet(color, owned, all))
    }

    @Test
    fun checkCompleteSet_returnsFalseIfWrongColor() {
        val color = PropertyColor.GREEN
        val owned = listOf(
            DummyProperty(31, 31, PropertyColor.LIGHT_BLUE),
            DummyProperty(32, 32, PropertyColor.LIGHT_BLUE)
        )
        val all = listOf(
            DummyProperty(31, 31, color),
            DummyProperty(32, 32, color),
            DummyProperty(34, 34, color)
        )
        assertFalse(checkCompleteSet(color, owned, all))
    }
    @Test
    fun copyWithOwner_setsNewOwnerIdCorrectly() {
        val original = DummyProperty(1, 1, PropertyColor.RED)
        val updated = original.copyWithOwner("abc123")
        assertEquals("abc123", updated.ownerId)
    }
}
