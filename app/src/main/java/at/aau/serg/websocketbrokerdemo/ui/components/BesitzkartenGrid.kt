package at.aau.serg.websocketbrokerdemo.ui.components

import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import at.aau.serg.websocketbrokerdemo.data.properties.Property
import at.aau.serg.websocketbrokerdemo.data.properties.PropertyColor

@Composable
fun BesitzkartenGrid(
    ownedProperties: List<Property>,
    allProperties: List<Property>,
    onPropertySetClicked: (PropertyColor) -> Unit
) {
    val propertySets = PropertyColor.entries.toTypedArray()

    LazyVerticalGrid(
        columns = GridCells.Fixed(5),
        modifier = Modifier
            .heightIn(max = 120.dp)
    ) {
        items(propertySets.size) { index ->
            val colorSet = propertySets[index]
            PropertySetCard(
                colorSet = colorSet,
                ownedProperties = ownedProperties,
                allProperties = allProperties,
                onClick = { onPropertySetClicked(colorSet) }
            )
        }
    }
}
