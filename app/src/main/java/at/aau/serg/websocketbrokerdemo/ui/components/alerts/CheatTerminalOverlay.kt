package at.aau.serg.websocketbrokerdemo.ui.components.alerts

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import at.aau.serg.websocketbrokerdemo.data.CheatEntry

@Composable
fun CheatTerminalOverlay(
    cheatMessages: List<CheatEntry>,
    cheatInput: String,
    onCheatInputChange: (String) -> Unit,
    onSendCheat: () -> Unit,
    onClose: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.65f))
            .padding(32.dp)
    ) {
        IconButton(
            onClick = onClose,
            modifier = Modifier.align(Alignment.TopStart)
        ) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color(0xFFCCFF90))
        }

        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxWidth()
                .background(Color.Black.copy(alpha = 0.65f), shape = MaterialTheme.shapes.medium)
                .padding(16.dp)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(8.dp),
                reverseLayout = true
            ) {
                items(cheatMessages.reversed()) { entry ->
                    Text(
                        text = "${entry.senderName.lowercase()}@monopoly > ${entry.message}",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 14.sp,
                        color = Color(0xFF00FF00)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                TextField(
                    value = cheatInput,
                    onValueChange = onCheatInputChange,
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Type your cheat code...") }
                )
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = onSendCheat
                ) {
                    Text("Send")
                }
            }
        }
    }
}
