package co.edu.unal.tictactoe.models

data class Game(
    val id: String = "",                  // Unique Game ID
    val player1: String = "",             // Player 1 ID
    val player2: String = "",             // Player 2 ID
    val currentTurn: String = "",         // Player ID whose turn it is
    val status: String = "waiting",       // Game status: "waiting", "ongoing", "finished"
    val board: List<String> = List(9) { " " }// Represents the 3x3 Tic-Tac-Toe board
)