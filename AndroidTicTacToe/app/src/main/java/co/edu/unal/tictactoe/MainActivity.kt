package co.edu.unal.tictactoe

import android.app.AlertDialog
import android.app.Dialog
import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import co.edu.unal.tictactoe.domain.StateHandler
import co.edu.unal.tictactoe.services.PlayerIdManager
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.GenericTypeIndicator
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine


class MainActivity : ComponentActivity() {
    private lateinit var database: DatabaseReference
    private lateinit var gameSettings: TicTacToeGame
    private lateinit var mBoardButtons: Array<Button>
    private lateinit var winstext: TextView
    private lateinit var lossestext: TextView
    private lateinit var tiestext: TextView
    private lateinit var buttonRestart: Button
    private lateinit var buttonEnd: Button
    private lateinit var mInfoTextView: TextView
    private lateinit var moveMediaPlayer: MediaPlayer
    private lateinit var winMediaPlayer: MediaPlayer
    private lateinit var loseMediaPlayer: MediaPlayer
    private lateinit var computerMediaPlayer: MediaPlayer
    private lateinit var playerIdManager: PlayerIdManager
    private var multiplayerGameId: String? = null
    private var isGuest: Boolean = false
    private var currentTurn = 1
    private val handler = Handler(Looper.getMainLooper())
    private var isHumanFirst = true
    private lateinit var stateHandler: StateHandler

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main)

        playerIdManager= PlayerIdManager(applicationContext)

        val gameId = intent.getStringExtra("gameId")
        val guest = intent.getStringExtra("guest")

        multiplayerGameId = gameId
        isGuest = guest.toBoolean()

        database = FirebaseDatabase.getInstance().reference.child("games")

        gameSettings = TicTacToeGame(multiplayerGameId, guest.toBoolean())
        stateHandler = StateHandler(this)

        stateHandler.restoreState()
        gameSettings.setDifficultyLevel(stateHandler.loadDifficulty())

        initButtons()
        updateUIStats()
        startNewGame()

        if (multiplayerGameId != null) {
            checkButtonsBehavior();
            listenForBoardUpdates(multiplayerGameId!!)
            listenForOpponentJoin()
        }
    }

    private fun checkButtonsBehavior()
    {
        val status = database.child(multiplayerGameId!!).child("status")

        status.get().addOnSuccessListener { snapshot ->
            if (snapshot.exists()) {
                val status = snapshot.getValue(String::class.java)
                if(status == "waiting") disableBoardButtons()
            }
        }

        val currentTurn = database.child(multiplayerGameId!!).child("currentTurn")

        currentTurn.get().addOnSuccessListener { snapshot ->
            if (snapshot.exists()) {
                val turn = snapshot.getValue(String::class.java)
                if(turn != playerIdManager.getOrCreateUserId()) disableBoardButtons()
                else if (turn == playerIdManager.getOrCreateUserId()) enableBoardButtons()
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        stateHandler.saveGameState()

        outState.putCharArray("board", gameSettings.getBoard())
        outState.putBoolean("gameOver", stateHandler.getState().isGameOver())
        outState.putBoolean("isHumanFirst", isHumanFirst)

        for ((key, value) in stateHandler.getState().getValue()) {
            outState.putInt(key, value)
        }

        outState.putString("infoText", mInfoTextView.text.toString())
        stateHandler.saveDifficulty(gameSettings.getDifficultyLevel())
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)

        stateHandler.setGameOver(savedInstanceState.getBoolean("gameOver"))
        gameSettings.setBoard(savedInstanceState.getCharArray("board")!!)
        stateHandler.setGameOver(savedInstanceState.getBoolean("gameOver"))
        isHumanFirst = savedInstanceState.getBoolean("isHumanFirst")
        currentTurn = savedInstanceState.getInt("currentTurn")

        if (multiplayerGameId == null && !stateHandler.getState().isGameOver() && currentTurn == 2) {
            handler.postDelayed({ gameSettings.getComputerMove() }, 1000)
        }
        else if(multiplayerGameId != null)
        {

        }

        stateHandler.restoreState()

        mInfoTextView.text = savedInstanceState.getString("infoText")

        if(stateHandler.getState().isGameOver()){
            if(multiplayerGameId == null)
                buttonRestart.visibility = View.VISIBLE
            else
                buttonEnd.visibility = View.VISIBLE
        }

        updateBoard()
        updateUIStats()
    }

    override fun onStop() {
        super.onStop()
        stateHandler.saveGameState()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        super.onCreateOptionsMenu(menu)
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.new_game -> {
                startNewGame()
                true
            }
            R.id.ai_difficulty -> {
                showDialog(Companion.DIALOG_DIFFICULTY_ID)
                true
            }
            R.id.quit -> {
                showDialog(Companion.DIALOG_QUIT_ID)
                true
            }
            R.id.about -> { // New "About" menu option
                showDialog(DIALOG_ABOUT_ID)
                true
            }
            R.id.reset_scores -> { // Handle reset scores
                resetScores()
                true
            }
            else -> false
        }
    }

    private fun initButtons(){
        winstext = findViewById(R.id.winsTextView)
        lossestext = findViewById(R.id.lossesTextView)
        tiestext = findViewById(R.id.tiesTextView)

        if(multiplayerGameId != null){
            winstext.visibility = View.INVISIBLE
            lossestext.visibility = View.INVISIBLE
            tiestext.visibility = View.INVISIBLE
        }
        else{
            winstext.visibility = View.VISIBLE
            lossestext.visibility = View.VISIBLE
            tiestext.visibility = View.VISIBLE
        }

        mInfoTextView = findViewById(R.id.statusTextView)
        buttonRestart = findViewById(R.id.button_restart)
        buttonEnd = findViewById(R.id.end_restart)
        mBoardButtons = arrayOf(
            findViewById(R.id.button1),
            findViewById(R.id.button2),
            findViewById(R.id.button3),
            findViewById(R.id.button4),
            findViewById(R.id.button5),
            findViewById(R.id.button6),
            findViewById(R.id.button7),
            findViewById(R.id.button8),
            findViewById(R.id.button9)
        )

        for (i in mBoardButtons.indices) {
            mBoardButtons[i].setOnClickListener { onButtonClick(i) }
        }

        buttonRestart.setOnClickListener {
            startNewGame()
            buttonRestart.visibility = View.INVISIBLE
        }

        buttonEnd.setOnClickListener {
            val gameRef = database.child("games").child(multiplayerGameId!!)

            // Delete the game from Firebase
            gameRef.removeValue()
                .addOnSuccessListener {
                    Log.d("Firebase", "Game successfully deleted")

                    // Return to the MultiplayerActivity
                    val intent = Intent(this, MultiplayerActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP // Clear previous activities
                    startActivity(intent)
                    finish() // Close the current activity
                }
                .addOnFailureListener { error ->
                    Log.e("Firebase", "Error deleting game: ${error.message}")
                    Toast.makeText(this, "Failed to delete game. Please try again.", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun resetScores() {
        stateHandler.resetState()
        updateUIStats()
        Toast.makeText(this, "Scores have been reset!", Toast.LENGTH_SHORT).show()
    }

    override fun onCreateDialog(id: Int): Dialog? {
        var dialog: Dialog? = null
        val builder = AlertDialog.Builder(this)

        when (id) {
            DIALOG_DIFFICULTY_ID -> {
                builder.setTitle(R.string.difficulty_choose)
                val levels = arrayOf<CharSequence>(
                    getString(R.string.difficulty_easy),
                    getString(R.string.difficulty_harder),
                    getString(R.string.difficulty_expert)
                )

                // TODO: Set selected, an integer (0 to n-1), for the Difficulty dialog.
                // Assuming `selected` is the currently selected difficulty level.
                var selected = gameSettings.getDifficultyLevel().ordinal// Replace with how your game retrieves difficulty
                builder.setSingleChoiceItems(levels, selected) { dialogInterface, item ->
                    dialogInterface.dismiss() // Close dialog

                    // TODO: Set the difficulty level in the game.
                    gameSettings.setDifficultyLevel(TicTacToeGame.DifficultyLevel.values()[item]) // Replace with how your game sets difficulty level.

                    // Display the selected difficulty level
                    Toast.makeText(applicationContext, levels[item], Toast.LENGTH_SHORT).show()
                    startNewGame()
                }

                dialog = builder.create()
            }
            DIALOG_QUIT_ID -> {
                // Create the quit confirmation dialog
                builder.setMessage(R.string.quit_question)
                    .setCancelable(false)
                    .setPositiveButton(R.string.yes) { _, _ ->
                        this.finish()
                    }
                    .setNegativeButton(R.string.no, null)
                dialog = builder.create()
            }
            DIALOG_ABOUT_ID -> { // New "About" dialog
                val customView = layoutInflater.inflate(R.layout.about_dialog, null) // Inflate custom view
                builder.setView(customView)
                    .setTitle(R.string.about_title)
                    .setPositiveButton(R.string.okay, null) // Add "OK" button to dismiss the dialog
                dialog = builder.create()
            }
        }

        return dialog
    }

    private fun startNewGame() {
        gameSettings.clearBoard()
        for (i in mBoardButtons.indices) {
            mBoardButtons[i].setBackgroundResource(R.drawable.custom_button) // Reset to default button style
            mBoardButtons[i].isEnabled = true // Re-enable buttons for a new game
        }

        if(multiplayerGameId == null){
            mInfoTextView.text = getString(R.string.first_human)

            if (isHumanFirst) {
                mInfoTextView.text = getString(R.string.first_human)
            } else {
                mInfoTextView.text = getString(R.string.first_computer)
                val move = gameSettings.getComputerMove()
                gameSettings.setMove(TicTacToeGame.COMPUTER_PLAYER, move)
                updateBoard() // Show the computer's move
            }

            isHumanFirst = !isHumanFirst
        }
        else{
            mInfoTextView.text = getString(R.string.waiting_player)
        }

        stateHandler.setGameOver(false)
    }

    private fun onButtonClick(location: Int) {

        var movement = TicTacToeGame.HUMAN_PLAYER

        if(isGuest) movement = TicTacToeGame.COMPUTER_PLAYER

        moveMediaPlayer.start()

        if (!stateHandler.getState().isGameOver() && gameSettings.setMove(movement, location)) {
            updateBoard()
            val winner = gameSettings.checkForWinner()

            if (winner == 0 && multiplayerGameId == null) {
                mInfoTextView.text = getString(R.string.turn_computer)
                val move = gameSettings.getComputerMove()

                val handler = Handler(Looper.getMainLooper())
                handler.postDelayed({
                    computerMediaPlayer.start()
                    gameSettings.setMove(TicTacToeGame.COMPUTER_PLAYER, move)
                    checkGameStatus()
                    updateBoard()
                }, 500)
            }
            else if (multiplayerGameId != null){
                getGamePlayers(multiplayerGameId!!) { player1, player2 ->
                    if (player1 != null && player2 != null) {
                        if(player1 == playerIdManager.getOrCreateUserId())
                        {
                            database.child(multiplayerGameId!!).child("currentTurn").setValue(player2)
                        }
                        else
                        {
                            database.child(multiplayerGameId!!).child("currentTurn").setValue(player1)
                        }

                        database.child(multiplayerGameId!!).child("board").setValue(gameSettings.getBoard().map { it.toString() })

                        mInfoTextView.text = getString(R.string.turn_opponent)

                        updateBoard()
                        checkGameStatus()
                    }
                }
            }
            checkGameStatus()
        }
    }

    private fun getGamePlayers(gameId: String, callback: (String?, String?) -> Unit) {
        database.child(gameId).get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val snapshot = task.result
                val player1 = snapshot?.child("player1")?.getValue(String::class.java)
                val player2 = snapshot?.child("player2")?.getValue(String::class.java)
                callback(player1, player2)
            } else {
                println("Failed to retrieve players: ${task.exception?.message}")
                callback(null, null)
            }
        }
    }

    private fun disableBoardButtons() {
        for (button in mBoardButtons) {
            button.isEnabled = false
        }
    }

    private fun enableBoardButtons() {
        for (i in mBoardButtons.indices) {
            if (gameSettings.getBoard()[i] == ' ') { // Check if the spot is open
                mBoardButtons[i].isEnabled = true
            }
        }
    }

    private fun updateBoard() {
        for (i in mBoardButtons.indices) {
            when (gameSettings.getBoard()[i]) {
                TicTacToeGame.HUMAN_PLAYER -> {
                    mBoardButtons[i].setBackgroundResource(R.drawable.o_image)
                }
                TicTacToeGame.COMPUTER_PLAYER -> {
                    mBoardButtons[i].setBackgroundResource(R.drawable.x_image)
                }
                else -> mBoardButtons[i].text = ""
            }
        }
    }

    private fun checkGameStatus() {
        when (gameSettings.checkForWinner()) {
            1 -> { // Tie
                stateHandler.increaseTies()
                mInfoTextView.text = getString(R.string.result_tie)
                stateHandler.setGameOver(true)
                updateUIStats()
            }
            2 -> { // Human won
                stateHandler.increaseWins()
                mInfoTextView.text = getString(R.string.result_human_wins)

                if(multiplayerGameId != null) {
                    checkMultiplayerResult();
                }
                else{
                    stateHandler.setGameOver(true)
                    winMediaPlayer.start()
                    updateUIStats()
                }

            }
            3 -> { // Computer won
                if(multiplayerGameId != null) {
                    checkMultiplayerResult();
                }
                else{
                    stateHandler.increaseLosses()
                    stateHandler.setGameOver(true)
                    loseMediaPlayer.start()
                    updateUIStats()
                }
            }
        }
    }

    private fun checkMultiplayerResult()
    {
        val currentTurn = database.child(multiplayerGameId!!).child("currentTurn")

        currentTurn.get().addOnSuccessListener { snapshot ->
            if (snapshot.exists()) {
                val turn = snapshot.getValue(String::class.java)
                if(turn != playerIdManager.getOrCreateUserId()){
                    mInfoTextView.text = getString(R.string.result_human_wins)
                    winMediaPlayer.start()
                }
                else if(turn == playerIdManager.getOrCreateUserId()){
                    mInfoTextView.text = getString(R.string.result_opponent_wins)
                    loseMediaPlayer.start()
                }
                stateHandler.setGameOver(true)
                updateUIStats()
            }
        }
    }

    private fun updateUIStats() {
        val restartButton: Button = findViewById(R.id.button_restart)

        if(multiplayerGameId == null)
            restartButton.visibility = if (stateHandler.getState().isGameOver()) View.VISIBLE else View.INVISIBLE
        else
            buttonEnd.visibility = if (stateHandler.getState().isGameOver()) View.VISIBLE else View.INVISIBLE

        findViewById<TextView>(R.id.winsTextView).text = "Wins: ${stateHandler.getState().getWins()}"
        findViewById<TextView>(R.id.lossesTextView).text = "Losses: ${stateHandler.getState().getLosses()}"
        findViewById<TextView>(R.id.tiesTextView).text = "Ties: ${stateHandler.getState().getTies()}"
    }

    companion object {
        const val DIALOG_DIFFICULTY_ID = 0
        const val DIALOG_QUIT_ID = 1
        const val DIALOG_ABOUT_ID = 2
    }

    override fun onResume() {
        super.onResume()
        moveMediaPlayer = MediaPlayer.create(applicationContext, R.raw.move)
        winMediaPlayer = MediaPlayer.create(applicationContext, R.raw.win)
        loseMediaPlayer = MediaPlayer.create(applicationContext, R.raw.lose)
        computerMediaPlayer = MediaPlayer.create(applicationContext, R.raw.computer)
    }

    override fun onPause() {
        super.onPause()
        moveMediaPlayer.release()
        winMediaPlayer.release()
        loseMediaPlayer.release()
        computerMediaPlayer.release()
    }

    private fun listenForOpponentJoin() {
        database.child(multiplayerGameId!!).child("status").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                // Retrieve the updated board from Firebase
                val status = snapshot.getValue(String::class.java)

                if (status == "ongoing" && !isGuest) {
                    mInfoTextView.text = getString(R.string.first_human)
                    enableBoardButtons()
                }
                else if (status == "ongoing" && isGuest) {
                    mInfoTextView.text = getString(R.string.first_opponent)
                    //disableBoardButtons()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("Firebase", "Error listening to board updates: ${error.message}")
            }
        })
    }

    private fun listenForBoardUpdates(gameId: String) {
        database.child(gameId).child("board").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                checkButtonsBehavior()
                // Retrieve the updated board from Firebase
                val boardList = snapshot.getValue(object : GenericTypeIndicator<List<String>>() {})

                if (boardList != null) {
                    // Convert the List<String> back to a CharArray
                    val updatedBoard = boardList.map { it[0] }.toCharArray()

                    // Update the local game board
                    gameSettings.setBoard(updatedBoard)

                    // Refresh the board UI
                    updateBoard()

                    val currentTurn = database.child(gameId).child("currentTurn")

                    currentTurn.get().addOnSuccessListener { snapshot ->
                        if (snapshot.exists()) {
                            val turn = snapshot.getValue(String::class.java)

                            var turnName = "contestant"

                            if(isGuest) turnName = "opponent"

                            if(turn == turnName){
                                //enableBoardButtons()
                                mInfoTextView.text = getString(R.string.turn_computer)
                            }

                            checkGameStatus()
                        }
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("Firebase", "Error listening to board updates: ${error.message}")
            }
        })
    }

}
