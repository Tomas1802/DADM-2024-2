package co.edu.unal.tictactoe

import android.app.AlertDialog
import android.app.Dialog
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import co.edu.unal.tictactoe.TicTacToeGame.DifficultyLevel


class MainActivity : ComponentActivity() {
    private lateinit var prefs: SharedPreferences
    private lateinit var mGame: TicTacToeGame
    private lateinit var mBoardButtons: Array<Button>
    private lateinit var buttonRestart: Button
    private lateinit var mInfoTextView: TextView
    private var gameOver = false
    private var currentTurn = 1
    private val handler = Handler(Looper.getMainLooper())
    private var isHumanFirst = true
    private var mGameOver = false
    private var wins = 0
    private var losses = 0
    private var ties = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main)

        prefs = getSharedPreferences("tic_tac_toe_prefs", MODE_PRIVATE)

        // Restore persistent data
        wins = prefs.getInt("humanWins", 0)
        losses = prefs.getInt("computerWins", 0)
        ties = prefs.getInt("ties", 0)

        // Update the UI with restored data
        updateStats()

        // Restore game state
        if (savedInstanceState != null) {
            gameOver = savedInstanceState.getBoolean("gameOver")
            currentTurn = savedInstanceState.getInt("currentTurn")
            if (!gameOver && currentTurn == 2) {
                handler.postDelayed({ mGame.getComputerMove() }, 1000)
            }
        }

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

        mInfoTextView = findViewById(R.id.statusTextView)
        buttonRestart = findViewById(R.id.button_restart)

        mGame = TicTacToeGame()

        val difficulty = loadDifficulty()
        mGame.setDifficultyLevel(difficulty)

        startNewGame()

        for (i in mBoardButtons.indices) {
            mBoardButtons[i].setOnClickListener { onButtonClick(i) }
        }

        buttonRestart.setOnClickListener {
            startNewGame()
            buttonRestart.visibility = View.INVISIBLE
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        // Save the game board state
        outState.putCharArray("board", mGame.getBoard())

        // Save the current game state
        outState.putBoolean("gameOver", mGameOver)
        outState.putBoolean("isHumanFirst", isHumanFirst)

        // Save player statistics
        outState.putInt("wins", wins)
        outState.putInt("losses", losses)
        outState.putInt("ties", ties)

        // Save the current info text
        outState.putString("infoText", mInfoTextView.text.toString())
        saveDifficulty(mGame.getDifficultyLevel())
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)

        // Restore the game board state
        mGame.setBoard(savedInstanceState.getCharArray("board")!!)

        // Restore the current game state
        mGameOver = savedInstanceState.getBoolean("gameOver")
        isHumanFirst = savedInstanceState.getBoolean("isHumanFirst")

        // Restore player statistics
        wins = savedInstanceState.getInt("wins")
        losses = savedInstanceState.getInt("losses")
        ties = savedInstanceState.getInt("ties")

        // Restore the current info text
        mInfoTextView.text = savedInstanceState.getString("infoText")
        if(mGameOver) buttonRestart.visibility = View.VISIBLE
        // Update the UI elements to reflect restored state
        updateBoard()
        updateStats()
    }

    override fun onStop() {
        super.onStop()

        // Save persistent data
        val editor = prefs.edit()
        editor.putInt("humanWins", wins)
        editor.putInt("computerWins", losses)
        editor.putInt("ties", ties)
        editor.apply() // Save changes asynchronously
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

    private fun resetScores() {
        // Reset score variables
        wins = 0
        losses = 0
        ties = 0

        // Update SharedPreferences to save reset scores
        val editor = prefs.edit()
        editor.putInt("humanWins", wins)
        editor.putInt("computerWins", losses)
        editor.putInt("ties", ties)
        editor.apply()

        // Update the UI
        updateStats()

        // Show a confirmation message
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
                var selected = mGame.getDifficultyLevel().ordinal// Replace with how your game retrieves difficulty
                builder.setSingleChoiceItems(levels, selected) { dialogInterface, item ->
                    dialogInterface.dismiss() // Close dialog

                    // TODO: Set the difficulty level in the game.
                    mGame.setDifficultyLevel(TicTacToeGame.DifficultyLevel.values()[item]) // Replace with how your game sets difficulty level.

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
                    .setPositiveButton(R.string.ok, null) // Add "OK" button to dismiss the dialog
                dialog = builder.create()
            }
        }

        return dialog
    }

    private fun startNewGame() {
        mGame.clearBoard()
        for (i in mBoardButtons.indices) {
            mBoardButtons[i].setBackgroundResource(R.drawable.custom_button) // Reset to default button style
            mBoardButtons[i].isEnabled = true // Re-enable buttons for a new game
        }
        mInfoTextView.text = getString(R.string.first_human)
        mGameOver = false

        if (isHumanFirst) {
            mInfoTextView.text = getString(R.string.first_human)
        } else {
            mInfoTextView.text = getString(R.string.first_computer)
            val move = mGame.getComputerMove()
            mGame.setMove(TicTacToeGame.COMPUTER_PLAYER, move)
            updateBoard() // Show the computer's move
        }

        isHumanFirst = !isHumanFirst
    }

    private fun onButtonClick(location: Int) {
        if (!mGameOver && mGame.setMove(TicTacToeGame.HUMAN_PLAYER, location)) {
            updateBoard()
            val winner = mGame.checkForWinner()
            if (winner == 0) {
                mInfoTextView.text = getString(R.string.turn_computer)
                val move = mGame.getComputerMove()
                mGame.setMove(TicTacToeGame.COMPUTER_PLAYER, move)
                updateBoard()
            }
            checkGameStatus()
        }
    }

    private fun updateBoard() {
        for (i in mBoardButtons.indices) {
            when (mGame.getBoard()[i]) {
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
        val restartButton: Button = findViewById(R.id.button_restart)

        when (mGame.checkForWinner()) {
            1 -> { // Tie
                ties++
                mInfoTextView.text = getString(R.string.result_tie)
                mGameOver = true
                restartButton.visibility = View.VISIBLE
            }
            2 -> { // Human won
                wins++
                mInfoTextView.text = getString(R.string.result_human_wins)
                mGameOver = true
                restartButton.visibility = View.VISIBLE
            }
            3 -> { // Computer won
                losses++
                mInfoTextView.text = getString(R.string.result_computer_wins)
                mGameOver = true
                restartButton.visibility = View.VISIBLE
            }
        }

        updateStats()
    }

    private fun updateStats() {
        findViewById<TextView>(R.id.winsTextView).text = "Wins: $wins"
        findViewById<TextView>(R.id.lossesTextView).text = "Losses: $losses"
        findViewById<TextView>(R.id.tiesTextView).text = "Ties: $ties"
    }

    companion object {
        const val DIALOG_DIFFICULTY_ID = 0
        const val DIALOG_QUIT_ID = 1
        const val DIALOG_ABOUT_ID = 2
    }

    private fun saveDifficulty(difficulty: DifficultyLevel) {
        prefs.edit().putInt("difficulty", difficulty.ordinal).apply()
    }

    private fun loadDifficulty(): TicTacToeGame.DifficultyLevel {
        val ordinal = prefs.getInt("difficulty", TicTacToeGame.DifficultyLevel.Easy.ordinal)
        return TicTacToeGame.DifficultyLevel.values().getOrElse(ordinal) { TicTacToeGame.DifficultyLevel.Easy }
    }

}
