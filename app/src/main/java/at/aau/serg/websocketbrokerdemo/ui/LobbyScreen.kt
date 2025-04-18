package at.aau.serg.websocketbrokerdemo.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
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
    onRollDice: () -> Unit,
    onLogout: () -> Unit,
    onProfileClick: () -> Unit,
    onJoinGame: () -> Unit,
) {
    var showWifiIcon by remember { mutableStateOf(false) }
    var showDisconnectIcon by remember { mutableStateOf(false) }
    var wifiIconSize by remember { mutableStateOf(320.dp) }
    var diceResult by remember { mutableStateOf("?") }
    var lastProcessedLogLength by remember { mutableIntStateOf(0) }

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
                DiceRollingButton("Roll Dice", Color(0xFF3FAF3F), onRollDice, diceResult)
                AnimatedButton("Logout", Color.Red, onLogout)
                AnimatedButton("Profile", Color.Blue, onProfileClick)

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

    LaunchedEffect(log) {
        if (log.length > lastProcessedLogLength){
            val newContent = log.substring(lastProcessedLogLength)
            lastProcessedLogLength = log.length
            diceResult = parseDiceResult(newContent)
        }
    }
}

fun parseDiceResult(newContent: String): String {
    val diceRegex = "rolled (\\d+)".toRegex()
    val matchResult = diceRegex.find(newContent)
    return if (matchResult != null){
        matchResult.groupValues[1]
    } else {
        "?" // Displays ? if no dice result was found
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

@Composable
fun DiceRollingButton(text: String, color: Color, onClick: () -> Unit, diceResult: String) {
    var isPressed by remember { mutableStateOf(false) }
    var rotateAngle by remember { mutableFloatStateOf(0f) }

    val rotation by animateFloatAsState(
        targetValue = rotateAngle,
        animationSpec = tween(durationMillis = 1000, easing = FastOutSlowInEasing)
    )
    val scale by animateFloatAsState(if (isPressed) 1.1f else 1f, animationSpec = tween(150))
    val buttonColor by animateColorAsState(
        targetValue = if (isPressed) color.copy(alpha = 0.7f) else color,
        animationSpec = tween(durationMillis = 150)
    )

    Button(
        onClick = {
            isPressed = true
            rotateAngle += 720f
            onClick() // Hier wird diceResult im Log aktualisiert
        },
        modifier = Modifier.height(56.dp).scale(scale).rotate(rotation),
        shape = RoundedCornerShape(8.dp),
        colors = ButtonDefaults.buttonColors(containerColor = buttonColor)
    ) {
        Text(text, fontSize = 18.sp)
    }

    // Anzeige der geworfenen Zahl
    DiceFace(diceResult)

    LaunchedEffect(isPressed) {
        if (isPressed) {
            delay(1000)
            isPressed = false
        }
    }
}

@Composable
fun DiceFace(diceValue: String) {
    Box(
        modifier = Modifier.size(100.dp).background(Color.White, RoundedCornerShape(12.dp)).padding(10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = diceValue,
            fontSize = 36.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )
    }
}