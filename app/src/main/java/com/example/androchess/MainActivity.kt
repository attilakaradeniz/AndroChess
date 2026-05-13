package com.example.androchess

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier


import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.androchess.ui.theme.ChessBoardView


import com.example.androchess.ui.theme.AndroChessTheme
import com.example.androchess.ui.theme.ChessViewModel

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
                        // Instantiate the ViewModel using the Compose Lifecycle function
                        val chessViewModel: ChessViewModel = viewModel()

                        // Pass the ViewModel to our board view
                        ChessBoardView(viewModel = chessViewModel)
                    }
                }
            }
        }
    }
}