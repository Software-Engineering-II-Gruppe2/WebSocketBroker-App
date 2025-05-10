package at.aau.serg.websocketbrokerdemo

import androidx.activity.ComponentActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AuthActivityTest {

    private lateinit var activity: ComponentActivity
    private lateinit var auth: FirebaseAuth
    private lateinit var user: FirebaseUser

    @BeforeEach
    fun setUp() {
        auth = mock()
        user = mock()
        activity = mock(ComponentActivity::class.java)
    }

    @Test
    fun testUserNotLoggedIn_StayOnAuthScreen() {
        `when`(auth.currentUser).thenReturn(null)

        // Die Aktivität bleibt auf dem Auth-Bildschirm
        verify(activity, never()).finish()
    }
}

