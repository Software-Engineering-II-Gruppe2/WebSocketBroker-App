package at.aau.serg.websocketbrokerdemo

import android.net.Uri
import android.widget.VideoView
import androidx.compose.foundation.layout.*
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.draw.shadow
import com.example.myapplication.R
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import kotlinx.coroutines.delay


@Composable
fun StartScreen(onEnterClick: () -> Unit) {
    val context = LocalContext.current
    val videoView = remember {
        VideoView(context).apply {
            setVideoURI(Uri.parse("android.resource://${context.packageName}/${R.raw.video_startseite}"))
            setOnPreparedListener {
                it.isLooping = true
                start()
                scaleX = 1.5f
                scaleY = 1.5f
            }
        }
    }

    val monopolyFont = FontFamily(Font(R.font.monopoly))

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(factory = { videoView },
            modifier = Modifier.fillMaxSize())

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 80.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "MONOPOLY",
                color = Color.White,
                fontFamily = monopolyFont,
                fontSize = 200.sp,
                style = MaterialTheme.typography.headlineLarge,
                        modifier = Modifier
                        .offset(x = 16.dp, y = 0.dp)
            )

            Spacer(modifier = Modifier.height(45.dp))

                AnimatedEnterGameButton(onClick = onEnterClick)
            }
        }
    }

@Composable
fun AnimatedEnterGameButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    var startBounce by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(2000L)
        startBounce = true
    }

    val infiniteTransition = rememberInfiniteTransition()
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (startBounce) 1.1f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    Button(
        onClick = onClick,
        modifier = Modifier
            .scale(scale)
            .padding(horizontal = 32.dp)
            .height(60.dp),
        shape = RoundedCornerShape(50.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF000000))
    ) {
        Text(
            text = "Enter Game",
            color = Color.White,
            fontSize = 24.sp,
            fontFamily = FontFamily(Font(R.font.monopoly))
        )
    }
}
