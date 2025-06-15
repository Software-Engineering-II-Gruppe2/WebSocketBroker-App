package at.aau.serg.websocketbrokerdemo.ui.components.dialogs

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun ActionMenuDialog(
    isMyTurn: Boolean,
    turnEnded: Boolean,
    onDismiss: () -> Unit,
    onStartDeal: () -> Unit,
    onBackToLobby: () -> Unit,
    onGiveUp: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {},
        dismissButton = {},
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (isMyTurn && !turnEnded) {
                    Button(
                        onClick = {
                            onStartDeal()
                            onDismiss()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF9C27B0)) // Violett
                    ) {
                        Text("Start Deal", color = Color.White)
                    }
                }

                Button(
                    onClick = {
                        onBackToLobby()
                        onDismiss()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0074cc)) // Blau
                ) {
                    Text("Back to Lobby", color = Color.White)
                }

                Button(
                    onClick = {
                        onGiveUp()
                        onDismiss()
                    },
                    enabled = isMyTurn,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isMyTurn) Color.Red else Color.Gray
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Give Up", color = Color.White)
                }

                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.LightGray)
                ) {
                    Text("Close", color = Color.Black)
                }
            }
        }
    )
}
