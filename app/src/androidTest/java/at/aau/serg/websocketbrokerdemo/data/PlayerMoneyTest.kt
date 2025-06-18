package at.aau.serg.websocketbrokerdemo.data

import org.junit.Assert.*
import org.junit.Test

class PlayerMoneyTest {

    @Test
    fun testPlayerMoneyInitialization() {
        val player = PlayerMoney(id = "123", name = "Alice", money = 1500, position = 0)
        assertEquals("123", player.id)
        assertEquals("Alice", player.name)
        assertEquals(1500, player.money)
    }

    @Test
    fun testPlayerMoneyEquality() {
        val player1 = PlayerMoney("1", "Bob", 1000, position = 0)
        val player2 = PlayerMoney("1", "Bob", 1000, position = 0)
        assertEquals(player1, player2) // data class liefert equals() automatisch
    }

    @Test
    fun testPlayerMoneyCopy() {
        val original = PlayerMoney("2", "Charlie", 1200, position = 0)
        val copy = original.copy(money = 1400)
        assertEquals("2", copy.id)
        assertEquals("Charlie", copy.name)
        assertEquals(1400, copy.money)
    }
}
