package at.aau.serg.websocketbrokerdemo.ui.components.alerts

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import at.aau.serg.websocketbrokerdemo.data.properties.Property
import at.aau.serg.websocketbrokerdemo.GameWebSocketClient
import at.aau.serg.websocketbrokerdemo.data.properties.getDrawableIdFromName

@Composable
fun PropertyPopup(
    selectedProperty: Property,
    canBuy: Boolean,
    isMyTurn: Boolean,
    openedByClick: Boolean,
    localPlayerId: String,
    gameEvents: SnapshotStateList<String>,
    webSocketClient: GameWebSocketClient,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var rentPaid by remember { mutableStateOf(false) }

    val imageResId = getDrawableIdFromName(selectedProperty.image, context)

    AlertDialog(
        modifier = Modifier
            .width(300.dp)
            .height(400.dp),
        onDismissRequest = onDismiss,
        title = {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = selectedProperty.name,
                    style = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.Bold)
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (imageResId != 0) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(250.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(imageResId),
                            contentDescription = selectedProperty.name,
                            modifier = Modifier
                                .width(180.dp)
                                .height(240.dp),
                        )
                    }
                }
            }
        },
        confirmButton = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Button(
                    onClick = {
                        Toast.makeText(context, "❌ Purchase declined", Toast.LENGTH_SHORT).show()
                        gameEvents.add("❌ Purchase declined")
                        onDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0074cc))
                ) {
                    Text("Exit")
                }

                if (canBuy && isMyTurn) {
                    Button(
                        onClick = {
                            val name = selectedProperty.name
                            val msg = "Purchase confirmed ✅ : $name"
                            webSocketClient.sendMessage("BUY_PROPERTY:${selectedProperty.id}")
                            Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                            gameEvents.add(msg)
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0074cc))
                    ) {
                        Text("Buy")
                    }
                }

                if (selectedProperty.ownerId != null &&
                    selectedProperty.ownerId != localPlayerId &&
                    isMyTurn
                ) {
                    Button(
                        onClick = {
                            val msg = "💸 Rent paid for ${selectedProperty.name}"
                            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                            gameEvents.add(msg)

                            webSocketClient.logic().payRent(selectedProperty.id)
                            rentPaid = true
                            onDismiss()
                        },
                        enabled = !rentPaid,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFE57373),
                            disabledContainerColor = Color(0xFFBDBDBD)
                        )
                    ) {
                        Text("Pay Rent")
                    }
                }
            }
        },
        dismissButton = {}
    )
}
