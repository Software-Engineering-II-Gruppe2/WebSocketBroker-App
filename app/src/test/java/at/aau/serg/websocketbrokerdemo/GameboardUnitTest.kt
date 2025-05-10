package at.aau.serg.websocketbrokerdemo

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.assertEquals
import at.aau.serg.websocketbrokerdemo.ui.*
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach

class GameboardUnitTest {
    var allPositions  = mutableListOf<Int>()

    @BeforeEach
    fun setup(){
        // Corners:
        allPositions.add(calculateTilePosition(0,0))
        allPositions.add(calculateTilePosition(0,10))
        allPositions.add(calculateTilePosition(10,0))
        allPositions.add(calculateTilePosition(10,10))

        // Bottom row excluding Corners:
        for (col in 1..9) {
            allPositions.add(calculateTilePosition(10, col))
        }

        // Left column excluding Corners:
        for (row in 1..9) {
            allPositions.add(calculateTilePosition(row, 0))
        }

        // Top row excluding Corners:
        for (col in 1..9) {
            allPositions.add(calculateTilePosition(0, col))
        }

        // Right column excluding Corners:
        for (row in 1..9) {
            allPositions.add(calculateTilePosition(row, 10))
        }
    }

    @Test
    fun testBottomRowPositions() {
        // Bottom row (0-10)
        assertEquals(10, calculateTilePosition(10, 0)) // Bottom left corner
        assertEquals(9, calculateTilePosition(10, 1))
        assertEquals(8, calculateTilePosition(10, 2))
        assertEquals(5, calculateTilePosition(10, 5))
        assertEquals(2, calculateTilePosition(10, 8))
        assertEquals(1, calculateTilePosition(10, 9))
        assertEquals(0, calculateTilePosition(10, 10)) // Bottom right corner (Start)
    }

    @Test
    fun testLeftColumnPositions() {
        // Left column (11-20)
        assertEquals(10, calculateTilePosition(10, 0)) // Bottom left corner
        assertEquals(11, calculateTilePosition(9, 0))
        assertEquals(12, calculateTilePosition(8, 0))
        assertEquals(15, calculateTilePosition(5, 0))
        assertEquals(18, calculateTilePosition(2, 0))
        assertEquals(19, calculateTilePosition(1, 0))
        assertEquals(20, calculateTilePosition(0, 0)) // Top left corner
    }

    @Test
    fun testTopRowPositions() {
        // Top row (21-30)
        assertEquals(20, calculateTilePosition(0, 0)) // Top left corner
        assertEquals(21, calculateTilePosition(0, 1))
        assertEquals(22, calculateTilePosition(0, 2))
        assertEquals(25, calculateTilePosition(0, 5))
        assertEquals(28, calculateTilePosition(0, 8))
        assertEquals(29, calculateTilePosition(0, 9))
        assertEquals(30, calculateTilePosition(0, 10)) // Top right corner
    }

    @Test
    fun testRightColumnPositions() {
        // Right column (31-39)
        assertEquals(30, calculateTilePosition(0, 10)) // Top right corner
        assertEquals(31, calculateTilePosition(1, 10))
        assertEquals(32, calculateTilePosition(2, 10))
        assertEquals(35, calculateTilePosition(5, 10))
        assertEquals(38, calculateTilePosition(8, 10))
        assertEquals(39, calculateTilePosition(9, 10))
        assertEquals(0, calculateTilePosition(10, 10)) // Bottom right corner (GO)
    }

    @Test
    fun testCornerPositions() {
        // All four corners
        assertEquals(0, calculateTilePosition(10, 10)) // Bottom right (GO)
        assertEquals(10, calculateTilePosition(10, 0)) // Bottom left
        assertEquals(20, calculateTilePosition(0, 0)) // Top left
        assertEquals(30, calculateTilePosition(0, 10)) // Top right
    }

    @Test
    fun testInnerPositionsReturnNegativeOne() {
        assertEquals(-1, calculateTilePosition(5, 5))
        assertEquals(-1, calculateTilePosition(1, 1))
        assertEquals(-1, calculateTilePosition(9, 9))
        assertEquals(-1, calculateTilePosition(3, 7))
    }

    @Test
    fun testPositionsOutsideTheBoundsReturnNegativeOne() {
        assertEquals(-1, calculateTilePosition(-1, 5))
        assertEquals(-1, calculateTilePosition(5, -1))
        assertEquals(-1, calculateTilePosition(11, 5))
        assertEquals(-1, calculateTilePosition(5, 11))
    }

    @Test
    fun testAllPositionsArePresentAndUnique() {
        assertEquals(40, allPositions.distinct().size)

        for (i in 0..39) {
            assert(allPositions.contains(i)) { "Position $i is missing" }
        }
    }
}
