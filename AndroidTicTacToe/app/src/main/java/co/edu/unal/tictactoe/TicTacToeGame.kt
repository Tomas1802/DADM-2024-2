package co.edu.unal.tictactoe

import kotlin.random.Random

class TicTacToeGame {

    companion object {
        const val HUMAN_PLAYER = 'X'
        const val COMPUTER_PLAYER = 'O'
        const val OPEN_SPOT = ' '
        const val BOARD_SIZE = 9
    }

    private val board = CharArray(BOARD_SIZE) { OPEN_SPOT }

    fun getBoard(): CharArray {
        return board
    }

    fun clearBoard() {
        for (i in board.indices) {
            board[i] = OPEN_SPOT
        }
    }

    fun setMove(player: Char, location: Int): Boolean {
        if (board[location] == OPEN_SPOT) {
            board[location] = player
            return true
        }
        return false
    }

    fun getComputerMove(): Int {
        // Simple AI: Try to win, block the human, or pick a random move
        for (i in board.indices) {
            if (board[i] == OPEN_SPOT) {
                board[i] = COMPUTER_PLAYER
                if (checkForWinner() == 3) {
                    board[i] = OPEN_SPOT
                    return i
                }
                board[i] = OPEN_SPOT
            }
        }

        for (i in board.indices) {
            if (board[i] == OPEN_SPOT) {
                board[i] = HUMAN_PLAYER
                if (checkForWinner() == 2) {
                    board[i] = OPEN_SPOT
                    return i
                }
                board[i] = OPEN_SPOT
            }
        }

        val openSpots = board.indices.filter { board[it] == OPEN_SPOT }
        return openSpots.random()
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
}
