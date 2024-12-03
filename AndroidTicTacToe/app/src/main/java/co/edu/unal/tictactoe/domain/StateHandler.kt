package co.edu.unal.tictactoe.domain
import android.content.SharedPreferences
import android.app.Activity.MODE_PRIVATE
import android.content.Context
import co.edu.unal.tictactoe.TicTacToeGame
import co.edu.unal.tictactoe.TicTacToeGame.DifficultyLevel
import co.edu.unal.tictactoe.models.GameState

class StateHandler(private val context: Context) {
    private lateinit var prefs: SharedPreferences
    private val gameState:GameState = GameState()

    fun saveGameState(){
        val editor = prefs.edit()

        for ((key, value) in gameState.getValue()) {
            editor.putInt(key, value)
        }

        editor.apply()
    }

    fun getState():GameState{
        return gameState
    }

    fun restoreState(){
        prefs = context.getSharedPreferences("tic_tac_toe_prefs", MODE_PRIVATE)

        gameState.setWins(prefs.getInt("wins", 0))
        gameState.setLosses(prefs.getInt("losses", 0))
        gameState.setTies(prefs.getInt("ties", 0))
    }

    fun resetState(){
        gameState.setWins(0)
        gameState.setLosses(0)
        gameState.setTies(0)
        saveGameState()
    }

    fun increaseWins(){
        gameState.increaseStat("wins")
    }

    fun increaseLosses(){
        gameState.increaseStat("losses")
    }

    fun increaseTies(){
        gameState.increaseStat("ties")
    }

    fun setGameOver(gameOver:Boolean){
        gameState.setGameOver(gameOver)
    }

    fun saveDifficulty(difficulty: DifficultyLevel) {
        prefs.edit().putInt("difficulty", difficulty.ordinal).apply()
    }

    fun loadDifficulty(): TicTacToeGame.DifficultyLevel {
        val ordinal = prefs.getInt("difficulty", TicTacToeGame.DifficultyLevel.Easy.ordinal)
        return TicTacToeGame.DifficultyLevel.values().getOrElse(ordinal) { TicTacToeGame.DifficultyLevel.Easy }
    }
}