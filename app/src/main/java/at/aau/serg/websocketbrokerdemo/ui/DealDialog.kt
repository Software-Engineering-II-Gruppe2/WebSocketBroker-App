package at.aau.serg.websocketbrokerdemo.ui

import android.content.Context
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import at.aau.serg.websocketbrokerdemo.data.PlayerMoney
import at.aau.serg.websocketbrokerdemo.data.messages.DealProposalMessage
import androidx.compose.runtime.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.sp
import at.aau.serg.websocketbrokerdemo.data.properties.Property
import at.aau.serg.websocketbrokerdemo.data.properties.getDrawableIdFromName
import com.example.myapplication.R


@Composable
fun DealDialog(
    players: List<PlayerMoney>,
    senderId: String,
    allProperties: List<Property>,
    receiver: PlayerMoney?,
    avatarMap: Map<String, Int>,
    initialRequested: List<Int> = emptyList(),
    initialOffered: List<Int> = emptyList(),
    initialMoney: Int = 0,
    onReceiverChange: (PlayerMoney) -> Unit = {},
    onSendDeal: (DealProposalMessage) -> Unit,
    onDismiss: () -> Unit
) {
    val offeredProperties = remember { mutableStateListOf<Int>() }
    val requestedProperties = remember { mutableStateListOf<Int>() }

    LaunchedEffect(initialOffered) {
        offeredProperties.clear()
        offeredProperties.addAll(initialOffered)
    }
    LaunchedEffect(initialRequested) {
        requestedProperties.clear()
        requestedProperties.addAll(initialRequested)
    }

    var offeredMoney by remember { mutableStateOf(initialMoney.toString()) }

    val offeredTotalValue by remember {
        derivedStateOf {
            allProperties.filter { it.id in offeredProperties }.sumOf { it.purchasePrice }
        }
    }

    val requestedTotalValue by remember {
        derivedStateOf {
            allProperties.filter { it.id in requestedProperties }.sumOf { it.purchasePrice }
        }
    }

    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Make a Deal") },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                if (receiver == null) {
                    Text("Choose a player:")
                    Spacer(Modifier.height(8.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        players.forEach { player ->
                            val imageRes = avatarMap[player.id] ?: R.drawable.player_red

                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(player.name)

                                Spacer(Modifier.height(4.dp))

                                Image(
                                    painter = painterResource(id = imageRes),
                                    contentDescription = "${player.name}'s figure",
                                    modifier = Modifier
                                        .size(64.dp)
                                        .clip(CircleShape)
                                        .clickable { onReceiverChange(player) }
                                )
                            }
                        }
                    }
                } else {
                    Text("Dealing with: ${receiver.name}")
                    Spacer(Modifier.height(12.dp))

                    // Eigene Grundstücke (Bilder mit Checkbox)
                    Text("Your properties:")
                    val senderProperties = allProperties.filter { it.ownerId == senderId || it.id in initialOffered
                    }
                    DealPropertyRow(
                        properties = senderProperties,
                        selectedIds = offeredProperties,
                        onToggle = { id, checked ->
                            if (checked) offeredProperties.add(id) else offeredProperties.remove(id)
                        },
                        context = context
                    )

                    Spacer(Modifier.height(12.dp))

                    //Empfänger
                    Text("${receiver.name}'s properties:")
                    val receiverProperties = allProperties.filter { it.ownerId == receiver.id || it.id in initialRequested
                    }
                    DealPropertyRow(
                        properties = receiverProperties,
                        selectedIds = requestedProperties,
                        onToggle = { id, checked ->
                            if (checked) requestedProperties.add(id) else requestedProperties.remove(id)
                        },
                        context = context
                    )

                    Spacer(Modifier.height(8.dp))
                    Text("Your selected properties total: €$offeredTotalValue")

                    Spacer(Modifier.height(8.dp))
                    Text("${receiver.name}'s selected properties total: €$requestedTotalValue")

                    Spacer(Modifier.height(12.dp))
                    OutlinedTextField(
                        value = offeredMoney,
                        onValueChange = {
                            if (it.all { ch -> ch.isDigit() }) {
                                offeredMoney = it
                            }
                        },
                        label = { Text("You offer (€)") },
                        keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        confirmButton = {
            if (receiver != null) {
                Button(onClick = {
                    onSendDeal(
                        DealProposalMessage(
                            type = "DEAL_PROPOSAL",
                            fromPlayerId = senderId,
                            toPlayerId = receiver.id,
                            offeredPropertyIds = offeredProperties.toList(),
                            requestedPropertyIds = requestedProperties.toList(),
                            offeredMoney = offeredMoney.toIntOrNull() ?: 0
                        )
                    )
                }) {
                    Text("Send Deal")
                }
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun DealPropertyRow(
    properties: List<Property>,
    selectedIds: MutableList<Int>,
    onToggle: (Int, Boolean) -> Unit,
    context: Context
) {
    Row(
        modifier = Modifier
            .horizontalScroll(rememberScrollState())
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        properties.forEach { property ->
            val isSelected = selectedIds.contains(property.id)
            val imageResId = getDrawableIdFromName(property.image, context)

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .clickable { onToggle(property.id, !isSelected) }
                ) {
                    if (imageResId != 0) {
                        Image(
                            painter = painterResource(id = imageResId),
                            contentDescription = property.name,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Fit,
                            alpha = if (isSelected) 1f else 0.4f
                        )
                    } else {
                        Text(property.name, fontSize = 10.sp, modifier = Modifier.align(Alignment.Center))
                    }
                }
                Checkbox(
                    checked = isSelected,
                    onCheckedChange = { checked -> onToggle(property.id, checked) }
                )
            }
        }
    }
}