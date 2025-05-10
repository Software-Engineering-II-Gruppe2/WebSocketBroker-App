package at.aau.serg.websocketbrokerdemo

import android.content.Context
import android.content.res.AssetManager
import okhttp3.WebSocket
import org.junit.jupiter.api.*
import org.mockito.Mockito.*
import java.io.ByteArrayInputStream
import kotlinx.coroutines.Dispatchers

class GameWebSocketClientTest {

    private lateinit var context: Context
    private lateinit var assetManager: AssetManager

    @BeforeEach
    fun setUp() {
        context = mock(Context::class.java)
        assetManager = mock(AssetManager::class.java)
        `when`(context.assets).thenReturn(assetManager)
    }

    private fun createClient(): GameWebSocketClient {
        val props = "server.url=ws://example.com"
        val input = ByteArrayInputStream(props.toByteArray())
        `when`(assetManager.open("config.properties")).thenReturn(input)
        return GameWebSocketClient(
            context,
            onConnected = { /* No-op */ },
            onMessageReceived = { /* No-op */ },
            onDiceRolled = { _, _ -> /* No-op */ },
            onGameStateReceived = { /* No-op */ },
            onPlayerTurn = { /* No-op */ },
            onPlayerPassedGo = { /* No-op */ },
            coroutineDispatcher = Dispatchers.IO,
            onChatMessageReceived = { _, _ -> },
            onCardDrawn = { _, _, _ -> },
            onTaxPayment = { _, _, _ -> }
        )
    }

    @Test
    fun testLoadServerUrl() {
        val client = createClient()
        val field = GameWebSocketClient::class.java.getDeclaredField("serverUrl")
        field.isAccessible = true
        val url = field.get(client) as String
        Assertions.assertEquals("ws://example.com", url)
    }

    @Test
    fun testSendMessageDelegatesToWebSocket() {
        val client = createClient()
        val ws = mock(WebSocket::class.java)
        val wsField = GameWebSocketClient::class.java.getDeclaredField("webSocket")
        wsField.isAccessible = true
        wsField.set(client, ws)

        client.sendMessage("HelloWorld")
        verify(ws, times(1)).send("HelloWorld")
    }

    @Test
    fun testSendChatMessage() {
        val client = createClient()
        val ws = mock(WebSocket::class.java)
        val wsField = GameWebSocketClient::class.java.getDeclaredField("webSocket")
        wsField.isAccessible = true
        wsField.set(client, ws)

        client.sendChatMessage("user123", "Hi there!")
        val expected = "{\"type\":\"CHAT_MESSAGE\",\"playerId\":\"user123\",\"message\":\"Hi there!\"}"
        verify(ws).send(expected)
    }

    @Test
    fun testRollDice() {
        val client = createClient()
        val spyClient = spy(client)
        doNothing().`when`(spyClient).sendMessage(anyString())

        spyClient.rollDice()
        verify(spyClient).sendMessage("Roll")
    }

    @Test
    fun testManualRollDiceValid() {
        val client = createClient()
        val spyClient = spy(client)
        doNothing().`when`(spyClient).sendMessage(anyString())

        spyClient.manualRollDice(10)
        verify(spyClient).sendMessage("MANUAL_ROLL:10")
    }

    @Test
    fun testManualRollDiceInvalid() {
        val client = createClient()
        val spyClient = spy(client)
        doNothing().`when`(spyClient).sendMessage(anyString())

        spyClient.manualRollDice(0)
        spyClient.manualRollDice(40)
        verify(spyClient, never()).sendMessage(startsWith("MANUAL_ROLL:"))
    }

    @Test
    fun testBuyProperty() {
        val client = createClient()
        val spyClient = spy(client)
        doNothing().`when`(spyClient).sendMessage(anyString())

        spyClient.buyProperty(5)
        verify(spyClient).sendMessage("BUY_PROPERTY:5")
    }

    @Test
    fun testSendTaxPayment() {
        val client = createClient()
        val spyClient = spy(client)
        doNothing().`when`(spyClient).sendMessage(anyString())

        spyClient.sendTaxPayment("p1", 100, "INCOME_TAX")
        verify(spyClient).sendMessage(contains("\"type\":\"TAX_PAYMENT\""))
        verify(spyClient).sendMessage(contains("\"playerId\":\"p1\""))
        verify(spyClient).sendMessage(contains("\"amount\":100"))
        verify(spyClient).sendMessage(contains("\"taxType\":\"INCOME_TAX\""))
    }

    @Test
    fun testSendGiveUpMessage() {
        val client = createClient()
        val spyClient = spy(client)
        doNothing().`when`(spyClient).sendMessage(anyString())

        spyClient.sendGiveUpMessage("u42")
        verify(spyClient).sendMessage(contains("\"type\":\"GIVE_UP\""))
        verify(spyClient).sendMessage(contains("\"userId\":\"u42\""))
    }


    @Test
    fun testSendPullCardCommunityChest() {
        val client = createClient()
        val spyClient = spy(client)
        doNothing().`when`(spyClient).sendMessage(anyString())

        spyClient.sendPullCard("p1", 2)
        verify(spyClient).sendMessage(contains("\"cardType\":\"COMMUNITY_CHEST\""))
    }

    @Test
    fun testSendPullCardChance() {
        val client = createClient()
        val spyClient = spy(client)
        doNothing().`when`(spyClient).sendMessage(anyString())

        spyClient.sendPullCard("p2", 7)
        verify(spyClient).sendMessage(contains("\"cardType\":\"CHANCE\""))
    }

    @Test
    fun testSendPullCardInvalid() {
        val client = createClient()
        val spyClient = spy(client)
        doNothing().`when`(spyClient).sendMessage(anyString())

        spyClient.sendPullCard("p3", 1)
        verify(spyClient, never()).sendMessage(anyString())
    }
}
