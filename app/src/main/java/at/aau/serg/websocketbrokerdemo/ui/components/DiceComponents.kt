package at.aau.serg.websocketbrokerdemo.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

@Composable
fun DiceRollingButton(
    text: String,
    color: Color,
    onClick: () -> Unit,
    diceValue: Int?,
    enabled: Boolean = true,
    onRollComplete: () -> Unit = {}
) {
    var isPressed by remember { mutableStateOf(false) }
    var rotateAngle by remember { mutableFloatStateOf(0f) }

    val rotation by animateFloatAsState(
        targetValue = rotateAngle,
        animationSpec = tween(durationMillis = 1000, easing = FastOutSlowInEasing)
    )
    val scale by animateFloatAsState(
        if (isPressed && enabled) 1.1f else 1f,
        animationSpec = tween(150)
    )
    val buttonColor by animateColorAsState(
        targetValue = when {
            !enabled -> Color.Gray
            isPressed -> color.copy(alpha = 0.7f)
            else -> color
        },
        animationSpec = tween(durationMillis = 150)
    )

    Button(
        onClick = {
            if (!enabled) return@Button
            isPressed = true
            rotateAngle += 720f
            onClick()
            onRollComplete()
        },
        enabled = enabled,
        modifier = Modifier
            .height(56.dp)
            .scale(scale)
            .rotate(rotation),
        shape = RoundedCornerShape(8.dp),
        colors = ButtonDefaults.buttonColors(containerColor = buttonColor)
    ) {
        Text(text, fontSize = 18.sp)
    }

    Spacer(modifier = Modifier.height(16.dp))

    DiceFace(diceValue)

    LaunchedEffect(isPressed) {
        if (isPressed) {
            delay(1000)
            isPressed = false
        }
    }
}

@Composable
fun DiceFace(diceValue: Int?) {
    Box(
        modifier = Modifier
            .size(100.dp)
            .background(Color.White, RoundedCornerShape(12.dp))
            .padding(10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = diceValue?.toString() ?: "?",
            fontSize = 36.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )
    }
}
