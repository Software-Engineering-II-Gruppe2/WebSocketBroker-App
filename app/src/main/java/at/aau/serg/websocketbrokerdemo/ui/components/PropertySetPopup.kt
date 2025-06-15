package at.aau.serg.websocketbrokerdemo.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import at.aau.serg.websocketbrokerdemo.data.properties.Property
import at.aau.serg.websocketbrokerdemo.data.properties.PropertyColor
import at.aau.serg.websocketbrokerdemo.data.properties.getDrawableIdFromName

@Composable
fun PropertySetPopup(
    colorSet: PropertyColor,
    ownedProperties: List<Property>,
    allProperties: List<Property>,
    onDismiss: () -> Unit,
    onSellProperty: (Int) -> Unit
) {
    val context = LocalContext.current
    val propertiesInSet =
        allProperties.filter { getColorForPosition(it.position) == colorSet }

    AlertDialog(
        modifier = Modifier
            .width(420.dp)
            .height(550.dp),
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "${colorSet.name} Set",
                style = TextStyle(
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                ),
                modifier = Modifier.fillMaxWidth(),
            )
        },
        text = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(350.dp)
                    .horizontalScroll(rememberScrollState())
                    .padding(8.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                propertiesInSet.forEach { property ->
                    val imageResId = getDrawableIdFromName(property.image, context)
                    val isOwned = ownedProperties.any { it.id == property.id }
                    Box(
                        modifier = Modifier
                            .width(180.dp)
                            .aspectRatio(0.7f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.LightGray.copy(alpha = 1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        if (imageResId != 0) {
                            Image(
                                painter = painterResource(id = imageResId),
                                contentDescription = property.name,
                                contentScale = ContentScale.Fit,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .alpha(if (isOwned) 1f else 0.4f)
                            )
                        } else {
                            Text(
                                text = property.name,
                                color = Color.Black,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ownedProperties.forEach { property ->
                    Button(
                        onClick = {
                            onSellProperty(property.id)
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE53935)),
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp)
                    ) {
                        Text("Sell ${property.name}", color = Color.White)
                    }
                }
                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0074cc)),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp)
                ) {
                    Text("Exit", color = Color.White)
                }
            }
        },
        dismissButton = {}
    )
}
