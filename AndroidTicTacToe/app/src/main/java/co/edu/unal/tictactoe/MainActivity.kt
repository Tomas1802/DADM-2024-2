package co.edu.unal.tictactoe

import android.app.AlertDialog
import android.app.Dialog
import android.media.MediaPlayer
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
import co.edu.unal.tictactoe.domain.StateHandler


class MainActivity : ComponentActivity() {

    private lateinit var gameSettings: TicTacToeGame
    private lateinit var mBoardButtons: Array<Button>
    private lateinit var buttonRestart: Button
    private lateinit var mInfoTextView: TextView
    private lateinit var moveMediaPlayer: MediaPlayer
    private lateinit var winMediaPlayer: MediaPlayer
    private lateinit var loseMediaPlayer: MediaPlayer
    private lateinit var computerMediaPlayer: MediaPlayer
    private var currentTurn = 1
    private val handler = Handler(Looper.getMainLooper())
    private var isHumanFirst = true
    private lateinit var stateHandler: StateHandler

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main)

        gameSettings = TicTacToeGame()
        stateHandler = StateHandler(this)

        stateHandler.restoreState()
        gameSettings.setDifficultyLevel(stateHandler.loadDifficulty())

        initButtons()
        updateUIStats()
        startNewGame()
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

        if (!stateHandler.getState().isGameOver() && currentTurn == 2) {
            handler.postDelayed({ gameSettings.getComputerMove() }, 1000)
        }

        stateHandler.restoreState()

        mInfoTextView.text = savedInstanceState.getString("infoText")
        if(stateHandler.getState().isGameOver()) buttonRestart.visibility = View.VISIBLE

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
        mInfoTextView = findViewById(R.id.statusTextView)
        buttonRestart = findViewById(R.id.button_restart)
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
                    .setPositiveButton(R.string.ok, null) // Add "OK" button to dismiss the dialog
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
        mInfoTextView.text = getString(R.string.first_human)
        stateHandler.setGameOver(false)

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

    private fun onButtonClick(location: Int) {
        moveMediaPlayer.start()
        if (!stateHandler.getState().isGameOver() && gameSettings.setMove(TicTacToeGame.HUMAN_PLAYER, location)) {
            updateBoard()
            val winner = gameSettings.checkForWinner()
            if (winner == 0) {
                mInfoTextView.text = getString(R.string.turn_computer)
                val move = gameSettings.getComputerMove()

                val handler = Handler(Looper.getMainLooper())
                handler.postDelayed({
                    computerMediaPlayer.start()
                    gameSettings.setMove(TicTacToeGame.COMPUTER_PLAYER, move)
                    updateBoard()
                }, 500)
            }
            checkGameStatus()
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
                stateHandler.setGameOver(true)
                winMediaPlayer.start()
                updateUIStats()
            }
            3 -> { // Computer won
                stateHandler.increaseLosses()
                mInfoTextView.text = getString(R.string.result_computer_wins)
                stateHandler.setGameOver(true)
                loseMediaPlayer.start()
                updateUIStats()
            }
        }
    }

    private fun updateUIStats() {
        val restartButton: Button = findViewById(R.id.button_restart)
        restartButton.visibility = if (stateHandler.getState().isGameOver()) View.VISIBLE else View.INVISIBLE

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

}
