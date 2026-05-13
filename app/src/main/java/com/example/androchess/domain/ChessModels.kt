package com.example.androchess.domain

// Represents the color of the chess pieces and board squares
enum class ChessColor {
    WHITE, BLACK
}

// Represents the type of a chess piece
enum class PieceType {
    PAWN, ROOK, KNIGHT, BISHOP, QUEEN, KING
}

// Represents a specific chess piece with its type and color
data class ChessPiece(
    val type: PieceType,
    val color: ChessColor,
    val hasMoved: Boolean = false // required for castling
)

// Represents a position on the 8x8 chess board
// row: 0 to 7 (where 0 is typically rank 8, and 7 is rank 1)
// col: 0 to 7 (where 0 is file A, and 7 is file H)
data class BoardPosition(
    val row: Int,
    val col: Int
)

// to hold move history & captured pieces
data class ChessMove(
    val piece: ChessPiece, // moving/moved piece
    val from: BoardPosition, // destination
    val to: BoardPosition, // target
    val capturedPiece: ChessPiece?, // if there is one, otherwise NULL
    // en passant mark (necessary to know in case of undoing correctly
    val inEnPassant: Boolean = false,
    // required for undoing castling
    val isCastling: Boolean = false
)

// Represents events that happen during the game (like playing a sound)
sealed class GameEvent {
    object Move : GameEvent()
    object Capture : GameEvent()
}