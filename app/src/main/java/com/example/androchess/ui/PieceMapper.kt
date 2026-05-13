package com.example.androchess.ui

import com.example.androchess.R
import com.example.androchess.domain.ChessColor
import com.example.androchess.domain.ChessPiece
import com.example.androchess.domain.PieceType

// Maps our domain piece to the Android drawable resource ID
fun getPieceDrawable(piece: ChessPiece): Int {
    return when (piece.color) {
        ChessColor.WHITE -> when (piece.type) {
            PieceType.PAWN -> R.drawable.wp // Replace with your actual file name
            PieceType.KNIGHT -> R.drawable.wn
            PieceType.BISHOP -> R.drawable.wb
            PieceType.ROOK -> R.drawable.wr
            PieceType.QUEEN -> R.drawable.wq
            PieceType.KING -> R.drawable.wk
        }
        ChessColor.BLACK -> when (piece.type) {
            PieceType.PAWN -> R.drawable.bp
            PieceType.KNIGHT -> R.drawable.bn
            PieceType.BISHOP -> R.drawable.bb
            PieceType.ROOK -> R.drawable.br
            PieceType.QUEEN -> R.drawable.bq
            PieceType.KING -> R.drawable.bk
        }
    }
}