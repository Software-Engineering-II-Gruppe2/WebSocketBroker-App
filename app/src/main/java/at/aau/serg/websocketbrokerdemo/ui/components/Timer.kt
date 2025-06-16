/*  ui/components/TurnTimer.kt  */
package at.aau.serg.websocketbrokerdemo.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun TurnTimer(
    seconds: Int,
    modifier: Modifier = Modifier
) {
    val color by animateColorAsState(
        targetValue = when {
            seconds > 20 -> Color(0xFF4CAF50) // grün
            seconds > 10 -> Color(0xFFFFA000) // gelb/orange
            else         -> Color(0xFFD32F2F) // rot
        },
        animationSpec = tween(300)
    )

    Box(
        modifier = modifier
            .size(60.dp)
            .background(color, RoundedCornerShape(12.dp)),
        contentAlignment = androidx.compose.ui.Alignment.Center
    ) {
        Text(
            text      = seconds.toString(),
            fontSize  = 24.sp,
            color     = Color.White
        )
    }
}
