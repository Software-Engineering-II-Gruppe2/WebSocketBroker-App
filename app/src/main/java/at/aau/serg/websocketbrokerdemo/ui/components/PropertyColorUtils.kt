package at.aau.serg.websocketbrokerdemo.ui.components

import androidx.compose.ui.graphics.Color
import at.aau.serg.websocketbrokerdemo.data.properties.Property
import at.aau.serg.websocketbrokerdemo.data.properties.PropertyColor

fun getColorForPosition(position: Int): PropertyColor {
    return when (position) {
        1, 3 -> PropertyColor.BROWN
        6, 8, 9 -> PropertyColor.LIGHT_BLUE
        11, 13, 14 -> PropertyColor.PINK
        16, 18, 19 -> PropertyColor.ORANGE
        21, 23, 24 -> PropertyColor.RED
        26, 27, 29 -> PropertyColor.YELLOW
        31, 32, 34 -> PropertyColor.GREEN
        37, 39 -> PropertyColor.DARK_BLUE
        5, 15, 25, 35 -> PropertyColor.RAILROAD
        12, 28 -> PropertyColor.UTILITY
        else -> error("Unhandled position: $position")
    }
}

fun getColorForSet(colorSet: PropertyColor): Color {
    return when (colorSet) {
        PropertyColor.BROWN -> Color(0xFF964B00)
        PropertyColor.LIGHT_BLUE -> Color(0xFFADD8E6)
        PropertyColor.PINK -> Color(0xFFFFC0CB)
        PropertyColor.ORANGE -> Color(0xFFFFA500)
        PropertyColor.RED -> Color.Red
        PropertyColor.YELLOW -> Color.Yellow
        PropertyColor.GREEN -> Color.Green
        PropertyColor.DARK_BLUE -> Color(0xFF00008B)
        PropertyColor.RAILROAD -> Color(0xFF8B4513)
        PropertyColor.UTILITY -> Color(0xFF20B2AA)
    }
}

fun checkCompleteSet(
    colorSet: PropertyColor,
    owned: List<Property>,
    allProperties: List<Property>
): Boolean {
    val totalInSet = allProperties.count { getColorForPosition(it.position) == colorSet }
    return owned.size == totalInSet
}
