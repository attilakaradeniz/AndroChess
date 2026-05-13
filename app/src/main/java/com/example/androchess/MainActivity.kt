package com.example.androchess

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.material3.Text

import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.androchess.ui.ChessBoardView


import com.example.androchess.ui.theme.AndroChessTheme
import com.example.androchess.ui.ChessViewModel

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.ui.unit.dp

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast

// import PGN conv from domain package
import com.example.androchess.domain.toPGNString





class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AndroChessTheme {
                val chessViewModel: ChessViewModel = viewModel()

                // Scaffold provides the safe area padding
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    floatingActionButton = {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {

                            // Copy PGN Button
                            FloatingActionButton(
                                onClick = {
                                    // grab the history and convert to PGN
                                    val moveHistory = chessViewModel.moveHistory.value
                                    val pgnString = moveHistory.toPGNString()

                                    if (pgnString.isNotEmpty()) {
                                        // copy to clipboard
                                        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                        val clip = ClipData.newPlainText("Chess PGN", pgnString)
                                        clipboard.setPrimaryClip(clip)

                                        // notification to user (Toast)
                                        Toast.makeText(this@MainActivity, "PGN Copied!", Toast.LENGTH_SHORT).show()
                                    } else {
                                        Toast.makeText(this@MainActivity, "No moves to copy", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            ) {
                                Text("PGN")
                            }

                            FloatingActionButton(
                                onClick = { chessViewModel.undoLastMove() }
                            ) {
                                Text("UNDO")
                            }

                            FloatingActionButton(
                                onClick = { chessViewModel.toggleBoardFlip() }
                            ) {
                                Text("FLIP") // TODO: turning arrows icon here
                            }
                        }
                    }

                ) { innerPadding ->
                    Surface(
                        // Apply the innerPadding here so the board doesn't overlap with the status bar
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        // Instantiate the ViewModel using the Compose Lifecycle function
                        // val chessViewModel: ChessViewModel = viewModel()

                        // Pass the ViewModel to our board view
                        ChessBoardView(viewModel = chessViewModel)
                    }
                }
            }
        }
    }
}