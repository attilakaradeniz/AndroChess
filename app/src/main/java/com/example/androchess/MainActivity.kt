package com.example.androchess

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface // Added missing import
import androidx.compose.ui.Modifier
// Corrected the path for ChessBoardView
import com.example.androchess.ui.theme.ChessBoardView
import com.example.androchess.ui.theme.AndroChessTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AndroChessTheme {
                // Scaffold provides the safe area padding
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Surface(
                        // Apply the innerPadding here so the board doesn't overlap with the status bar
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        // Draw our beautiful chess board
                        ChessBoardView()
                    }
                }
            }
        }
    }
}