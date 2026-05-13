package com.example.androchess.ui

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
import com.example.androchess.ui.ChessViewModel

val LightSquareColor = Color(0xFFEBECD0)
val DarkSquareColor = Color(0xFF779556)

@Composable
fun ChessBoardView(viewModel: ChessViewModel) {
    // Observe the board state from the ViewModel
    val boardState by viewModel.boardState.collectAsState()

    // state for boerd direction
    val isFlipped by viewModel.isBoardFlipped.collectAsState()

    // BoxWithConstraints allows us to know the exact pixel size of the screen/board
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .padding(16.dp)
    ) {
        // Calculate the width of a single square in pixels
        val squareSizePx = constraints.maxWidth / 8f

        // Keep track of which position is currently being dragged at the board level
        var draggedPosition by remember { mutableStateOf<BoardPosition?>(null) }

        Column(modifier = Modifier.fillMaxSize()) {
            for (row in 0 until 8) {
                val displayRow = if (isFlipped) 7 - row else row

                // Elevate the entire Row if it contains the piece being dragged
                val rowHasDragging = draggedPosition?.row == displayRow

                Row(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .zIndex(if (rowHasDragging) 1f else 0f) // this brings row to the front (no vanish effect)
                ) {
                    for (col in 0 until 8) {
                        // for flipboard
                        //val displayRow = if (isFlipped)  7 - row else row

                        val displayCol = if (isFlipped)  7- col else col

                        val isLightSquare = (displayRow + displayCol) % 2 == 0
                        val squareColor = if (isLightSquare) LightSquareColor else DarkSquareColor

                        val currentPosition = BoardPosition(displayRow, displayCol)
                        val pieceOnSquare = boardState[currentPosition]

                        // NEW: Elevate the specific Box if it is the one being dragged
                        val isDraggingThisSquare = draggedPosition == currentPosition

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxSize()
                                .background(squareColor)
                                .zIndex(if (isDraggingThisSquare) 1f else 0f) // square to the front
                        ) {
                            if (pieceOnSquare != null) {
                                // State variables to track dragging offset
                                var offsetX by remember { mutableStateOf(0f) }
                                var offsetY by remember { mutableStateOf(0f) }
                                //var isDragging by remember { mutableStateOf(false) }

                                Image(
                                    painter = painterResource(id = getPieceDrawable(pieceOnSquare)),
                                    contentDescription = "${pieceOnSquare.color} ${pieceOnSquare.type}",
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(2.dp)
                                        // Visually move the image based on drag offset
                                        .offset { IntOffset(offsetX.roundToInt(), offsetY.roundToInt()) }
                                        // Handle drag gestures
                                        .pointerInput(Unit) {
                                            detectDragGestures(
                                                onDragStart = {
                                                    // Tell the board which piece is moving
                                                    draggedPosition = currentPosition
                                                              },
                                                onDragEnd = {
                                                    // Clear the dragged position
                                                    draggedPosition = null

                                                    // if board flipped reverse dragging calculation
                                                    val directionMultiplier = if (isFlipped) -1 else 1

                                                    // Calculate how many squares the piece was dragged
                                                    val colOffset = (offsetX / squareSizePx).roundToInt() * directionMultiplier
                                                    val rowOffset = (offsetY / squareSizePx).roundToInt() * directionMultiplier

                                                    // Determine the target position
                                                    val targetRow = displayRow + rowOffset
                                                    val targetCol = displayCol + colOffset

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