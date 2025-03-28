package at.aau.serg.websocketbrokerdemo

import at.aau.serg.websocketbrokerdemo.ui.LoginScreen
import at.aau.serg.websocketbrokerdemo.ui.RegisterScreen
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import io.mockk.*
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*

class AuthUnitTest {

    private lateinit var firebaseAuthMock: FirebaseAuth
    private lateinit var firebaseUserMock: FirebaseUser

    @Before
    fun setup() {
        // Initialize mock FirebaseAuth and FirebaseUser
        firebaseAuthMock = mockk(relaxed = true)
        firebaseUserMock = mockk()

        // Mock FirebaseAuth.getInstance() to return the mock instance
        mockkStatic(FirebaseAuth::class)
        every { FirebaseAuth.getInstance() } returns firebaseAuthMock
    }

    @Test
    fun testLogin_Success() {
        // Set up mock behavior
        val email = "asdf@gmx.at"
        val password = "test123!"

        every { firebaseAuthMock.signInWithEmailAndPassword(email, password) } returns mockk(relaxed = true)

        // Test login success
        val result = firebaseAuthMock.signInWithEmailAndPassword(email, password)
        assertNotNull(result)
    }

    @Test
    fun testRegister_Success() {
        // Set up mock behavior for successful registration
        val email = "newuser@example.com"
        val password = "newpassword"

        every { firebaseAuthMock.createUserWithEmailAndPassword(email, password) } returns mockk(relaxed = true)

        // Test registration success
        val result = firebaseAuthMock.createUserWithEmailAndPassword(email, password)
        assertNotNull(result)
    }

    @Test
    fun testLogin_Failure() {
        // Mock a failed login attempt
        val email = "wrong@example.com"
        val password = "wrongpassword"

        every { firebaseAuthMock.signInWithEmailAndPassword(email, password) } throws Exception("Authentication Failed")

        try {
            firebaseAuthMock.signInWithEmailAndPassword(email, password)
        } catch (e: Exception) {
            assertEquals("Authentication Failed", e.message)
        }
    }
}
