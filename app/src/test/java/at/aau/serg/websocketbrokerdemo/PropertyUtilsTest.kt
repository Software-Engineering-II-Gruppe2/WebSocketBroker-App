package at.aau.serg.websocketbrokerdemo

import at.aau.serg.websocketbrokerdemo.data.properties.DummyProperty
import at.aau.serg.websocketbrokerdemo.data.properties.PropertyColor
import at.aau.serg.websocketbrokerdemo.ui.components.checkCompleteSet
import at.aau.serg.websocketbrokerdemo.ui.components.getColorForPosition
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test


class PropertyUtilsTest {

    @Test
    fun getColorForPosition_returnsCorrectColor() {
        Assertions.assertEquals(PropertyColor.BROWN, getColorForPosition(1))
        Assertions.assertEquals(PropertyColor.LIGHT_BLUE, getColorForPosition(6))
        Assertions.assertEquals(PropertyColor.DARK_BLUE, getColorForPosition(39))
    }

    @Test
    fun checkCompleteSet_returnsTrueIfAllOwned() {
        val color = PropertyColor.RED
        val owned = listOf(
            DummyProperty(21, 21, color),
            DummyProperty(23, 23, color),
            DummyProperty(24, 24, color)
        )
        Assertions.assertTrue(checkCompleteSet(color, owned, owned))
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
        Assertions.assertFalse(checkCompleteSet(color, owned, all))
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
        Assertions.assertFalse(checkCompleteSet(color, owned, all))
    }
    @Test
    fun copyWithOwner_setsNewOwnerIdCorrectly() {
        val original = DummyProperty(1, 1, PropertyColor.RED)
        val updated = original.copyWithOwner("abc123")
        Assertions.assertEquals("abc123", updated.ownerId)
    }
}
