package at.aau.serg.websocketbrokerdemo.ui.components.alerts

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import at.aau.serg.websocketbrokerdemo.data.ChatEntry

@Composable
fun ChatOverlay(
    chatMessages: List<ChatEntry>,
    chatInput: String,
    onChatInputChange: (String) -> Unit,
    currentPlayerId: String,
    onSendMessage: () -> Unit,
    playerColorMap: Map<String, Color>,
    onClose: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.3f))
            .padding(32.dp)
    ) {
        IconButton(
            onClick = onClose,
            modifier = Modifier.align(Alignment.TopStart)
        ) {
            Icon(Icons.Filled.ArrowBack, contentDescription = "Back", tint = Color.Black)
        }

        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxWidth()
                .background(Color.White.copy(alpha = 0.85f), shape = MaterialTheme.shapes.medium)
                .padding(16.dp)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(8.dp),
                reverseLayout = true
            ) {
                items(chatMessages.reversed()) { entry ->
                    val isOwnMessage = entry.senderId == currentPlayerId
                    val nameColor = playerColorMap[entry.senderId] ?: Color.Gray

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = if (isOwnMessage) Arrangement.End else Arrangement.Start
                    ) {
                        Box(
                            modifier = Modifier
                                .background(
                                    if (isOwnMessage) Color(0xFFDCF8C6) else Color.White,
                                    shape = MaterialTheme.shapes.medium
                                )
                                .padding(12.dp)
                                .widthIn(max = 240.dp)
                        ) {
                            Column {
                                Text(
                                    text = entry.senderName,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = nameColor
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = entry.message,
                                    fontSize = 16.sp,
                                    color = Color.Black
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                TextField(
                    value = chatInput,
                    onValueChange = onChatInputChange,
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Type your message...") }
                )
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = onSendMessage
                ) {
                    Text("Send")
                }
            }
        }
    }
}
