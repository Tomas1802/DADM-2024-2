package co.edu.unal.tictactoe.models

import android.content.SharedPreferences

class GameState {

    private var gameOver: Boolean = false
    private var value = mutableMapOf(
        "wins" to 0,
        "losses" to 0,
        "ties" to 0,
    )

    fun getValue(): MutableMap<String, Int>{
        return value
    }

    fun getWins(): Int {
        return value["wins"] ?: 0
    }

    fun getLosses(): Int {
        return value["losses"] ?: 0
    }

    fun getTies(): Int {
        return value["ties"] ?: 0
    }

    fun setWins(wins:Int){
        value["wins"] = wins
    }

    fun setLosses(losses:Int){
        value["losses"] = losses
    }

    fun setTies(ties:Int){
        value["ties"] = ties
    }

    fun increaseStat(stat: String) {
        value[stat] = (value[stat] ?: 0) + 1
    }

    fun setGameOver(_gameOver:Boolean){
        gameOver = _gameOver
    }

    fun isGameOver(): Boolean{
        return gameOver
    }
}