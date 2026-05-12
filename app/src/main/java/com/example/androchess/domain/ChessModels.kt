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
    val color: ChessColor
)

// Represents a position on the 8x8 chess board
// row: 0 to 7 (where 0 is typically rank 8, and 7 is rank 1)
// col: 0 to 7 (where 0 is file A, and 7 is file H)
data class BoardPosition(
    val row: Int,
    val col: Int
)