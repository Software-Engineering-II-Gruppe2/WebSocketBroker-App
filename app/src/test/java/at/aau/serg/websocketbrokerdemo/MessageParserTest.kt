package at.aau.serg.websocketbrokerdemo

import com.google.gson.Gson
import at.aau.serg.websocketbrokerdemo.data.PlayerMoney
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class MessageParserTest {

    private lateinit var gson: Gson
    // Note: PlayerMoney now requires a 'position' field
    private val dummyPlayers = listOf(
        PlayerMoney(id = "p1", name = "Alice", money = 0, position = 0),
        PlayerMoney(id = "p2", name = "Bob",   money = 0, position = 1)
    )

    @BeforeEach
    fun setUp() {
        gson = Gson()
    }

    @Test
    fun testTaxPayment() {
        val json = """{"type":"TAX_PAYMENT","playerId":"p1","amount":250,"taxType":"INCOME"}"""
        var invoked = false
        var capName: String? = null
        var capAmt: Int? = null
        var capType: String? = null

        val parser = MessageParser(
            gson = gson,
            getPlayers = { dummyPlayers },
            onTaxPayment = { name, amt, type ->
                invoked = true; capName = name; capAmt = amt; capType = type
            },
            onPlayerPassedGo = { fail("should not hit passed GO") },
            onPropertyBought = { fail("should not hit property bought") },
            onGameStateReceived = { fail("should not hit game state") },
            onPlayerTurn = { fail("should not hit player turn") },
            onDiceRolled = { _, _ -> fail("should not hit dice roll") },
            onCardDrawn = { _, _, _ -> fail("should not hit card drawn") },
            onChatMessageReceived = { _, _ -> fail("should not hit chat") },
            onMessageReceived = { fail("should not hit fallback") }
        )

        parser.parse(json)

        assertTrue(invoked, "onTaxPayment must be invoked")
        assertEquals("Alice", capName)
        assertEquals(250, capAmt)
        assertEquals("INCOME", capType)
    }

    @Test
    fun testPassedGo() {
        val raw = "Player p2 passed GO and collected 200"
        var invoked = false
        var capName: String? = null

        val parser = MessageParser(
            gson = gson,
            getPlayers = { dummyPlayers },
            onTaxPayment = { _, _, _ -> fail() },
            onPlayerPassedGo = { name -> invoked = true; capName = name },
            onPropertyBought = { fail() },
            onGameStateReceived = { fail() },
            onPlayerTurn = { fail() },
            onDiceRolled = { _, _ -> fail() },
            onCardDrawn = { _, _, _ -> fail() },
            onChatMessageReceived = { _, _ -> fail() },
            onMessageReceived = { fail() }
        )

        parser.parse(raw)

        assertTrue(invoked, "onPlayerPassedGo must be invoked")
        assertEquals("Bob", capName)
    }

    @Test
    fun testPropertyBought() {
        val raw = "XXX PROPERTY_BOUGHT YYY"
        var invoked = false
        var capRaw: String? = null

        val parser = MessageParser(
            gson = gson,
            getPlayers = { dummyPlayers },
            onTaxPayment = { _, _, _ -> fail() },
            onPlayerPassedGo = { fail() },
            onPropertyBought = { rawText -> invoked = true; capRaw = rawText },
            onGameStateReceived = { fail() },
            onPlayerTurn = { fail() },
            onDiceRolled = { _, _ -> fail() },
            onCardDrawn = { _, _, _ -> fail() },
            onChatMessageReceived = { _, _ -> fail() },
            onMessageReceived = { fail() }
        )

        parser.parse(raw)

        assertTrue(invoked)
        assertEquals(raw, capRaw)
    }

    @Test
    fun testGameState() {
        val jsonList = "[]"
        val raw = "GAME_STATE:$jsonList"
        var invoked = false
        var capList: List<PlayerMoney>? = null

        val parser = MessageParser(
            gson = gson,
            getPlayers = { dummyPlayers },
            onTaxPayment = { _, _, _ -> fail() },
            onPlayerPassedGo = { fail() },
            onPropertyBought = { fail() },
            onGameStateReceived = { list -> invoked = true; capList = list },
            onPlayerTurn = { fail() },
            onDiceRolled = { _, _ -> fail() },
            onCardDrawn = { _, _, _ -> fail() },
            onChatMessageReceived = { _, _ -> fail() },
            onMessageReceived = { fail() }
        )

        parser.parse(raw)

        assertTrue(invoked)
        assertNotNull(capList)
        assertEquals(0, capList!!.size)
    }

    @Test
    fun testPlayerTurn() {
        val raw = "PLAYER_TURN:session42"
        var invoked = false
        var capId: String? = null

        val parser = MessageParser(
            gson = gson,
            getPlayers = { dummyPlayers },
            onTaxPayment = { _, _, _ -> fail() },
            onPlayerPassedGo = { fail() },
            onPropertyBought = { fail() },
            onGameStateReceived = { fail() },
            onPlayerTurn = { id -> invoked = true; capId = id },
            onDiceRolled = { _, _ -> fail() },
            onCardDrawn = { _, _, _ -> fail() },
            onChatMessageReceived = { _, _ -> fail() },
            onMessageReceived = { fail() }
        )

        parser.parse(raw)

        assertTrue(invoked)
        assertEquals("session42", capId)
    }

    @Test
    fun testDiceRoll() {
        val json = """{"type":"DICE_ROLL","playerId":"p1","value":6}"""
        var invoked = false
        var capPid: String? = null
        var capVal: Int? = null

        val parser = MessageParser(
            gson = gson,
            getPlayers = { dummyPlayers },
            onTaxPayment = { _, _, _ -> fail() },
            onPlayerPassedGo = { fail() },
            onPropertyBought = { fail() },
            onGameStateReceived = { fail() },
            onPlayerTurn = { fail() },
            onDiceRolled = { pid, v -> invoked = true; capPid = pid; capVal = v },
            onCardDrawn = { _, _, _ -> fail() },
            onChatMessageReceived = { _, _ -> fail() },
            onMessageReceived = { fail() }
        )

        parser.parse(json)

        assertTrue(invoked)
        assertEquals("p1", capPid)
        assertEquals(6, capVal)
    }

    @Test
    fun testCardDrawn() {
        val json = """
        {
          "type":"CARD_DRAWN",
          "playerId":"p2",
          "cardType":"CHANCE",
          "card":{"description":"You win!"}
        }
        """.trimIndent()
        var invoked = false
        var capPid: String? = null
        var capType: String? = null
        var capDesc: String? = null

        val parser = MessageParser(
            gson = gson,
            getPlayers = { dummyPlayers },
            onTaxPayment = { _, _, _ -> fail() },
            onPlayerPassedGo = { fail() },
            onPropertyBought = { fail() },
            onGameStateReceived = { fail() },
            onPlayerTurn = { fail() },
            onDiceRolled = { _, _ -> fail() },
            onCardDrawn = { pid, type, desc ->
                invoked = true; capPid = pid; capType = type; capDesc = desc
            },
            onChatMessageReceived = { _, _ -> fail() },
            onMessageReceived = { fail()
            }
        )

        parser.parse(json)

        assertTrue(invoked)
        assertEquals("p2", capPid)
        assertEquals("CHANCE", capType)
        assertEquals("You win!", capDesc)
    }

    @Test
    fun testChatMessage() {
        val json = """{"type":"CHAT_MESSAGE","playerId":"p1","message":"Hey!"}"""
        var invoked = false
        var capPid: String? = null
        var capMsg: String? = null

        val parser = MessageParser(
            gson = gson,
            getPlayers = { dummyPlayers },
            onTaxPayment = { _, _, _ -> fail() },
            onPlayerPassedGo = { fail() },
            onPropertyBought = { fail() },
            onGameStateReceived = { fail() },
            onPlayerTurn = { fail() },
            onDiceRolled = { _, _ -> fail() },
            onCardDrawn = { _, _, _ -> fail() },
            onChatMessageReceived = { pid, msg ->
                invoked = true; capPid = pid; capMsg = msg
            },
            onMessageReceived = { fail() }
        )

        parser.parse(json)

        assertTrue(invoked)
        assertEquals("p1", capPid)
        assertEquals("Hey!", capMsg)
    }

    @Test
    fun testFallback() {
        val raw = "SOME_UNRECOGNIZED_MESSAGE"
        var invoked = false
        var capText: String? = null

        val parser = MessageParser(
            gson = gson,
            getPlayers = { dummyPlayers },
            onTaxPayment = { _, _, _ -> fail() },
            onPlayerPassedGo = { fail() },
            onPropertyBought = { fail() },
            onGameStateReceived = { fail() },
            onPlayerTurn = { fail() },
            onDiceRolled = { _, _ -> fail() },
            onCardDrawn = { _, _, _ -> fail() },
            onChatMessageReceived = { _, _ -> fail() },
            onMessageReceived = { text -> invoked = true; capText = text }
        )

        parser.parse(raw)

        assertTrue(invoked)
        assertEquals(raw, capText)
    }
}
