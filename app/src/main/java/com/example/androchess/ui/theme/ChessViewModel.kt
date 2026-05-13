package com.example.androchess.ui.theme


import androidx.lifecycle.ViewModel
import com.example.androchess.domain.BoardPosition
import com.example.androchess.domain.ChessPiece
import com.example.androchess.domain.createInitialBoard
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class ChessViewModel : ViewModel() {

    // Holds the private mutable state of the board
    private val _boardState = MutableStateFlow(createInitialBoard())

    // Exposes the immutable state to the UI
    val boardState: StateFlow<Map<BoardPosition, ChessPiece>> = _boardState.asStateFlow()

    // Handles the logic of moving a piece from one square to another
    fun movePiece(from: BoardPosition, to: BoardPosition) {
        val currentBoard = _boardState.value.toMutableMap()
        val pieceToMove = currentBoard[from]

        // If there is a piece to move, execute the move
        if (pieceToMove != null) {
            currentBoard.remove(from)
            currentBoard[to] = pieceToMove

            // Update the state, triggering a UI recomposition
            _boardState.value = currentBoard
        }
    }
}