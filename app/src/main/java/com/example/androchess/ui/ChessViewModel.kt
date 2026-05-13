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
        // get to last move & chech whether it was en Passant or not
        val lastMove = _moveHistory.value.lastOrNull()
        // check if piece exists
        //check if its the correct players turn
        // check if the move is valid



        if (pieceToMove != null && pieceToMove.color ==_currentTurn.value &&  isValidMove(currentBoard, from, to , lastMove)) {
            // to hold captured piece if there is one
            var capturedPiece = currentBoard[to]
            var isEnPassant = false
            var enPassantCapturedPos: BoardPosition? = null

            // Detect if this valid move is an En Passant execution
            // A pawn moving diagonally to an empty square is definitively an En Passant

            if (pieceToMove.type == PieceType.PAWN && from.col != to.col && currentBoard[to] == null) {
                isEnPassant = true
                enPassantCapturedPos = BoardPosition(from.row, to.col) // the square beside our pawn
                capturedPiece = currentBoard[enPassantCapturedPos]
            }

            // save the move
            val move = ChessMove(pieceToMove, from, to, capturedPiece, isEnPassant)
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

            // NEW: Remove the ghost pawn that was captured via En Passant
            if (isEnPassant && enPassantCapturedPos != null) {
                currentBoard.remove(enPassantCapturedPos)
            }

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
            // UPDATED: Put the captured pawn back to its correct square if it was En Passant
            if (lastMove.inEnPassant) {
                val originalPawnPosition = BoardPosition(lastMove.from.row, lastMove.to.col)
                currentBoard[originalPawnPosition] = lastMove.capturedPiece
            } else {
                currentBoard[lastMove.to] = lastMove.capturedPiece
            }
        }
        _boardState.value = currentBoard
        _moveHistory.value = history.dropLast(1)
        // Switch the turn back
        _currentTurn.value = if (_currentTurn.value == ChessColor.WHITE) ChessColor.BLACK else ChessColor.WHITE

    }
}