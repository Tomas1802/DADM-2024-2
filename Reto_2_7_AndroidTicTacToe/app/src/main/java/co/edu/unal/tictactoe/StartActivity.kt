package co.edu.unal.tictactoe

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class StartActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.start_activity)

        val singlePlayerButton: Button = findViewById(R.id.button_single_player)
        val multiPlayerButton: Button = findViewById(R.id.button_multiplayer_player)

        singlePlayerButton.setOnClickListener {
            // Navigate to MainActivity
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

        multiPlayerButton.setOnClickListener {
            // Navigate to MainActivity
            val intent = Intent(this, MultiplayerActivity::class.java)
            startActivity(intent)
        }
    }
}