package com.example.androchess.domain

import kotlin.math.abs
import kotlin.math.sign
import com.example.androchess.domain.ChessMove

// move validation func
fun isValidMove(
    board: Map<BoardPosition, ChessPiece>,
    from: BoardPosition,
    to: BoardPosition,
    lastMove: ChessMove? = null
): Boolean {
    val piece = board[from] ?: return false
    val targetPiece = board[to]

    // rule 1: piece cant move in its current location
    if (from == to) return false

    // rule 2: piece cant capture a piece with own color
    if (targetPiece != null && targetPiece.color == piece.color) return false

    val rowDiff = to.row - from.row
    val colDiff = to.col - from.col
    val absRowDiff = abs(rowDiff)
    val absColDiff = abs(colDiff)

    // pieces geometrical move rules
    return when (piece.type) {
        PieceType.KNIGHT -> {
            // Knight: L shape (2 & 1 or 1 & 2)
            (absRowDiff == 2 && absColDiff == 1) || (absRowDiff == 1 && absColDiff == 2)
        }
        PieceType.KING -> {
            // King: every direction only one step
            if (absRowDiff <= 1 && absColDiff <= 1) return true

            // castling
            // if king hasnt moved & can move two square left/right
            if (rowDiff == 0 && absColDiff == 2 && !piece.hasMoved) {
                val enemyColor = if (piece.color == ChessColor.WHITE) ChessColor.BLACK else ChessColor.WHITE

                // Rule 1: Cannot castle out of check
                if (isSquareAttacked(board, from, enemyColor)) return false

                val isKingside = to.col > from.col
                val step = if (isKingside) 1 else -1
                val passedSquare = BoardPosition(from.row, from.col + step)

                // Rule 2: Cannot castle through check
                if (isSquareAttacked(board, passedSquare, enemyColor)) return false

                val rookCol = if (isKingside) 7 else 0
                val rookPos = BoardPosition(from.row, rookCol)
                val rook = board[rookPos]

                // is piece at the corner a rook and hasnt move yet
                if (rook != null && rook.type == PieceType.ROOK && !rook.hasMoved) {
                    // is the path between king & rook clear
                    return isPathClear(board, from, rookPos)
                }
            }
            false // <-- İŞTE KAYIP OLAN DÖNÜŞ DEĞERİ
        } // <-- VE İŞTE SİSTEMİ ÇÖKERTEN KAYIP PARANTEZ!

        PieceType.ROOK -> {
            // Rook: whether a file or row change not both, plus only open path
            (rowDiff == 0 || colDiff == 0) && isPathClear(board, from, to)
        }
        PieceType.BISHOP -> {
            // Bishop: diagonal (file & row change must equal) + clear path
            (absRowDiff == absColDiff) && isPathClear(board, from, to)
        }
        PieceType.QUEEN -> {
            // Queen: moves like rook or bishop + clear path
            (rowDiff == 0 || colDiff == 0 || absRowDiff == absColDiff) && isPathClear(board, from, to)
        }
        PieceType.PAWN -> {
            // pawns: move depends on its color
            // blacks: (0 and 1st row) direction down (+1), whitres (6 & 7th row) direction up (-1)
            val direction = if (piece.color == ChessColor.BLACK) 1 else -1
            val startRow = if (piece.color == ChessColor.BLACK) 1 else 6

            // straith move
            if (colDiff == 0) {
                if (rowDiff == direction && targetPiece == null) return true // 1 square
                if (from.row == startRow && rowDiff == direction * 2 && targetPiece == null && board[BoardPosition(from.row + direction, from.col)] == null) return true // two square on first move
            }
            // diagonal capture
            // now plus En Passant
            // else if (absColDiff == 1 && rowDiff == direction ) {
            else if (absColDiff == 1 && rowDiff == direction && targetPiece != null && targetPiece.color != piece.color ) {
                return true
            }
            else if (absColDiff == 1 && rowDiff == direction && targetPiece == null) {
                // now have to chech if the last move was a pwn moving two squares
                if (lastMove != null && lastMove.piece.type == PieceType.PAWN) {
                    val lastMoveRowDiff = abs(lastMove.to.row - lastMove.from.row)
                    // Did it move 2 squares?
                    // is it right next to our pawn?
                    // is it on the destination column?
                    if (lastMoveRowDiff == 2 && lastMove.to.row == from.row && lastMove.to.col == to.col) {
                        return true
                    }
                }
            }
            false
        }
        else -> false // special debug case
    }
}

// obstacle check for long disteance pieces (rook, bishop, queen)
private fun isPathClear(
    board: Map<BoardPosition, ChessPiece>,
    from: BoardPosition,
    to: BoardPosition
): Boolean {
    val rowStep = (to.row - from.row).sign
    val colStep = (to.col - from.col).sign

    var currentRow = from.row + rowStep
    var currentCol = from.col + colStep

    // check all squares between current & target square
    while (currentRow != to.row || currentCol != to.col) {
        if (board.containsKey(BoardPosition(currentRow, currentCol))) {
            return false // there is a piece on road, means path blocked
        }
        currentRow += rowStep
        currentCol += colStep
    }
    return true
}

// check if a specific square is under attack by the given color
fun isSquareAttacked(
    board: Map<BoardPosition, ChessPiece>,
    targetSquare: BoardPosition,
    attackerColor: ChessColor
): Boolean {
    for ((pos, piece) in board) {
        if (piece.color == attackerColor) {
            // We use isValidMove to check if the attacker can geometrically hit the square.
            if (isValidMove(board, pos, targetSquare, null)) {
                return true
            }
        }
    }
    return false
}

// check if the king of the given color is in check
fun isKingInCheck(board: Map<BoardPosition, ChessPiece>, color: ChessColor): Boolean {
    // Find the king's position
    val kingPos = board.entries.find { it.value.type == PieceType.KING && it.value.color == color }?.key ?: return false
    val attackerColor = if (color == ChessColor.WHITE) ChessColor.BLACK else ChessColor.WHITE

    // Is the king's square attacked by the enemy?
    return isSquareAttacked(board, kingPos, attackerColor)
}

// check if a player has ANY legal moves left (for Checkmate & Stalemate)
fun hasLegalMoves(
    board: Map<BoardPosition, ChessPiece>,
    color: ChessColor,
    lastMove: ChessMove?
): Boolean {
    for ((fromPos, piece) in board) {
        if (piece.color == color) {
            // Try moving this piece to every possible square on the board
            for (row in 0..7) {
                for (col in 0..7) {
                    val toPos = BoardPosition(row, col)
                    if (isValidMove(board, fromPos, toPos, lastMove)) {

                        // Simulate the move
                        val simulatedBoard = board.toMutableMap()
                        simulatedBoard.remove(fromPos)
                        simulatedBoard[toPos] = piece

                        // If this move removes the check, we have at least one legal move!
                        if (!isKingInCheck(simulatedBoard, color)) {
                            return true
                        }
                    }
                }
            }
        }
    }
    return false // No legal moves found!
}