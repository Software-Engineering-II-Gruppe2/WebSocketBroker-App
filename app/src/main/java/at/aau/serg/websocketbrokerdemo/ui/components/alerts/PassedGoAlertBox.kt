package at.aau.serg.websocketbrokerdemo.ui.components.alerts

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
    fun PassedGoAlertBox(playerName: String) {
        Box(
        modifier = Modifier
        .fillMaxWidth()
        .fillMaxHeight(0.3f)
        .background(Color(0xFF4CAF50).copy(alpha = 0.9f))
        .padding(16.dp),
        contentAlignment = Alignment.Center
        ) {
         Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
        ) {
        Text(
            text = "Glückwunsch!",
            style = TextStyle(
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        )
        Spacer(modifier = Modifier.height(8.dp))
             Text(
                 text = "$playerName fuhr über los und erhält 200€!",
                 style = TextStyle(
                     fontSize = 20.sp,
                     color = Color.White
                 )
             )
        }
    }
}