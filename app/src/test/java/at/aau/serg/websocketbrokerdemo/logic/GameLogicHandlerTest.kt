package at.aau.serg.websocketbrokerdemo.logic

import android.content.Context
import at.aau.serg.websocketbrokerdemo.data.PlayerProfile
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.gson.Gson
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.junit.jupiter.MockitoExtension
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
@ExtendWith(MockitoExtension::class)
class GameLogicHandlerTest {

    @Mock
    private lateinit var context: Context

    private lateinit var sendLog: MutableList<String>
    private lateinit var handler: GameLogicHandler
    private lateinit var mockProfileProvider: UserProfileProvider
    private val dispatcher = StandardTestDispatcher()

    @BeforeEach
    fun setup() {
        sendLog = mutableListOf()
        mockProfileProvider = mock(UserProfileProvider::class.java)

        handler = GameLogicHandler(
            context = context,
            sendMessage = { sendLog.add(it) },
            gson = Gson(),
            coroutineDispatcher = dispatcher,
            userProfileProvider = mockProfileProvider
        )
    }

    @Test
    fun testSendChatMessage() {
        handler.sendChatMessage("player1", "Hello!")
        assertTrue(sendLog[0].contains("CHAT_MESSAGE"))
    }

    @Test
    fun testSendCheatMessage(){
        handler.sendCheatMessage("player1", "Cheat1")
        assertTrue(sendLog[0].contains("CHEAT_MESSAGE"))
    }

    @Test
    fun testRollDice() {
        handler.rollDice()
        assertTrue(sendLog.contains("Roll"))
    }

    @Test
    fun testManualRollDice_validValue() {
        handler.manualRollDice(5)
        assertTrue(sendLog.contains("MANUAL_ROLL:5"))
    }

    @Test
    fun testManualRollDice_invalidValue() {
        handler.manualRollDice(40)
        assertTrue(sendLog.isEmpty())
    }

    @Test
    fun testSendTaxPayment() {
        handler.sendTaxPayment("player2", 200, "INCOME")
        assertTrue(sendLog[0].contains("TAX_PAYMENT"))
    }

    @Test
    fun testSendGiveUpMessage() {
        handler.sendGiveUpMessage("player3")
        assertTrue(sendLog[0].contains("GIVE_UP"))
    }

    @Test
    fun testSendPullCard_validChanceField() {
        handler.sendPullCard("player4", 7)
        assertTrue(sendLog[0].contains("CHANCE"))
    }

    @Test
    fun testSendPullCard_validCommunityChestField() {
        handler.sendPullCard("player4", 2)
        assertTrue(sendLog[0].contains("COMMUNITY_CHEST"))
    }

    @Test
    fun testSendPullCard_invalidField() {
        handler.sendPullCard("player4", 1)
        assertTrue(sendLog.isEmpty())
    }

    @Test
    fun testBuyProperty() {
        handler.buyProperty(10)
        assertTrue(sendLog.contains("BUY_PROPERTY:10"))
    }

    @Test
    fun testPayRent_withValidUser() {
        // Mock FirebaseAuth and current user
        val mockUser = mock(FirebaseUser::class.java)
        `when`(mockUser.uid).thenReturn("mockUid")

        mockStatic(FirebaseAuth::class.java).use { firebaseAuth ->
            val mockAuth = mock(FirebaseAuth::class.java)
            `when`(mockAuth.currentUser).thenReturn(mockUser)
            `when`(FirebaseAuth.getInstance()).thenReturn(mockAuth)

            handler.payRent(15)
            
            // Verify the message was sent
            assertTrue(sendLog.isNotEmpty())
            val sentMessage = sendLog[0]
            assertTrue(sentMessage.contains("RENT_PAYMENT"))
            assertTrue(sentMessage.contains("mockUid"))
            assertTrue(sentMessage.contains("15"))
        }
    }

    @Test
    fun testPayRent_withNoUser() {
        // Mock FirebaseAuth with no current user
        mockStatic(FirebaseAuth::class.java).use { firebaseAuth ->
            val mockAuth = mock(FirebaseAuth::class.java)
            `when`(mockAuth.currentUser).thenReturn(null)
            `when`(FirebaseAuth.getInstance()).thenReturn(mockAuth)

            handler.payRent(15)
            
            // Verify no message was sent
            assertTrue(sendLog.isEmpty())
        }
    }

    @Test
    fun testGetInitPayload() = runTest {
        // Mock FirebaseAuth and current user
        val mockUser = mock(FirebaseUser::class.java)
        `when`(mockUser.uid).thenReturn("mockUid")

        mockStatic(FirebaseAuth::class.java).use { firebaseAuth ->
            val mockAuth = mock(FirebaseAuth::class.java)
            `when`(mockAuth.currentUser).thenReturn(mockUser)
            `when`(FirebaseAuth.getInstance()).thenReturn(mockAuth)

            `when`(mockProfileProvider.getUserProfile("mockUid"))
                .thenReturn(PlayerProfile(name = "TestUser"))

            val json = handler.getInitPayload()
            assertNotNull(json)
            assertTrue(json.contains("INIT"))
            assertTrue(json.contains("TestUser"))
        }
    }


    @Test
    fun testSellProperty_withValidUser() {
        // Mock FirebaseAuth and current user
        val mockUser = mock(FirebaseUser::class.java)
        `when`(mockUser.uid).thenReturn("mockUid")

        mockStatic(FirebaseAuth::class.java).use { firebaseAuth ->
            val mockAuth = mock(FirebaseAuth::class.java)
            `when`(mockAuth.currentUser).thenReturn(mockUser)
            `when`(FirebaseAuth.getInstance()).thenReturn(mockAuth)

            handler.sellProperty(20)

            assertTrue(sendLog.isNotEmpty())
            val sentMessage = sendLog[0]
            assertTrue(sentMessage.contains("SELL_PROPERTY"))
            assertTrue(sentMessage.contains("20"))
        }
    }

    @Test
    fun testSellProperty_withNoUser() {
        // Mock FirebaseAuth with no current user
        mockStatic(FirebaseAuth::class.java).use { firebaseAuth ->
            val mockAuth = mock(FirebaseAuth::class.java)
            `when`(mockAuth.currentUser).thenReturn(null)
            `when`(FirebaseAuth.getInstance()).thenReturn(mockAuth)

            handler.sellProperty(20)

            assertTrue(sendLog.isEmpty())
        }
    }
}
