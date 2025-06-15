package at.aau.serg.websocketbrokerdemo.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlin.random.Random
import androidx.compose.material3.Button

@Composable
fun WinScreen(onTimeout: () -> Unit) {
    val visible = remember { mutableStateOf(false) }
    val showGiftBox = remember { mutableStateOf(true) }

    // Zufällige Siegesnachricht
    val messages = listOf(
        "🏆 You are the king of real estate!",
        "🤑 All opponents are bankrupt!",
        "🎉 Megadeal! You've won!",
        "💸 The dice were on your side!"
    )
    val winMessage = remember { messages.random() }

    // Animationen
    val scale by rememberInfiniteTransition().animateFloat(
        initialValue = 1f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    val alpha by rememberInfiniteTransition().animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000),
            repeatMode = RepeatMode.Reverse
        )
    )

    // Sichtbarkeit und Timeout
    LaunchedEffect(Unit) {
        visible.value = true
        delay(10000)
        onTimeout()
    }

    LaunchedEffect(Unit) {
        visible.value = false
        delay(4000) // Geschenk zeigen
        showGiftBox.value = false
        visible.value = true
        delay(8000) // Nach Gesamtzeit zur Lobby zurück
        onTimeout()
    }

    // UI Layout
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF2196F3), // Dunkelblau
                        Color(0xFFBBDEFB)  // Hellblau
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        // 💸 Fallende Symbole im Hintergrund
        repeat(50) {
            FallingSymbol()
        }

        if (showGiftBox.value) {
            Text(
                text = "🎁",
                fontSize = 64.sp,
                modifier = Modifier
                    .scale(scale)
                    .alpha(alpha)
            )
        } else if (visible.value) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // ✨ Glitzer oben
                Text(
                    text = "✨",
                    fontSize = 28.sp,
                    color = Color.White,
                    modifier = Modifier.alpha(alpha)
                )

                Spacer(modifier = Modifier.height(8.dp))

                // 🎉 Haupttext
                Text(
                    modifier = Modifier.scale(scale),
                    text = "🎉 Congratulations, you have won! 🎉",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(16.dp))

                // 💬 Zufällige Nachricht
                Text(
                    text = winMessage,
                    fontSize = 18.sp,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                // ✨ Glitzer unten
                Text(
                    text = "✨",
                    fontSize = 28.sp,
                    color = Color.White,
                    modifier = Modifier.alpha(alpha)
                )

                Spacer(modifier = Modifier.height(24.dp))

                Button(onClick = { onTimeout() }) {
                    Text("🔁 Back to Lobby")
                }
            }
        }
    }
}

@Composable
fun FallingSymbol() {
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp
    val screenHeight = configuration.screenHeightDp

    val randomX = remember { Random.nextInt(0, screenWidth).dp }
    val duration = remember { Random.nextInt(3000, 6000) }

    val offsetY by rememberInfiniteTransition().animateFloat(
        initialValue = -50f,
        targetValue = screenHeight.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = duration, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )

    val rotation by rememberInfiniteTransition().animateFloat(
        initialValue = -10f,
        targetValue = 10f,
        animationSpec = infiniteRepeatable(
            animation = tween(500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    val symbols = listOf("💵", "🏠", "🎲", "🏦", "🤑", "\uD83D\uDC10")
    val symbol = remember { symbols.random() }

    Box(modifier = Modifier.fillMaxSize()) {
        Text(
            text = symbol,
            fontSize = 28.sp,
            modifier = Modifier
                .absoluteOffset(x = randomX, y = offsetY.dp)
                .graphicsLayer {
                    rotationZ = rotation
                }
        )
    }
}
