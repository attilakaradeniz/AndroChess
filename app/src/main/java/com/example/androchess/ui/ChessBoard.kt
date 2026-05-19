package com.example.androchess.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.example.androchess.domain.BoardPosition
import kotlin.math.roundToInt

val LightSquareColor = Color(0xFFEBECD0)
val DarkSquareColor = Color(0xFF779556)
val HighlightColor = Color(0x66E5A93C) // background selected piece
val HoverColor = Color(0x99A5D6A7)     // highlighting when dragging

@Composable
fun ChessBoardView(viewModel: ChessViewModel) {
    val boardState by viewModel.boardState.collectAsState()
    val isFlipped by viewModel.isBoardFlipped.collectAsState()
    val selectedPosition by viewModel.selectedPosition.collectAsState()
    val validMoves by viewModel.validMoves.collectAsState()
    val currentTurn by viewModel.currentTurn.collectAsState()

    // move history from viewModel
    val moveHistory by viewModel.moveHistory.collectAsState()

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .padding(16.dp)
    ) {
        val squareSizePx = constraints.maxWidth / 8f
        var draggedPosition by remember { mutableStateOf<BoardPosition?>(null) }
        var hoverPosition by remember { mutableStateOf<BoardPosition?>(null) }

        Column(modifier = Modifier.fillMaxSize()) {
            for (row in 0 until 8) {
                val displayRow = if (isFlipped) 7 - row else row

                // Z-INDEX
                val rowHasDragging = draggedPosition?.row == displayRow
                val rowHasHover = hoverPosition?.row == displayRow
                val rowZIndex = if (rowHasDragging) 2f else if (rowHasHover) 1f else 0f

                Row(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .zIndex(rowZIndex)
                ) {
                    for (col in 0 until 8) {
                        val displayCol = if (isFlipped) 7 - col else col
                        val currentPosition = BoardPosition(displayRow, displayCol)
                        val pieceOnSquare = boardState[currentPosition]

                        // states
                        val isSelected = selectedPosition == currentPosition
                        val isMoveTarget = validMoves.contains(currentPosition)
                        val isDraggingThisSquare = draggedPosition == currentPosition
                        val isHoveringValidTarget = hoverPosition == currentPosition && isMoveTarget

                        // last move check
                        val lastMove = moveHistory.lastOrNull()
                        val isLastMove = lastMove?.from == currentPosition || lastMove?.to == currentPosition

                        // base colors
                        val isLightSquare = (displayRow + displayCol) % 2 == 0
                        val baseColor = if (isLightSquare) LightSquareColor else DarkSquareColor

                        //
                        val squareColor = when {
                            isSelected -> HighlightColor // selected piece
                            isLastMove -> Color(0xFFE5A93C).copy(alpha = 0.5f) // last move trace
                            else -> baseColor // regular base color
                        }

                        // Z-INDEX
                        val squareZIndex = if (isDraggingThisSquare) 2f else if (isHoveringValidTarget) 1f else 0f

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxSize()
                                .background(squareColor)
                                .clickable { viewModel.onSquareClick(currentPosition) }
                                .zIndex(squareZIndex)
                        ) {

                            // target and legal move indicators
                            if (isHoveringValidTarget) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .scale(2.0f) // circle shape overflow size %200
                                        .clip(CircleShape)
                                        .background(Color.White.copy(alpha = 0.4f)) // semi transparent red
                                        //.background(Color.Red.copy(alpha = 0.4f)) // semi transparent red
                                )
                            } else if (isMoveTarget) {
                                val isCapture = pieceOnSquare != null
                                if (isCapture) {
                                    // capturable piece
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(2.dp)
                                            .border(6.dp, Color.Black.copy(alpha = 0.25f), CircleShape)
                                    )
                                } else {
                                    // clear legal square
                                    Box(
                                        modifier = Modifier
                                            .size(14.dp)
                                            .align(Alignment.Center)
                                            .clip(CircleShape)
                                            .background(Color.Black.copy(alpha = 0.25f))
                                    )
                                }
                            }

                            if (pieceOnSquare != null) {
                                var offsetX by remember { mutableStateOf(0f) }
                                var offsetY by remember { mutableStateOf(0f) }

                                val pieceScale by animateFloatAsState(
                                    targetValue = if (isDraggingThisSquare) 2.0f else 1f,
                                    label = "scale"
                                )
                                val pieceLift by animateFloatAsState(
                                    targetValue = if (isDraggingThisSquare) -150f else 0f,
                                    label = "lift"
                                )

                                val isMyTurn = pieceOnSquare.color == currentTurn

                                Image(
                                    painter = painterResource(id = getPieceDrawable(pieceOnSquare)),
                                    contentDescription = "${pieceOnSquare.color} ${pieceOnSquare.type}",
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(2.dp)
                                        .pointerInput(isMyTurn) {
                                            if (!isMyTurn) return@pointerInput

                                            detectDragGestures(
                                                onDragStart = {
                                                    draggedPosition = currentPosition
                                                    viewModel.selectPiece(currentPosition)
                                                },
                                                onDragEnd = {
                                                    draggedPosition = null
                                                    hoverPosition = null
                                                    viewModel.clearSelection()

                                                    val directionMultiplier = if (isFlipped) -1 else 1
                                                    val colOffset = (offsetX / squareSizePx).roundToInt() * directionMultiplier
                                                    val rowOffset = (offsetY / squareSizePx).roundToInt() * directionMultiplier

                                                    val targetRow = displayRow + rowOffset
                                                    val targetCol = displayCol + colOffset

                                                    if (targetRow in 0..7 && targetCol in 0..7) {
                                                        val targetPosition = BoardPosition(targetRow, targetCol)
                                                        viewModel.movePiece(currentPosition, targetPosition)
                                                    }

                                                    offsetX = 0f
                                                    offsetY = 0f
                                                },
                                                onDragCancel = {
                                                    draggedPosition = null
                                                    hoverPosition = null
                                                    viewModel.clearSelection()
                                                    offsetX = 0f
                                                    offsetY = 0f
                                                },
                                                onDrag = { change, dragAmount ->
                                                    change.consume()
                                                    offsetX += dragAmount.x
                                                    offsetY += dragAmount.y

                                                    val directionMultiplier = if (isFlipped) -1 else 1
                                                    val colOffset = (offsetX / squareSizePx).roundToInt() * directionMultiplier
                                                    val rowOffset = (offsetY / squareSizePx).roundToInt() * directionMultiplier
                                                    val targetRow = displayRow + rowOffset
                                                    val targetCol = displayCol + colOffset

                                                    val newHover = if (targetRow in 0..7 && targetCol in 0..7) {
                                                        BoardPosition(targetRow, targetCol)
                                                    } else null

                                                    if (hoverPosition != newHover) {
                                                        hoverPosition = newHover
                                                    }
                                                }
                                            )
                                        }
                                        .graphicsLayer {
                                            translationX = offsetX
                                            translationY = offsetY + pieceLift
                                            scaleX = pieceScale
                                            scaleY = pieceScale
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