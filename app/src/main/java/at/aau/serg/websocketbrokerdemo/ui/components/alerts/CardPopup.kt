package at.aau.serg.websocketbrokerdemo.ui.components.alerts

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun CardPopup(
    cardType: String,
    cardId: Int,
    cardDesc: String?,
    onDismiss: () -> Unit
) {
    // Build the resource name, e.g. "chance_2" or "community_chest_7"
    val context = LocalContext.current
    val resName = "${cardType.lowercase()}_$cardId"
    val imageResId = context.resources.getIdentifier(resName, "drawable", context.packageName)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (cardType == "CHANCE") "Ereigniskarte" else "Gemeinschaftskarte",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Show the card image if it exists
                if (imageResId != 0) {
                    Image(
                        painter = painterResource(id = imageResId),
                        contentDescription = "Card $resName",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .padding(8.dp),
                    )
                } else {
                    // Fallback
                    Text(
                        text = cardDesc ?: "",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss
            ) {
                Text("OK")
            }
        },
        dismissButton = {}
    )
}
