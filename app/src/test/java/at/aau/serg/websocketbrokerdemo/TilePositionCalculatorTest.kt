package at.aau.serg.websocketbrokerdemo

import androidx.compose.ui.unit.dp
import at.aau.serg.websocketbrokerdemo.logic.TilePositionCalculator
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach

class TilePositionCalculatorTest {
    private var allPositions  = mutableListOf<Int>()

    @BeforeEach
    fun setup(){
        // Corners:
        allPositions.add(TilePositionCalculator.calculateTilePosition(0,0))
        allPositions.add(TilePositionCalculator.calculateTilePosition(0,10))
        allPositions.add(TilePositionCalculator.calculateTilePosition(10,0))
        allPositions.add(TilePositionCalculator.calculateTilePosition(10,10))

        // Bottom row excluding Corners:
        for (col in 1..9) {
            allPositions.add(TilePositionCalculator.calculateTilePosition(10, col))
        }

        // Left column excluding Corners:
        for (row in 1..9) {
            allPositions.add(TilePositionCalculator.calculateTilePosition(row, 0))
        }

        // Top row excluding Corners:
        for (col in 1..9) {
            allPositions.add(TilePositionCalculator.calculateTilePosition(0, col))
        }

        // Right column excluding Corners:
        for (row in 1..9) {
            allPositions.add(TilePositionCalculator.calculateTilePosition(row, 10))
        }
    }

    @Test
    fun testBottomRowPositions() {
        // Bottom row (0-10)
        assertEquals(10, TilePositionCalculator.calculateTilePosition(10, 0)) // Bottom left corner
        assertEquals(9, TilePositionCalculator.calculateTilePosition(10, 1))
        assertEquals(8, TilePositionCalculator.calculateTilePosition(10, 2))
        assertEquals(5, TilePositionCalculator.calculateTilePosition(10, 5))
        assertEquals(2, TilePositionCalculator.calculateTilePosition(10, 8))
        assertEquals(1, TilePositionCalculator.calculateTilePosition(10, 9))
        assertEquals(0, TilePositionCalculator.calculateTilePosition(10, 10)) // Bottom right corner (Start)
    }

    @Test
    fun testLeftColumnPositions() {
        // Left column (11-20)
        assertEquals(10, TilePositionCalculator.calculateTilePosition(10, 0)) // Bottom left corner
        assertEquals(11, TilePositionCalculator.calculateTilePosition(9, 0))
        assertEquals(12, TilePositionCalculator.calculateTilePosition(8, 0))
        assertEquals(15, TilePositionCalculator.calculateTilePosition(5, 0))
        assertEquals(18, TilePositionCalculator.calculateTilePosition(2, 0))
        assertEquals(19, TilePositionCalculator.calculateTilePosition(1, 0))
        assertEquals(20, TilePositionCalculator.calculateTilePosition(0, 0)) // Top left corner
    }

    @Test
    fun testTopRowPositions() {
        // Top row (21-30)
        assertEquals(20, TilePositionCalculator.calculateTilePosition(0, 0)) // Top left corner
        assertEquals(21, TilePositionCalculator.calculateTilePosition(0, 1))
        assertEquals(22, TilePositionCalculator.calculateTilePosition(0, 2))
        assertEquals(25, TilePositionCalculator.calculateTilePosition(0, 5))
        assertEquals(28, TilePositionCalculator.calculateTilePosition(0, 8))
        assertEquals(29, TilePositionCalculator.calculateTilePosition(0, 9))
        assertEquals(30, TilePositionCalculator.calculateTilePosition(0, 10)) // Top right corner
    }

    @Test
    fun testRightColumnPositions() {
        // Right column (31-39)
        assertEquals(30, TilePositionCalculator.calculateTilePosition(0, 10)) // Top right corner
        assertEquals(31, TilePositionCalculator.calculateTilePosition(1, 10))
        assertEquals(32, TilePositionCalculator.calculateTilePosition(2, 10))
        assertEquals(35, TilePositionCalculator.calculateTilePosition(5, 10))
        assertEquals(38, TilePositionCalculator.calculateTilePosition(8, 10))
        assertEquals(39, TilePositionCalculator.calculateTilePosition(9, 10))
        assertEquals(0, TilePositionCalculator.calculateTilePosition(10, 10)) // Bottom right corner (GO)
    }

    @Test
    fun testCornerPositions() {
        // All four corners
        assertEquals(0, TilePositionCalculator.calculateTilePosition(10, 10)) // Bottom right (GO)
        assertEquals(10, TilePositionCalculator.calculateTilePosition(10, 0)) // Bottom left
        assertEquals(20, TilePositionCalculator.calculateTilePosition(0, 0)) // Top left
        assertEquals(30, TilePositionCalculator.calculateTilePosition(0, 10)) // Top right
    }

    @Test
    fun testInnerPositionsReturnNegativeOne() {
        assertEquals(-1, TilePositionCalculator.calculateTilePosition(5, 5))
        assertEquals(-1, TilePositionCalculator.calculateTilePosition(1, 1))
        assertEquals(-1, TilePositionCalculator.calculateTilePosition(9, 9))
        assertEquals(-1, TilePositionCalculator.calculateTilePosition(3, 7))
    }

    @Test
    fun testPositionsOutsideTheBoundsReturnNegativeOne() {
        assertEquals(-1, TilePositionCalculator.calculateTilePosition(-1, 5))
        assertEquals(-1, TilePositionCalculator.calculateTilePosition(5, -1))
        assertEquals(-1, TilePositionCalculator.calculateTilePosition(11, 5))
        assertEquals(-1, TilePositionCalculator.calculateTilePosition(5, 11))
    }

    @Test
    fun testAllPositionsArePresentAndUnique() {
        assertEquals(40, allPositions.distinct().size)

        for (i in 0..39) {
            assert(allPositions.contains(i)) { "Position $i is missing" }
        }
    }

    @Test
    fun testOffsetListValues() {
        val pullIn = 12.dp
        val offsets = listOf(
            pullIn to pullIn,     // top-start
            -pullIn to pullIn,    // top-end
            pullIn to -pullIn,    // bottom-start
            -pullIn to -pullIn    // bottom-end
        )

        // We should have exactly 4 offset entries
        assertEquals(4, offsets.size)

        // Check the first and last entries explicitly
        assertEquals(pullIn,  offsets[0].first)
        assertEquals(pullIn,  offsets[0].second)

        assertEquals(-pullIn, offsets[3].first)
        assertEquals(-pullIn, offsets[3].second)
    }
}
