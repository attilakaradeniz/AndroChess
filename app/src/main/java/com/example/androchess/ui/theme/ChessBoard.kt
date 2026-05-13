package com.example.androchess.ui.theme

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.example.androchess.domain.BoardPosition
import kotlin.math.roundToInt
import com.example.androchess.ui.theme.ChessViewModel
import com.example.androchess.ui.theme.getPieceDrawable

val LightSquareColor = Color(0xFFEBECD0)
val DarkSquareColor = Color(0xFF779556)

@Composable
fun ChessBoardView(viewModel: ChessViewModel) {
    // Observe the board state from the ViewModel
    val boardState by viewModel.boardState.collectAsState()

    // BoxWithConstraints allows us to know the exact pixel size of the screen/board
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .padding(16.dp)
    ) {
        // Calculate the width of a single square in pixels
        val squareSizePx = constraints.maxWidth / 8f

        Column(modifier = Modifier.fillMaxSize()) {
            for (row in 0 until 8) {
                Row(modifier = Modifier.weight(1f).fillMaxWidth()) {
                    for (col in 0 until 8) {
                        val isLightSquare = (row + col) % 2 == 0
                        val squareColor = if (isLightSquare) LightSquareColor else DarkSquareColor

                        val currentPosition = BoardPosition(row, col)
                        val pieceOnSquare = boardState[currentPosition]

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxSize()
                                .background(squareColor)
                        ) {
                            if (pieceOnSquare != null) {
                                // State variables to track dragging offset
                                var offsetX by remember { mutableStateOf(0f) }
                                var offsetY by remember { mutableStateOf(0f) }
                                var isDragging by remember { mutableStateOf(false) }

                                Image(
                                    painter = painterResource(id = getPieceDrawable(pieceOnSquare)),
                                    contentDescription = "${pieceOnSquare.color} ${pieceOnSquare.type}",
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(2.dp)
                                        // Elevate the piece while dragging so it renders above other pieces
                                        .zIndex(if (isDragging) 1f else 0f)
                                        // Visually move the image based on drag offset
                                        .offset { IntOffset(offsetX.roundToInt(), offsetY.roundToInt()) }
                                        // Handle drag gestures
                                        .pointerInput(Unit) {
                                            detectDragGestures(
                                                onDragStart = { isDragging = true },
                                                onDragEnd = {
                                                    isDragging = false

                                                    // Calculate how many squares the piece was dragged
                                                    val colOffset = (offsetX / squareSizePx).roundToInt()
                                                    val rowOffset = (offsetY / squareSizePx).roundToInt()

                                                    // Determine the target position
                                                    val targetRow = row + rowOffset
                                                    val targetCol = col + colOffset

                                                    // Validate if the move is within the board limits
                                                    if (targetRow in 0..7 && targetCol in 0..7) {
                                                        val targetPosition = BoardPosition(targetRow, targetCol)
                                                        viewModel.movePiece(currentPosition, targetPosition)
                                                    }

                                                    // Reset the visual offset (the state update will redraw it at the new square)
                                                    offsetX = 0f
                                                    offsetY = 0f
                                                },
                                                onDrag = { change, dragAmount ->
                                                    change.consume()
                                                    offsetX += dragAmount.x
                                                    offsetY += dragAmount.y
                                                }
                                            )
                                        }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}