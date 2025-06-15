package at.aau.serg.websocketbrokerdemo.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import at.aau.serg.websocketbrokerdemo.data.properties.Property
import at.aau.serg.websocketbrokerdemo.data.properties.PropertyColor

@Composable
fun PropertySetCard(
    colorSet: PropertyColor,
    ownedProperties: List<Property>,
    allProperties: List<Property>,
    onClick: () -> Unit
) {
    val propertiesInSet = ownedProperties.filter {
        getColorForPosition(it.position) == colorSet
    }

    val ownsCompleteSet = checkCompleteSet(colorSet, propertiesInSet, allProperties)

    val cardAlpha = when {
        propertiesInSet.isEmpty() -> 0.3f
        ownsCompleteSet -> 1f
        else -> 0.6f
    }

    Card(
        modifier = Modifier
            .padding(2.dp)
            .aspectRatio(1f)
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = getColorForSet(colorSet).copy(alpha = cardAlpha)
        )
    ) {}
}
