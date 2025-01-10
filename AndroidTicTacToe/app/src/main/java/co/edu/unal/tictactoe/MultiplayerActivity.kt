package co.edu.unal.tictactoe

import GameAdapter
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import co.edu.unal.tictactoe.models.Game
import co.edu.unal.tictactoe.services.PlayerIdManager
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class MultiplayerActivity : ComponentActivity() {
    private lateinit var database: DatabaseReference
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: GameAdapter
    private lateinit var playerIdManager: PlayerIdManager
    private val gamesList = mutableListOf<Game>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_multiplayer)

        playerIdManager= PlayerIdManager(applicationContext)

        database = FirebaseDatabase.getInstance().reference.child("games")
        recyclerView = findViewById(R.id.recyclerViewGames)
        adapter = GameAdapter(gamesList) { gameId -> joinGame(gameId) }
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        findViewById<Button>(R.id.btnCreateGame).setOnClickListener { createNewGame() }

        fetchGamesList()
    }

    private fun fetchGamesList() {
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                gamesList.clear()
                for (gameSnapshot in snapshot.children) {
                    val game = gameSnapshot.getValue(Game::class.java)
                    if (game?.status == "waiting") gamesList.add(game)
                }
                adapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("Firebase", "Error: ${error.message}")
            }
        })
    }

    private fun createNewGame() {
        val gameId = database.push().key ?: return
        val newGame = Game(gameId, player1 = playerIdManager.getOrCreateUserId(), status = "waiting")
        database.child(gameId).setValue(newGame)

        val intent = Intent(this, MainActivity::class.java)
        intent.putExtra("gameId", gameId)
        startActivity(intent)
    }

    private fun joinGame(gameId: String) {
        getGamePlayer(gameId) { player1 ->
            if (player1 != null) {
                database.child(gameId).child("player2").setValue(playerIdManager.getOrCreateUserId())
                database.child(gameId).child("status").setValue("ongoing")
                database.child(gameId).child("currentTurn").setValue(player1)

                val intent = Intent(this, MainActivity::class.java)
                intent.putExtra("gameId", gameId)
                intent.putExtra("guest", "true")
                startActivity(intent)
            }
        }
    }

    private fun getGamePlayer(gameId: String, callback: (String?) -> Unit) {
        database.child(gameId).child("player1").get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val player1 = task.result?.getValue(String::class.java)
                callback(player1)
            } else {
                println("Failed to retrieve Player 1: ${task.exception?.message}")
                callback(null)
            }
        }
    }
}