package at.aau.serg.websocketbrokerdemo

import MyStomp
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import com.google.firebase.auth.FirebaseAuth
import com.example.myapplication.R

class MainActivity : ComponentActivity(), Callbacks {
    lateinit var mystomp:MyStomp
    lateinit var  response:TextView
    private val auth = FirebaseAuth.getInstance() // Firebase Auth-Instanz
    override fun onCreate(savedInstanceState: Bundle?) {
        mystomp=MyStomp(this)

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.fragment_fullscreen)

        findViewById<Button>(R.id.connectbtn).setOnClickListener { mystomp.connect() }
        findViewById<Button>(R.id.hellobtn).setOnClickListener{mystomp.sendHello()}
        findViewById<Button>(R.id.jsonbtn).setOnClickListener{mystomp.sendJson()}
        findViewById<Button>(R.id.logoutbtn).setOnClickListener {
            auth.signOut()  // Benutzer ausloggen
            val intent = Intent(this, AuthActivity::class.java)
            startActivity(intent)
            finish() // MainActivity schließen
        }

        response=findViewById(R.id.response_view)

    }

    override fun onResponse(res: String) {
        response.setText(res)
    }


}

