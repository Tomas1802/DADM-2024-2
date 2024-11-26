package co.edu.unal.tictactoe

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity


class MainActivity : ComponentActivity() {
    private lateinit var mGame: TicTacToeGame
    private lateinit var mBoardButtons: Array<Button>
    private lateinit var buttonRestart: Button
    private lateinit var mInfoTextView: TextView
    private var isHumanFirst = true
    private var mGameOver = false
    private var wins = 0
    private var losses = 0
    private var ties = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main)

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
        startNewGame()

        for (i in mBoardButtons.indices) {
            mBoardButtons[i].setOnClickListener { onButtonClick(i) }
        }

        buttonRestart.setOnClickListener {
            startNewGame()
            buttonRestart.visibility = View.INVISIBLE
        }

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
            else -> false
        }
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
}
