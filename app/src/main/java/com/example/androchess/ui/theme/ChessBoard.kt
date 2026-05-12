package com.example.androchess.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

// Standard chess board colors (pastel tones to be easy on eyes)
// LATER: adding alternatives  - this is where we will define them.
val LightSquareColor = Color(0xFFEBECD0) // Beige/White
val DarkSquareColor = Color(0xFF779556)  // Green

/**
 * A Composable that draws a simple 8x8 chess board without pieces.
 * Using aspect ratio to ensure it stays square on any screen.
 */
@Composable
fun ChessBoardView() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f) // Ensures Width == Height (Square board)
            .padding(16.dp)  // Some breathing room from screen edges
    ) {
        for (row in 0 until 8) {
            // A Row Composable represents a horizontal line of squares
            Row(modifier = Modifier.weight(1f).fillMaxWidth()) {
                for (col in 0 until 8) {
                    // Standard chess logic to determine square color
                    val isLightSquare = (row + col) % 2 == 0
                    val squareColor = if (isLightSquare) LightSquareColor else DarkSquareColor

                    // A Box Composable is a simple container to draw the square background
                    Box(
                        modifier = Modifier
                            .weight(1f) // Takes equal width in the Row
                            .fillMaxSize() // Takes equal height in the Row (via weight)
                            .background(squareColor)
                    )
                }
            }
        }
    }
}