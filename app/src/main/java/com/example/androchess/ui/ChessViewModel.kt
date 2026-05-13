package com.example.androchess.ui

import androidx.lifecycle.ViewModel
import com.example.androchess.domain.BoardPosition
import com.example.androchess.domain.ChessPiece
import com.example.androchess.domain.createInitialBoard
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import com.example.androchess.domain.isValidMove
import com.example.androchess.domain.ChessMove

import com.example.androchess.domain.ChessColor
import com.example.androchess.domain.PieceType

class ChessViewModel : ViewModel() {

    // Holds the private mutable state of the board
    private val _boardState = MutableStateFlow(createInitialBoard())
    // Exposes the immutable state to the UI
    val boardState: StateFlow<Map<BoardPosition, ChessPiece>> = _boardState.asStateFlow()

    private val _isBoardFlipped = MutableStateFlow(false)
    val isBoardFlipped: StateFlow<Boolean> = _isBoardFlipped.asStateFlow()

    private val _moveHistory = MutableStateFlow<List<ChessMove>>(emptyList())
    val moveHistory: StateFlow<List<ChessMove>> = _moveHistory.asStateFlow()

    // White moves first
    private val _currentTurn = MutableStateFlow(ChessColor.WHITE)
    val currentTurn: StateFlow<ChessColor> = _currentTurn.asStateFlow()

    fun toggleBoardFlip() {
        _isBoardFlipped.value = !_isBoardFlipped.value
    }


    // Handles the logic of moving a piece from one square to another
    fun movePiece(from: BoardPosition, to: BoardPosition) {
        val currentBoard = _boardState.value.toMutableMap()
        val pieceToMove = currentBoard[from]

        // check if piece exists
        //check if its the correct players turn
        // check if the move is valid


        if (pieceToMove != null && pieceToMove.color ==_currentTurn.value &&  isValidMove(currentBoard, from, to)) {
            // to hold captured piece if there is one
            val capturedPiece = currentBoard[to]

            // save the move
            val move = ChessMove(pieceToMove, from, to, capturedPiece)
            _moveHistory.value = _moveHistory.value + move

            // pawn promootion logic (for now auto queen) TODO: extend to underpromotion
            var pieceToPlace = pieceToMove

            // if a pawn reaches to the 0th row (w) or 7th row (b) promote to queen
            if (pieceToMove.type == PieceType.PAWN) {
                if ((pieceToMove.color == ChessColor.WHITE && to.row == 0) ||
                    (pieceToMove.color == ChessColor.BLACK && to.row == 7)) {
                    pieceToPlace = ChessPiece(PieceType.QUEEN, pieceToMove.color)
                }
            }


            // execute the move on board
            currentBoard.remove(from)
            //currentBoard[to] = pieceToMove
            currentBoard[to] = pieceToPlace
            _boardState.value = currentBoard

            // Switch the turn after a successful move
            _currentTurn.value = if (_currentTurn.value == ChessColor.WHITE) ChessColor.BLACK else ChessColor.WHITE

        }
    }
    fun undoLastMove() {
        val history = _moveHistory.value
        if (history.isEmpty()) return // if there is no move to rewind DO NOTHING

        val lastMove = history.last()
        val currentBoard = _boardState.value.toMutableMap()

        // take back the moved piece to its old square
        currentBoard[lastMove.from] = lastMove.piece

        // remove the piece on its own new square
        currentBoard.remove(lastMove.to)

        // if a piece capture happened pull alive and put it on target square
        if (lastMove.capturedPiece != null) {
            currentBoard[lastMove.to] = lastMove.capturedPiece
        }
        _boardState.value = currentBoard
        _moveHistory.value = history.dropLast(1)

        // Switch the turn back
        _currentTurn.value = if (_currentTurn.value == ChessColor.WHITE) ChessColor.BLACK else ChessColor.WHITE

    }
}