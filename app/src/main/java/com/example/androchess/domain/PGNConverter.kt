package com.example.androchess.domain

// Converts a BoardPosition to standard chess algebraic coordinates (e.g., "e4")
fun BoardPosition.toAlgebraic(): String {
    // Columns 0-7 become 'a'-'h'
    val file = ('a' + this.col).toString()
    // Rows 0-7 become '8'-'1' (since row 0 is top/8th rank)
    val rank = (8 - this.row).toString()
    return "$file$rank"
}

// Converts a single ChessMove into standard PGN notation
fun ChessMove.toPGN(): String {
    // castling notation (O-O or O-O-O)
    if (this.isCastling) {
    return if (this.to.col > this.from.col) "O-O" else "O-O-O"
    }

    // Determine the piece letter
    val pieceStr = when (this.piece.type) {
        PieceType.PAWN -> "" // Pawns don't have a letter in PGN
        PieceType.KNIGHT -> "N"
        PieceType.BISHOP -> "B"
        PieceType.ROOK -> "R"
        PieceType.QUEEN -> "Q"
        PieceType.KING -> "K"
    }

    // Determine if it was a capture
    val captureStr = if (this.capturedPiece != null) {
        // If a pawn captures, it includes its starting file (e.g., "exd4")
        if (this.piece.type == PieceType.PAWN) "${('a' + this.from.col)}x" else "x"
    } else ""

    // Combine them with the destination square
    return "$pieceStr$captureStr${this.to.toAlgebraic()}"
}

// Converts a list of moves into a full PGN string
fun List<ChessMove>.toPGNString(): String {
    val sb = StringBuilder()
    for (i in this.indices step 2) {
        val moveNumber = (i / 2) + 1
        val whiteMove = this[i].toPGN()
        // Check if black has made a reply yet
        val blackMove = if (i + 1 < this.size) this[i + 1].toPGN() else ""
        sb.append("$moveNumber. $whiteMove $blackMove ")
    }
    return sb.toString().trim()
}

