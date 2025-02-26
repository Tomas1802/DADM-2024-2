package co.edu.unal.tictactoe

import android.util.Log
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.GenericTypeIndicator
import com.google.firebase.database.ValueEventListener


class TicTacToeGame(private val gameId: String?, private val guest: Boolean = false) {
    companion object {
        var HUMAN_PLAYER = 'X'
        var COMPUTER_PLAYER = 'O'
        const val OPEN_SPOT = ' '
        const val BOARD_SIZE = 9
    }

    private lateinit var database: DatabaseReference
    private var board = CharArray(BOARD_SIZE) { OPEN_SPOT }
    enum class DifficultyLevel {
        Easy, Harder, Expert
    };
    private var mDifficultyLevel = DifficultyLevel.Expert

    fun getBoard(): CharArray {
        return board
    }

    fun clearBoard() {
        for (i in board.indices) {
            board[i] = OPEN_SPOT
        }
    }

    fun setBoard(newBoard: CharArray) {
        board = newBoard.clone()
    }


    fun setMove(player: Char, location: Int): Boolean {
        if (board[location] == OPEN_SPOT) {
            board[location] = player
            return true
        }
        return false
    }

    fun getComputerMove(): Int {
        var move = -1
        if (mDifficultyLevel == DifficultyLevel.Easy) move = getRandomMove()
        else if (mDifficultyLevel == DifficultyLevel.Harder) {
            move = getBlockingMove()
            if (move == -1) move = getRandomMove()
        } else if (mDifficultyLevel == DifficultyLevel.Expert) {
            move = getWinningMove()
            if (move == -1) move = getBlockingMove()
            if (move == -1) move = getRandomMove()
        }
        return move
    }

    fun getRandomMove(): Int {
        val openSpots = board.indices.filter { board[it] == OPEN_SPOT }
        return openSpots.random()
    }

    fun getBlockingMove(): Int {
        // Check if the AI can win or block the human
        for (i in board.indices) {
            if (board[i] == OPEN_SPOT) {
                // Try to win
                board[i] = COMPUTER_PLAYER
                if (checkForWinner() == 3) {
                    board[i] = OPEN_SPOT
                    return i
                }
                board[i] = OPEN_SPOT

                // Try to block the human
                board[i] = HUMAN_PLAYER
                if (checkForWinner() == 2) {
                    board[i] = OPEN_SPOT
                    return i
                }
                board[i] = OPEN_SPOT
            }
        }
        return -1 // No winning or blocking move found
    }

    private fun getWinningMove(): Int {
        var bestMove = -1
        var bestScore = Int.MIN_VALUE
        for (i in board.indices) {
            if (board[i] == OPEN_SPOT) {
                board[i] = COMPUTER_PLAYER
                val score = minimax(0, false)
                board[i] = OPEN_SPOT
                if (score > bestScore) {
                    bestScore = score
                    bestMove = i
                }
            }
        }
        return bestMove
    }

    // Algoritmo Minimax
    private fun minimax(depth: Int, isMaximizing: Boolean): Int {
        val winner = checkForWinner()
        if (winner == 2) return -10 + depth
        if (winner == 3) return 10 - depth
        if (board.none { it == OPEN_SPOT }) return 0

        if (isMaximizing) {
            var bestScore = Int.MIN_VALUE
            for (i in board.indices) {
                if (board[i] == OPEN_SPOT) {
                    board[i] = COMPUTER_PLAYER
                    val score = minimax(depth + 1, false)
                    board[i] = OPEN_SPOT
                    bestScore = maxOf(score, bestScore)
                }
            }
            return bestScore
        }
        else
        {
            var bestScore = Int.MAX_VALUE
            for (i in board.indices) {
                if (board[i] == OPEN_SPOT) {
                    board[i] = HUMAN_PLAYER
                    val score = minimax(depth + 1, true)
                    board[i] = OPEN_SPOT
                    bestScore = minOf(score, bestScore)
                }
            }
            return bestScore
        }
    }

    fun checkForWinner(): Int {
        // Check rows, columns, and diagonals
        for (i in 0..2) {
            if (board[i] != OPEN_SPOT && board[i] == board[i + 3] && board[i] == board[i + 6]) return if (board[i] == HUMAN_PLAYER) 2 else 3
            val rowStart = i * 3
            if (board[rowStart] != OPEN_SPOT && board[rowStart] == board[rowStart + 1] && board[rowStart] == board[rowStart + 2]) return if (board[rowStart] == HUMAN_PLAYER) 2 else 3
        }
        if (board[0] != OPEN_SPOT && board[0] == board[4] && board[0] == board[8]) return if (board[0] == HUMAN_PLAYER) 2 else 3
        if (board[2] != OPEN_SPOT && board[2] == board[4] && board[2] == board[6]) return if (board[2] == HUMAN_PLAYER) 2 else 3

        return if (board.none { it == OPEN_SPOT }) 1 else 0
    }

    fun getDifficultyLevel(): DifficultyLevel {
        return mDifficultyLevel
    }

    fun setDifficultyLevel(difficultyLevel: DifficultyLevel) {
        mDifficultyLevel = difficultyLevel
    }
}
