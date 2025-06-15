package at.aau.serg.websocketbrokerdemo.logic

// Utility for mapping a (row, col) on the 11 xx 11 grid
// into a value 0 - 39
object TilePositionCalculator {

    /**
     * 0 = Start-Tile
     * Indices are increased clockwise (0-39)
     */
    fun calculateTilePosition(row: Int, col: Int): Int {
        return when {
            row == 10 && col == 10 -> 0   // Bottom-right corner (Start)
            row == 10 && col == 0  -> 10  // Bottom-left corner
            row == 0  && col == 0  -> 20  // Top-left corner
            row == 0  && col == 10 -> 30  // Top-right corner

            row == 10 -> if (col in 1..9) 10 - col else -1
            col == 0  -> if (row in 1..9) 10 + (10 - row) else -1
            row == 0  -> if (col in 1..9) 20 + col else -1
            col == 10 -> if (row in 1..9) 30 + row else -1

            else      -> -1
        }
    }

}