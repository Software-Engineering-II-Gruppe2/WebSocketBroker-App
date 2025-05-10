package at.aau.serg.websocketbrokerdemo.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.R
import kotlinx.coroutines.delay

@Composable
fun LobbyScreen(
    message: String,
    log: String,
    onMessageChange: (String) -> Unit,
    onConnect: () -> Unit,
    onDisconnect: () -> Unit,
    onSendMessage: () -> Unit,
    onLogout: () -> Unit,
    onProfileClick: () -> Unit,
    onStatisticsClick: () -> Unit,
    onLeaderboardClick: () -> Unit,
    onJoinGame: () -> Unit,
) {
    var showWifiIcon by remember { mutableStateOf(false) }
    var showDisconnectIcon by remember { mutableStateOf(false) }
    var wifiIconSize by remember { mutableStateOf(320.dp) }

    Box(modifier = Modifier.fillMaxSize()) {
        // Hintergrundbild
        Image(
            painter = painterResource(id = R.drawable.lobbybackground),
            contentDescription = "Monopoly Lobby Hintergrund",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(120.dp))

            TextField(
                value = message,
                onValueChange = onMessageChange,
                label = { Text("Enter your message") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                textStyle = LocalTextStyle.current.copy(fontSize = 18.sp)
            )

            // Buttons in einer Zeile
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                AnimatedButton("Connect", Color(0xFFFF9800)) {
                    showWifiIcon = true
                    showDisconnectIcon = false
                    wifiIconSize = 220.dp // Vergrößern des WiFi-Symbols
                    onConnect()
                }
                AnimatedButton("Disconnect", Color.Gray) {
                    showWifiIcon = false
                    showDisconnectIcon = true
                    wifiIconSize = 220.dp // Vergrößern des WiFi-Symbols
                    onDisconnect()
                }
                AnimatedButton("Send Message", Color(0xFF0074cc), onSendMessage)
                AnimatedButton("Logout", Color.Red, onLogout)
                AnimatedButton("Profile", Color.Blue, onProfileClick)
                AnimatedButton("Statistics", Color.Blue, onStatisticsClick)
                AnimatedButton("Leaderboard", Color.Blue, onLeaderboardClick)


                // Join Game button
                Spacer(modifier = Modifier.height(16.dp))

                AnimatedButton("Join Game", Color(0xFF9C27B0)) {
                    onJoinGame()
                }
            }

            // WiFi Icon Animation (Connect)
            AnimatedVisibility(
                visible = showWifiIcon,
                enter = fadeIn(animationSpec = tween(500)) + scaleIn(initialScale = 0.5f, animationSpec = tween(500)),
                exit = fadeOut(animationSpec = tween(500)) + scaleOut(targetScale = 0.5f, animationSpec = tween(500))
            ) {
                Image(
                    painter = painterResource(id = R.drawable.wifi_icon),
                    contentDescription = "WiFi Symbol",
                    modifier = Modifier
                        .size(wifiIconSize) // Größe des WiFi-Symbols
                        .padding(top = 16.dp)
                )
            }

            // Disconnect Icon Animation
            AnimatedVisibility(
                visible = showDisconnectIcon,
                enter = fadeIn(animationSpec = tween(500)) + scaleIn(initialScale = 0.5f, animationSpec = tween(500)),
                exit = fadeOut(animationSpec = tween(500)) + scaleOut(targetScale = 0.5f, animationSpec = tween(500))
            ) {
                Image(
                    painter = painterResource(id = R.drawable.wifi_notconnected), // Hier das neue Disconnect-Symbol
                    contentDescription = "Disconnect Symbol",
                    modifier = Modifier
                        .size(wifiIconSize) // Größe des Disconnect-Symbols
                        .padding(top = 16.dp)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Log-Ausgabe mit weißem Text und fett
            Text(
                text = log,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }

    // Verzögerung bevor das WiFi-Symbol verschwindet
    LaunchedEffect(showWifiIcon, showDisconnectIcon) {
        if (showWifiIcon || showDisconnectIcon) {
            delay(1000) // WiFi- oder Disconnect-Symbol bleibt 2 Sekunden lang sichtbar
            showWifiIcon = false
            showDisconnectIcon = false
        }
    }
}

@Composable
fun AnimatedButton(text: String, color: Color, onClick: () -> Unit) {
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(if (isPressed) 1.1f else 1f, animationSpec = tween(150))
    val buttonColor by animateColorAsState(
        targetValue = if (isPressed) color.copy(alpha = 0.7f) else color,
        animationSpec = tween(durationMillis = 150)
    )

    Button(
        onClick = {
            isPressed = true
            onClick()
        },
        modifier = Modifier
            .height(56.dp)
            .scale(scale),
        shape = RoundedCornerShape(8.dp),
        colors = ButtonDefaults.buttonColors(containerColor = buttonColor)
    ) {
        Text(text, fontSize = 18.sp)
    }

    LaunchedEffect(isPressed) {
        if (isPressed) {
            delay(150)
            isPressed = false
        }
    }
}