package com.example.androchess.ui

import android.util.Log

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

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import com.example.androchess.domain.GameEvent

import com.example.androchess.domain.isKingInCheck
import com.example.androchess.domain.hasLegalMoves

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



    private val _gameEvents = MutableSharedFlow<GameEvent>()
    val gameEvents = _gameEvents.asSharedFlow()



    fun toggleBoardFlip() {
        _isBoardFlipped.value = !_isBoardFlipped.value
    }




    // ...
    fun movePiece(from: BoardPosition, to: BoardPosition) {
        val currentBoard = _boardState.value.toMutableMap()
        val pieceToMove = currentBoard[from]
        val lastMove = _moveHistory.value.lastOrNull()

        if (pieceToMove != null && pieceToMove.color == _currentTurn.value && isValidMove(currentBoard, from, to, lastMove)) {
            var capturedPiece = currentBoard[to]
            var isEnPassant = false
            var isCastling = false
            var enPassantCapturedPos: BoardPosition? = null

            // Detect En Passant
            if (pieceToMove.type == PieceType.PAWN && from.col != to.col && currentBoard[to] == null) {
                isEnPassant = true
                enPassantCapturedPos = BoardPosition(from.row, to.col)
                capturedPiece = currentBoard[enPassantCapturedPos]
            }

            // Detect Castling
            if (pieceToMove.type == PieceType.KING && kotlin.math.abs(from.col - to.col) == 2) {
                isCastling = true
            }

            // Pawn promotion logic
            var pieceToPlace = pieceToMove
            if (pieceToMove.type == PieceType.PAWN) {
                if ((pieceToMove.color == ChessColor.WHITE && to.row == 0) ||
                    (pieceToMove.color == ChessColor.BLACK && to.row == 7)) {
                    pieceToPlace = ChessPiece(PieceType.QUEEN, pieceToMove.color)
                }
            }

            // --- YENİ: PARALEL EVREN SİMÜLASYONU (Açmaz ve Şah Kontrolü) ---
            val simulatedBoard = currentBoard.toMutableMap()
            simulatedBoard.remove(from)
            simulatedBoard[to] = pieceToPlace
            if (isEnPassant && enPassantCapturedPos != null) simulatedBoard.remove(enPassantCapturedPos)

            // If this move leaves our own king in check, abort the move! (Absolute Pin or Moving into Check)
            if (isKingInCheck(simulatedBoard, pieceToMove.color)) {
                return // DO NOTHING. UI will snap the piece back.
            }
            // ---------------------------------------------------------------

            // save the move
            val move = ChessMove(
                piece = pieceToMove,
                from = from,
                to = to,
                capturedPiece = capturedPiece,
                inEnPassant = isEnPassant,
                isCastling = isCastling
            )
            _moveHistory.value = _moveHistory.value + move

            pieceToPlace = pieceToPlace.copy(hasMoved = true)

            // execute the move on board
            currentBoard.remove(from)
            currentBoard[to] = pieceToPlace

            if (isEnPassant && enPassantCapturedPos != null) {
                currentBoard.remove(enPassantCapturedPos)
            }

            if (isCastling) {
                val isKingside = to.col > from.col
                val rookStartCol = if (isKingside) 7 else 0
                val rookEndCol = if (isKingside) to.col - 1 else to.col + 1
                val rookStartPos = BoardPosition(from.row, rookStartCol)
                val rookEndPos = BoardPosition(from.row, rookEndCol)
                val rook = currentBoard[rookStartPos]
                if (rook != null) {
                    currentBoard.remove(rookStartPos)
                    currentBoard[rookEndPos] = rook.copy(hasMoved = true)
                }
            }

            _boardState.value = currentBoard
            val nextTurn = if (_currentTurn.value == ChessColor.WHITE) ChessColor.BLACK else ChessColor.WHITE
            _currentTurn.value = nextTurn

            viewModelScope.launch {
                if (capturedPiece != null || isEnPassant) {
                    _gameEvents.emit(GameEvent.Capture)
                } else {
                    _gameEvents.emit(GameEvent.Move)
                }
            }

            // --- YENİ: ŞAH-MAT VE PAT KONTROLÜ (Oyun bitti mi?) ---
            val isNextPlayerInCheck = isKingInCheck(currentBoard, nextTurn)
            val hasNextPlayerMoves = hasLegalMoves(currentBoard, nextTurn, move)

            if (!hasNextPlayerMoves) {
                if (isNextPlayerInCheck) {
                    //println("GAME OVER! CHECKMATE! ${pieceToMove.color} wins!")
                    // Log.d (d = debug)
                    Log.d("AndroChess_Game", "GAME OVER! CHECKMATE! ${pieceToMove.color} wins!")
                    // TODO: UI Dialog
                } else {
                    //println("GAME OVER! STALEMATE! It's a draw.")
                    Log.d("AndroChess_Game", "GAME OVER! STALEMATE! It's a draw.")
                }
            } else if (isNextPlayerInCheck) {
                //println("CHECK!") // Şah çekildi!
                Log.d("AndroChess_Game", "CHECK!")
            }
            // ------------------------------------------------------
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

        if (lastMove.isCastling) {
            val isKingside = lastMove.to.col > lastMove.from.col
            val rookCurrentCol = if (isKingside) 5 else 3 // the square for rook after castling
            val rookOriginalCol = if (isKingside) 7 else 0 // rooks original square

            val rookCurrentPos = BoardPosition(lastMove.from.row, rookCurrentCol)
            val rookOriginalPos = BoardPosition(lastMove.from.row, rookOriginalCol)

            val rook = currentBoard[rookCurrentPos]
            if (rook != null) {
                currentBoard.remove(rookCurrentPos)
                currentBoard[rookOriginalPos] = rook.copy(hasMoved = false)
            }
        }

        // common piece capture or en passant cases
        if (lastMove.capturedPiece != null && !lastMove.isCastling) {

            if (lastMove.inEnPassant) {
                val originalPawnPosition = BoardPosition(lastMove.from.row, lastMove.to.col)
                currentBoard[originalPawnPosition] = lastMove.capturedPiece
            } else {
                currentBoard[lastMove.to] = lastMove.capturedPiece
            }
        }

//            val rookStartCol = if (isKingside) 7 else 0
//            val rookEndCol = if (isKingside) lastMove.to.col - 1 else lastMove.to.col + 1

//            val rookStartPos = BoardPosition(lastMove.from.row, rookStartCol)
//            val rookEndPos = BoardPosition(lastMove.from.row, rookEndCol)

//            val rook = currentBoard[rookEndPos]
//            if (rook != null) {
//                currentBoard.remove(rookEndPos)
//                // move rook its originated square & flag as not moved yet
//                currentBoard[rookStartPos] = rook.copy(hasMoved = false)
        //           }
        //       }



        // undo castling


        _boardState.value = currentBoard
        _moveHistory.value = history.dropLast(1)
        // Switch the turn back
        _currentTurn.value = if (_currentTurn.value == ChessColor.WHITE) ChessColor.BLACK else ChessColor.WHITE

    }
}