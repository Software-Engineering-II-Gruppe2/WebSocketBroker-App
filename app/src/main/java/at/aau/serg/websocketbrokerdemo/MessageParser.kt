package at.aau.serg.websocketbrokerdemo

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import at.aau.serg.websocketbrokerdemo.data.messages.ChatMessage
import at.aau.serg.websocketbrokerdemo.data.messages.DiceRollMessage
import at.aau.serg.websocketbrokerdemo.data.messages.DrawnCardMessage
import at.aau.serg.websocketbrokerdemo.data.PlayerMoney
import at.aau.serg.websocketbrokerdemo.data.messages.CheatMessage
import at.aau.serg.websocketbrokerdemo.data.messages.ClearChatMessage
import at.aau.serg.websocketbrokerdemo.data.messages.HasWonMessage
import at.aau.serg.websocketbrokerdemo.data.messages.TaxPaymentMessage
import at.aau.serg.websocketbrokerdemo.data.messages.DealProposalMessage
import at.aau.serg.websocketbrokerdemo.data.messages.DealResponseMessage
import at.aau.serg.websocketbrokerdemo.data.messages.GiveUpMessage
import com.google.gson.JsonSyntaxException


class MessageParser(
    private val gson: Gson,
    private val getPlayers: () -> List<PlayerMoney>,
    private val onTaxPayment: (playerName: String, amount: Int, taxType: String) -> Unit,
    private val onPlayerPassedGo: (playerName: String) -> Unit,
    private val onPropertyBought: (raw: String) -> Unit,
    private val onGameStateReceived: (List<PlayerMoney>) -> Unit,
    private val onGiveUpReceived: (userId: String) -> Unit,
    private val onPlayerTurn: (sessionId: String) -> Unit,
    private val onDiceRolled: (playerId: String, value: Int, manual: Boolean, isPasch: Boolean) -> Unit,
    private val onCardDrawn: (playerId: String, cardType: String, description: String, cardId: Int) -> Unit,
    private val onChatMessageReceived: (playerId: String, message: String) -> Unit,
    private val onCheatMessageReceived: (playerId: String, message: String) -> Unit,
    private val onClearChat: () -> Unit,
    private val onHasWon: (winnerId: String) -> Unit,
    private val onMessageReceived: (text: String) -> Unit,
    private val onDealProposal: (DealProposalMessage) -> Unit,
    private val onDealResponse: (DealResponseMessage) -> Unit,
    private val onReset: () -> Unit
    ) {
    fun parse(text: String) {
        // 1) TAX_PAYMENT:
        if(parseTaxPayment(text)) return
        // 2) PASSED GO:
        if (parsePassedGo(text)) return
        // 3) PROPERTY_BOUGHT:
        if (parsePropertyBought(text)) return
        // 4) PROPERTY_SOLD:
        if (parsePropertySold(text)) return
        // 5) GAME_STATE:
        if (parseGameState(text)) return
        // 6) PLAYER_TURN:
        if (parsePlayerTurn(text)) return
        // 7) DICE_ROLL:
        if (parseDiceRoll(text)) return
        // 8) CARD_DRAWN:
        if (parseCardDrawn(text)) return
        // 9) CHAT_MESSAGE:
        if (parseChatMessage(text)) return
        // 10) CHEAT_MESSAGE:
        if (parseCheatMessage(text)) return
        // 11) DEAL_PROPOSAL:
        if (parseDealProposal(text)) return
        // 12) DEAL_RESPONSE:
        if (parseDealResponse(text)) return
        // 13) HAS_WON:
        if (parseHasWon(text)) return
        // 14) CLEAR_CHAT:
        if (parseClearMessage(text)) return
        // 15) GIVE_UP:
        if (parseGiveUp(text)) return
        // 16) RESET:
        if (parseReset(text)) return
        // 17) FALLBACK:
        onMessageReceived(text)
    }

    fun parseTaxPayment(text: String): Boolean {
        return try {
            val taxMessage = gson.fromJson(text, TaxPaymentMessage::class.java)
            if (taxMessage.type == "TAX_PAYMENT") {
                val playerName = getPlayers().find { it.id == taxMessage.playerId }?.name ?: "Unknown Player"
                onTaxPayment(playerName, taxMessage.amount, taxMessage.taxType)
                true
            } else false
        } catch (_: JsonSyntaxException) { false }
    }

    fun parsePassedGo(text: String): Boolean {
        return try {
            if (text.contains("passed GO and collected")) {
                val playerId = text.substringAfter("Player ").substringBefore(" passed")
                val playerName = getPlayers().find { it.id == playerId }?.name ?: "Unknown Player"
                onPlayerPassedGo(playerName)
                true
            } else false
        } catch (_: JsonSyntaxException) { false }
    }

    fun parsePropertyBought(text: String): Boolean {
        return try {
            if (text.contains("PROPERTY_BOUGHT")) {
                onPropertyBought(text)
                true
            } else false
        } catch (_: JsonSyntaxException) { false }
    }

    fun parsePropertySold(text: String): Boolean {
        return try {
            if (text.contains("sold property")) {
                onPropertyBought(text) // Reuse the property bought listener to update the UI
                true
            } else false
        } catch (_: JsonSyntaxException) { false }
    }

    fun parseGameState(text: String): Boolean {
        return try {
            if (text.startsWith("GAME_STATE:")) {
                val jsonData = text.removePrefix("GAME_STATE:")
                val type = object : TypeToken<List<PlayerMoney>>() {}.type
                val players = gson.fromJson<List<PlayerMoney>>(jsonData, type)
                onGameStateReceived(players)
                true
            } else false
        } catch (_: JsonSyntaxException) { false }

    }

    fun parsePlayerTurn(text: String): Boolean {
        return try {
            if (text.startsWith("PLAYER_TURN")) {
                val sessionId = text.substringAfter("PLAYER_TURN:")
                onPlayerTurn(sessionId)
                true
            } else false
        } catch (_: JsonSyntaxException) { false }
    }

    fun parseDiceRoll(text: String): Boolean {
        return try {
            val roll = gson.fromJson(text, DiceRollMessage::class.java)
            if (roll.type == "DICE_ROLL") {
                onDiceRolled(roll.playerId, roll.value, roll.manual, roll.isPasch)
                true
            } else false
        } catch (_: JsonSyntaxException) { false }
    }

    fun parseCardDrawn(text: String): Boolean {
        return try {
            val drawn = gson.fromJson(text, DrawnCardMessage::class.java)
            if (drawn.type == "CARD_DRAWN") {
                val desc = drawn.card["description"].asString
                val cardId = drawn.card["id"].asInt
                onCardDrawn(drawn.playerId, drawn.cardType, desc, cardId)
                true
            } else false
        } catch (_: JsonSyntaxException) { false }
    }

    fun parseChatMessage(text: String): Boolean {
        return try {
            val chat = gson.fromJson(text, ChatMessage::class.java)
            if (chat.type == "CHAT_MESSAGE") {
                onChatMessageReceived(chat.playerId, chat.message)
                true
            } else false
        } catch (_: JsonSyntaxException) { false }
    }

    fun parseCheatMessage(text: String): Boolean {
        return try {
            val cheat = gson.fromJson(text, CheatMessage::class.java)
            if (cheat.type == "CHEAT_MESSAGE") {
                onCheatMessageReceived(cheat.playerId, cheat.message)
                true
            } else false
        } catch (_: JsonSyntaxException) { false }
    }

    fun parseDealProposal(text: String): Boolean {
        return try {
            val proposal = gson.fromJson(text, DealProposalMessage::class.java)
            if (proposal.type == "DEAL_PROPOSAL") {
                onDealProposal.invoke(proposal)
                true
            } else false
        } catch (_: JsonSyntaxException) { false }
    }

    fun parseDealResponse(text: String): Boolean {
        return try {
            val response = gson.fromJson(text, DealResponseMessage::class.java)
            if (response.type == "DEAL_RESPONSE") {
                onDealResponse.invoke(response)
                true
            } else false
        } catch (_: JsonSyntaxException) { false }
    }

    fun parseHasWon(text: String): Boolean {
        return try {
            val won = gson.fromJson(text, HasWonMessage::class.java)
            if (won.type == "HAS_WON") {
                onHasWon(won.userId)
                true
            } else false
        } catch (_: JsonSyntaxException) { false }
    }

    fun parseClearMessage(text: String): Boolean {
        return try {
            val clearMessage = gson.fromJson(text, ClearChatMessage::class.java)
            if (clearMessage.type == "CLEAR_CHAT") {
                onClearChat()
                true
            } else false
        } catch (_: JsonSyntaxException) { false }
    }

    private fun parseGiveUp(text: String): Boolean {
        return try {
            val giveUp = gson.fromJson(text, GiveUpMessage::class.java)
            if (giveUp.type == "GIVE_UP") {
                onGiveUpReceived(giveUp.userId)
                true
            } else false
        } catch (_: JsonSyntaxException) { false }
    }

    fun parseReset(text: String): Boolean {
        return try {
            if (text.contains("\"type\":\"RESET\"")) {
                onReset()
                true
            } else false
        } catch (_: JsonSyntaxException) { false }
    }
}
