package com.example.androchess.domain

// Creates the initial layout of a standard chess board.
// Returns a Map where the key is the position and the value is the piece.
fun createInitialBoard(): Map<BoardPosition, ChessPiece> {
    val board = mutableMapOf<BoardPosition, ChessPiece>()

    // Helper function to set up the back rank pieces for a given color
    fun setupBackRank(row: Int, color: ChessColor) {
        val pieceOrder = listOf(
            PieceType.ROOK, PieceType.KNIGHT, PieceType.BISHOP, PieceType.QUEEN,
            PieceType.KING, PieceType.BISHOP, PieceType.KNIGHT, PieceType.ROOK
        )
        for (col in 0..7) {
            board[BoardPosition(row, col)] = ChessPiece(pieceOrder[col], color)
        }
    }

    // Set up Black pieces (top of the board, rows 0 and 1)
    setupBackRank(0, ChessColor.BLACK)
    for (col in 0..7) {
        board[BoardPosition(1, col)] = ChessPiece(PieceType.PAWN, ChessColor.BLACK)
    }

    // Set up White pieces (bottom of the board, rows 6 and 7)
    setupBackRank(7, ChessColor.WHITE)
    for (col in 0..7) {
        board[BoardPosition(6, col)] = ChessPiece(PieceType.PAWN, ChessColor.WHITE)
    }

    return board
}