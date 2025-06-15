package at.aau.serg.websocketbrokerdemo

import com.google.gson.Gson
import at.aau.serg.websocketbrokerdemo.data.PlayerMoney
import at.aau.serg.websocketbrokerdemo.data.messages.DealProposalMessage
import at.aau.serg.websocketbrokerdemo.data.messages.DealResponseMessage
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class MessageParserTest {

    private lateinit var gson: Gson

    private val dummyPlayers = listOf(
        PlayerMoney(id = "p1", name = "Alice", money = 0, position = 0),
        PlayerMoney(id = "p2", name = "Bob",   money = 0, position = 1)
    )

    @BeforeEach
    fun setUp() {
        gson = Gson()
    }

    @Test
    fun `parse TAX_PAYMENT JSON invokes onTaxPayment with correct args`() {
        val json = """{"type":"TAX_PAYMENT","playerId":"p1","amount":250,"taxType":"INCOME"}"""
        var invoked = false
        var capturedName: String? = null
        var capturedAmt: Int? = null
        var capturedType: String? = null

        val parser = MessageParser(
            gson = gson,
            getPlayers = { dummyPlayers },
            onTaxPayment = { name, amt, type ->
                invoked = true
                capturedName = name
                capturedAmt = amt
                capturedType = type
            },
            onPlayerPassedGo = { fail("should not hit passed GO") },
            onPropertyBought = { fail("should not hit property bought") },
            onGameStateReceived = { fail("should not hit game state") },
            onPlayerTurn = { fail("should not hit player turn") },
            onDiceRolled = { _, _, _, _ -> fail("should not hit dice roll") },
            onCardDrawn = { _, _, _, _ -> fail("should not hit card drawn") },
            onChatMessageReceived = { _, _ -> fail("should not hit chat") },
            onCheatMessageReceived = { _, _ -> fail("should not hit cheat") },
            onClearChat = { },
            onHasWon = { _ -> },
            onMessageReceived = { fail("should not hit fallback") },
            onDealProposal = { _: DealProposalMessage -> },
            onGiveUpReceived = { fail("should not hit onGiveUp") },
            onDealResponse = { _: DealResponseMessage -> },
            onReset = {}
        )

        parser.parse(json)

        assertTrue(invoked, "onTaxPayment must be invoked")
        assertEquals("Alice", capturedName)
        assertEquals(250, capturedAmt)
        assertEquals("INCOME", capturedType)
    }

    @Test
    fun `parse raw PASS_GO text invokes onPlayerPassedGo with player name`() {
        val raw = "Player p2 passed GO and collected 200"
        var invoked = false
        var capturedName: String? = null

        val parser = MessageParser(
            gson = gson,
            getPlayers = { dummyPlayers },
            onTaxPayment = { _, _, _ -> fail() },
            onPlayerPassedGo = { name ->
                invoked = true
                capturedName = name
            },
            onPropertyBought = { fail() },
            onGameStateReceived = { fail() },
            onPlayerTurn = { fail() },
            onDiceRolled = { _, _, _, _ -> fail() },
            onCardDrawn = { _, _, _, _ -> fail() },
            onChatMessageReceived = { _, _ -> fail() },
            onCheatMessageReceived = { _, _ -> fail("should not hit cheat") },
            onClearChat = { },
            onHasWon = { _ -> },
            onMessageReceived = { fail() },
            onDealProposal = { _: DealProposalMessage -> },
            onGiveUpReceived = { fail("should not hit onGiveUp") },
            onDealResponse = { _: DealResponseMessage -> },
            onReset = {}
        )

        parser.parse(raw)

        assertTrue(invoked, "onPlayerPassedGo must be invoked")
        assertEquals("Bob", capturedName)
    }

    @Test
    fun `parse raw PROPERTY_BOUGHT text invokes onPropertyBought with raw message`() {
        val raw = "XXX PROPERTY_BOUGHT YYY"
        var invoked = false
        var capturedRaw: String? = null

        val parser = MessageParser(
            gson = gson,
            getPlayers = { dummyPlayers },
            onTaxPayment = { _, _, _ -> fail() },
            onPlayerPassedGo = { fail() },
            onPropertyBought = { msg ->
                invoked = true
                capturedRaw = msg
            },
            onGameStateReceived = { fail() },
            onPlayerTurn = { fail() },
            onDiceRolled = { _, _, _, _ -> fail() },
            onCardDrawn = { _, _, _, _ -> fail() },
            onChatMessageReceived = { _, _ -> fail() },
            onCheatMessageReceived = { _, _ -> fail("should not hit cheat") },
            onClearChat = { },
            onHasWon = { _ -> },
            onMessageReceived = { fail() },
            onDealProposal = { _: DealProposalMessage -> },
            onGiveUpReceived = { fail("should not hit onGiveUp") },
            onDealResponse = { _: DealResponseMessage -> },
            onReset = {}
        )

        parser.parse(raw)

        assertTrue(invoked)
        assertEquals(raw, capturedRaw)
    }

    @Test
    fun `parse GAME_STATE JSON invokes onGameStateReceived with list of PlayerMoney`() {
        val jsonList = "[]"
        val raw = "GAME_STATE:$jsonList"
        var invoked = false
        var capturedList: List<PlayerMoney>? = null

        val parser = MessageParser(
            gson = gson,
            getPlayers = { dummyPlayers },
            onTaxPayment = { _, _, _ -> fail() },
            onPlayerPassedGo = { fail() },
            onPropertyBought = { fail() },
            onGameStateReceived = { list ->
                invoked = true
                capturedList = list
            },
            onPlayerTurn = { fail() },
            onDiceRolled = { _, _, _, _ -> fail() },
            onCardDrawn = { _, _, _, _ -> fail() },
            onChatMessageReceived = { _, _ -> fail() },
            onCheatMessageReceived = { _, _ -> fail("should not hit cheat") },
            onClearChat = { },
            onHasWon = { _ -> },
            onMessageReceived = { fail() },
            onDealProposal = { _: DealProposalMessage -> },
            onGiveUpReceived = { fail("should not hit onGiveUp") },
            onDealResponse = { _: DealResponseMessage -> },
            onReset = {}
        )

        parser.parse(raw)

        assertTrue(invoked)
        assertNotNull(capturedList)
        assertEquals(0, capturedList!!.size)
    }

    @Test
    fun `parse PLAYER_TURN text invokes onPlayerTurn with session ID`() {
        val raw = "PLAYER_TURN:session42"
        var invoked = false
        var capturedId: String? = null

        val parser = MessageParser(
            gson = gson,
            getPlayers = { dummyPlayers },
            onTaxPayment = { _, _, _ -> fail() },
            onPlayerPassedGo = { fail() },
            onPropertyBought = { fail() },
            onGameStateReceived = { fail() },
            onPlayerTurn = { id ->
                invoked = true
                capturedId = id
            },
            onDiceRolled = { _, _, _, _ -> fail() },
            onCardDrawn = { _, _, _, _ -> fail() },
            onChatMessageReceived = { _, _ -> fail() },
            onCheatMessageReceived = { _, _ -> fail("should not hit cheat") },
            onClearChat = { },
            onHasWon = { _ -> },
            onMessageReceived = { fail() },
            onDealProposal = { _: DealProposalMessage -> },
            onGiveUpReceived = { fail("should not hit onGiveUp") },
            onDealResponse = { _: DealResponseMessage -> },
            onReset = {}
        )

        parser.parse(raw)

        assertTrue(invoked)
        assertEquals("session42", capturedId)
    }

    @Test
    fun `parse DICE_ROLL JSON invokes onDiceRolled with correct values`() {
        val json = """{"type":"DICE_ROLL","playerId":"p1","value":6,"manual":false,"isPasch":false}"""
        var invoked = false
        var capturedPid: String? = null
        var capturedVal: Int? = null

        val parser = MessageParser(
            gson = gson,
            getPlayers = { dummyPlayers },
            onTaxPayment = { _, _, _ -> fail() },
            onPlayerPassedGo = { fail() },
            onPropertyBought = { fail() },
            onGameStateReceived = { fail() },
            onPlayerTurn = { fail() },
            onDiceRolled = { pid, v, _, _ ->
                invoked = true
                capturedPid = pid
                capturedVal = v
            },
            onCardDrawn = { _, _, _, _ -> fail() },
            onChatMessageReceived = { _, _ -> fail() },
            onCheatMessageReceived = { _, _ -> fail("should not hit cheat") },
            onClearChat = { },
            onHasWon = { _ -> },
            onMessageReceived = { fail() },
            onDealProposal = { _: DealProposalMessage -> },
            onGiveUpReceived = { fail("should not hit onGiveUp") },
            onDealResponse = { _: DealResponseMessage -> },
            onReset = {}
        )

        parser.parse(json)

        assertTrue(invoked)
        assertEquals("p1", capturedPid)
        assertEquals(6, capturedVal)
    }

    @Test
    fun `parse CARD_DRAWN JSON invokes onCardDrawn with details`() {
        val json = """
        {
          "type":"CARD_DRAWN",
          "playerId":"p2",
          "cardType":"CHANCE",
          "card":{"id":2,"description":"You win!"}
        }
        """.trimIndent()
        var invoked = false
        var pid: String? = null
        var type: String? = null
        var desc: String? = null
        var id: Int? = null

        val parser = MessageParser(
            gson = gson,
            getPlayers = { dummyPlayers },
            onTaxPayment = { _, _, _ -> fail() },
            onPlayerPassedGo = { fail() },
            onPropertyBought = { fail() },
            onGameStateReceived = { fail() },
            onPlayerTurn = { fail() },
            onDiceRolled = { _, _, _, _ -> fail() },
            onCardDrawn = { p, t, d, i ->
                invoked = true
                pid = p
                type = t
                desc = d
                id = i
            },
            onChatMessageReceived = { _, _ -> fail() },
            onCheatMessageReceived = { _, _ -> fail("should not hit cheat") },
            onClearChat = { },
            onHasWon = { _ -> },
            onMessageReceived = { fail() },
            onDealProposal = { _: DealProposalMessage -> },
            onGiveUpReceived = { fail("should not hit onGiveUp") },
            onDealResponse = { _: DealResponseMessage -> },
            onReset = {}
        )

        parser.parse(json)

        assertTrue(invoked)
        assertEquals("p2", pid)
        assertEquals("CHANCE", type)
        assertEquals("You win!", desc)
        assertEquals(2, id)
    }

    @Test
    fun `parse CHAT_MESSAGE JSON invokes onChatMessageReceived with playerId and text`() {
        val json = """{"type":"CHAT_MESSAGE","playerId":"p1","message":"Hey!"}"""
        var invoked = false
        var pid: String? = null
        var msg: String? = null

        val parser = MessageParser(
            gson = gson,
            getPlayers = { dummyPlayers },
            onTaxPayment = { _, _, _ -> fail() },
            onPlayerPassedGo = { fail() },
            onPropertyBought = { fail() },
            onGameStateReceived = { fail() },
            onPlayerTurn = { fail() },
            onDiceRolled = { _, _, _, _ -> fail() },
            onCardDrawn = { _, _, _, _ -> fail() },
            onChatMessageReceived = { p, m ->
                invoked = true
                pid = p
                msg = m
            },
            onCheatMessageReceived = { _, _ -> fail("should not hit cheat") },
            onClearChat = { },
            onHasWon = { _ -> },
            onMessageReceived = { fail("should not hit fallback") },
            onDealProposal = { _: DealProposalMessage -> },
            onGiveUpReceived = { fail("should not hit onGiveUp") },
            onDealResponse = { _: DealResponseMessage -> },
            onReset = {}
        )

        parser.parse(json)

        assertTrue(invoked)
        assertEquals("p1", pid)
        assertEquals("Hey!", msg)
    }

    @Test
    fun `parse CHEAT_MESSAGE JSON invokes onCheatMessageReceived with playerId and text`() {
        val json = """{"type":"CHEAT_MESSAGE","playerId":"p2","message":"kill all"}"""
        var invoked = false
        var pid: String? = null
        var msg: String? = null

        val parser = MessageParser(
            gson = gson,
            getPlayers = { dummyPlayers },
            onTaxPayment = { _, _, _ -> fail() },
            onPlayerPassedGo = { fail() },
            onPropertyBought = { fail() },
            onGameStateReceived = { fail() },
            onPlayerTurn = { fail() },
            onDiceRolled = { _, _, _, _ -> fail() },
            onCardDrawn = { _, _, _, _ -> fail() },
            onChatMessageReceived = { _, _ -> fail() },
            onCheatMessageReceived = { p, m ->
                invoked = true
                pid = p
                msg = m
            },
            onClearChat = { },
            onHasWon = { _ -> },
            onMessageReceived = { fail("should not hit fallback") },
            onDealProposal = { _: DealProposalMessage -> },
            onGiveUpReceived = { fail("should not hit onGiveUp") },
            onDealResponse = { _: DealResponseMessage -> },
            onReset = {}
        )

        parser.parse(json)

        assertTrue(invoked, "onCheatMessageReceived must be invoked")
        assertEquals("p2", pid)
        assertEquals("kill all", msg)
    }

    @Test
    fun `parse CLEAR_CHAT JSON invokes onClearChat`() {
        val json = """{"type":"CLEAR_CHAT","reason":"Reset"}"""
        var invoked = false

        val parser = MessageParser(
            gson = gson,
            getPlayers = { dummyPlayers },
            onTaxPayment = { _, _, _ -> fail() },
            onPlayerPassedGo = { fail() },
            onPropertyBought = { fail() },
            onGameStateReceived = { fail() },
            onPlayerTurn = { fail() },
            onDiceRolled = { _, _, _, _ -> fail() },
            onCardDrawn = { _, _, _, _ -> fail() },
            onChatMessageReceived = { _, _ -> fail() },
            onCheatMessageReceived = { _, _ -> fail() },
            onClearChat = { invoked = true },
            onHasWon = { _ -> },
            onMessageReceived = { fail() },
            onDealProposal = { _: DealProposalMessage -> },
            onGiveUpReceived = { fail("should not hit onGiveUp") },
            onDealResponse = { _: DealResponseMessage -> },
            onReset = {}
        )

        parser.parse(json)
        assertTrue(invoked)
    }

    @Test
    fun `parse HAS_WON JSON invokes onHasWon with winnerId`() {
        val json = """{"type":"HAS_WON","userId":"p2"}"""
        var invoked = false
        var winner: String? = null

        val parser = MessageParser(
            gson = gson,
            getPlayers = { dummyPlayers },
            onTaxPayment = { _, _, _ -> fail() },
            onPlayerPassedGo = { fail() },
            onPropertyBought = { fail() },
            onGameStateReceived = { fail() },
            onPlayerTurn = { fail() },
            onDiceRolled = { _, _, _, _ -> fail() },
            onCardDrawn = { _, _, _, _ -> fail() },
            onChatMessageReceived = { _, _ -> fail() },
            onCheatMessageReceived = { _, _ -> fail() },
            onClearChat = { },
            onHasWon = { id ->
                invoked = true
                winner = id
            },
            onMessageReceived = { fail("should not hit fallback") },
            onDealProposal = { _: DealProposalMessage -> },
            onGiveUpReceived = { fail("should not hit onGiveUp") },
            onDealResponse = { _: DealResponseMessage -> },
            onReset = {}
        )

        parser.parse(json)

        assertTrue(invoked, "onHasWon must be invoked")
        assertEquals("p2", winner)
    }

    @Test
    fun `parse unrecognized text invokes fallback onMessageReceived`() {
        val raw = "SOME_UNRECOGNIZED_MESSAGE"
        var invoked = false
        var text: String? = null

        val parser = MessageParser(
            gson = gson,
            getPlayers = { dummyPlayers },
            onTaxPayment = { _, _, _ -> fail() },
            onPlayerPassedGo = { fail() },
            onPropertyBought = { fail() },
            onGameStateReceived = { fail() },
            onPlayerTurn = { fail() },
            onDiceRolled = { _, _, _, _ -> fail() },
            onCardDrawn = { _, _, _, _ -> fail() },
            onChatMessageReceived = { _, _ -> fail() },
            onCheatMessageReceived = { _, _ -> fail("should not hit cheat") },
            onClearChat = { },
            onHasWon = { _ -> },
            onMessageReceived = { t ->
                invoked = true
                text = t
            },
            onDealProposal = { _: DealProposalMessage -> },
            onGiveUpReceived = { fail("should not hit onGiveUp") },
            onDealResponse = { _: DealResponseMessage -> },
            onReset = {}
        )

        parser.parse(raw)

        assertTrue(invoked)
        assertEquals(raw, text)
    }

    @Test
    fun `parse DEAL_PROPOSAL JSON invokes onDealProposal with message object`() {
        val json = """{"type":"DEAL_PROPOSAL"}"""
        var invoked = false
        var captured: DealProposalMessage? = null

        val parser = MessageParser(
            gson = gson,
            getPlayers = { dummyPlayers },
            onTaxPayment = { _, _, _ -> fail() },
            onPlayerPassedGo = { fail() },
            onPropertyBought = { fail() },
            onGameStateReceived = { fail() },
            onPlayerTurn = { fail() },
            onDiceRolled = { _, _, _, _ -> fail() },
            onCardDrawn = { _, _, _, _ -> fail() },
            onChatMessageReceived = { _, _ -> fail() },
            onCheatMessageReceived = { _, _ -> fail() },
            onClearChat = { },
            onHasWon = { _ -> },
            onMessageReceived = { fail("should not hit fallback") },
            onDealProposal = { msg ->
                invoked = true
                captured = msg
            },
            onGiveUpReceived = { fail("should not hit onGiveUp") },
            onDealResponse = { _: DealResponseMessage -> },
            onReset = {}
        )

        parser.parse(json)

        assertTrue(invoked, "onDealProposal should be invoked for DEAL_PROPOSAL")
        assertNotNull(captured)
        assertEquals("DEAL_PROPOSAL", captured?.type)
    }

    @Test
    fun `parse GIVE_UP JSON invokes onGiveUpReceived with userId`() {
        val json = """{"type":"GIVE_UP","userId":"p1"}"""
        var invoked = false

        val parser = MessageParser(
            gson = gson,
            getPlayers = { dummyPlayers },
            onTaxPayment = { _, _, _ -> fail("should not hit tax payment") },
            onPlayerPassedGo = { fail("should not hit passed GO") },
            onPropertyBought = { fail("should not hit property bought") },
            onGameStateReceived = { fail("should not hit game state") },
            onPlayerTurn = { fail("should not hit player turn") },
            onDiceRolled = { _, _, _, _ -> fail("should not hit dice roll") },
            onCardDrawn = { _, _, _, _ -> fail("should not hit card drawn") },
            onChatMessageReceived = { _, _ -> fail("should not hit chat") },
            onCheatMessageReceived = { _, _ -> fail("should not hit cheat") },
            onClearChat = { fail("should not hit clear chat") },
            onHasWon = { fail("should not hit has won") },
            onMessageReceived = { fail("should not hit fallback") },
            onDealProposal = { _: DealProposalMessage -> fail("should not hit deal proposal") },
            onGiveUpReceived = { invoked = true },
            onDealResponse = { _: DealResponseMessage -> fail("should not hit deal response") },
            onReset = {}
        )

        parser.parse(json)

        assertTrue(invoked, "onGiveUpReceived should be invoked for GIVE_UP")
    }

    @Test
    fun `parse DEAL_RESPONSE JSON invokes onDealResponse with message object`() {
        val json = """{"type":"DEAL_RESPONSE"}"""
        var invoked = false
        var captured: DealResponseMessage? = null

        val parser = MessageParser(
            gson = gson,
            getPlayers = { dummyPlayers },
            onTaxPayment = { _, _, _ -> fail() },
            onPlayerPassedGo = { fail() },
            onPropertyBought = { fail() },
            onGameStateReceived = { fail() },
            onPlayerTurn = { fail() },
            onDiceRolled = { _, _, _, _ -> fail() },
            onCardDrawn = { _, _, _, _ -> fail() },
            onChatMessageReceived = { _, _ -> fail() },
            onCheatMessageReceived = { _, _ -> fail() },
            onClearChat = { },
            onHasWon = { _ -> },
            onMessageReceived = { fail("should not hit fallback") },
            onDealProposal = { _: DealProposalMessage -> },
            onGiveUpReceived = { fail("should not hit onGiveUp") },
            onDealResponse = { resp ->
                invoked = true
                captured = resp
            },
            onReset = {}
        )

        parser.parse(json)

        assertTrue(invoked, "onDealResponse should be invoked for DEAL_RESPONSE")
        assertNotNull(captured)
        assertEquals("DEAL_RESPONSE", captured?.type)
    }

    @Test
    fun `parse RESET JSON invokes onReset`() {
        val json = """{"type":"RESET"}"""
        var resetInvoked = false

        val parser = MessageParser(
            gson = gson,
            getPlayers = { dummyPlayers },
            onTaxPayment = { _, _, _ -> fail("should not hit tax payment") },
            onPlayerPassedGo = { fail("should not hit passed GO") },
            onPropertyBought = { fail("should not hit property bought") },
            onGameStateReceived = { fail("should not hit game state") },
            onPlayerTurn = { fail("should not hit player turn") },
            onDiceRolled = { _, _, _, _ -> fail("should not hit dice roll") },
            onCardDrawn = { _, _, _, _ -> fail("should not hit card drawn") },
            onChatMessageReceived = { _, _ -> fail("should not hit chat") },
            onCheatMessageReceived = { _, _ -> fail("should not hit cheat") },
            onClearChat = { fail("should not hit clear chat") },
            onHasWon = { fail("should not hit has won") },
            onMessageReceived = { fail("should not hit fallback") },
            onDealProposal = { _: DealProposalMessage -> fail("should not hit deal proposal") },
            onGiveUpReceived = { fail("should not hit onGiveUp") },
            onDealResponse = { _: DealResponseMessage -> fail("should not hit deal response") },
            onReset = { resetInvoked = true }
        )

        parser.parse(json)

        assertTrue(resetInvoked, "onReset should be invoked for RESET")
    }
}
