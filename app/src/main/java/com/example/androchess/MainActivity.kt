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

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.example.androchess.ui.SoundManager
import com.example.androchess.domain.GameEvent


import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.collectAsState

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.compose.material3.Switch
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AndroChessTheme {
                val chessViewModel: ChessViewModel = viewModel()

                val context = LocalContext.current
                val soundManager = remember { SoundManager(context) }
                // NEW: Hold the state for showing/hiding the Settings Dialog
                var showSettingsDialog by remember { mutableStateOf(false) }
                // NEW: Observe the sound setting
                val isSoundEnabled = chessViewModel.isSoundEnabled.collectAsState().value

                // NEW: Listen for game events (sounds) from the ViewModel
                LaunchedEffect(Unit) {
                    chessViewModel.gameEvents.collect { event ->
                        // NEW: Play sound ONLY if the switch is ON
                        if (chessViewModel.isSoundEnabled.value) {
                            when (event) {
                                is GameEvent.Move -> soundManager.playMoveSound()
                                is GameEvent.Capture -> soundManager.playCaptureSound()
                            }
                        }
                    }
                }

// NEW: Settings Dialog UI
                if (showSettingsDialog) {
                    AlertDialog(
                        onDismissRequest = { showSettingsDialog = false },
                        title = { Text("Settings") },
                        text = {
                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Sound Effects")
                                    Switch(
                                        checked = isSoundEnabled,
                                        onCheckedChange = { chessViewModel.toggleSound() }
                                    )
                                }
                                // Gelecekte buraya Language Toggle (TR/EN) gibi başka ayarlar da eklenecek!
                            }
                        },
                        confirmButton = {
                            TextButton(onClick = { showSettingsDialog = false }) {
                                Text("OK")
                            }
                        }
                    )
                }


                // Scaffold provides the safe area padding
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                // --- YENİ: ÜST MENÜ ÇUBUĞU (TOP BAR) ---
                    topBar = {
                        TopAppBar(
                            title = { Text("AndroChess") },
                            actions = {
                                IconButton(onClick = { showSettingsDialog = true }) {
                                    Icon(
                                        imageVector = Icons.Default.Settings,
                                        contentDescription = "Settings"
                                    )
                                }
                            }
                        )
                    },
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
                                onClick = { chessViewModel.redoLastMove() }
                            ) {
                                Text("REDO")
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
                        // column placement for board & move list
                        Column(modifier = Modifier.fillMaxSize()) {

                            // Pass the ViewModel to our board view
                            ChessBoardView(viewModel = chessViewModel)

                            // move list (below the board)
                            val moveHistory = chessViewModel.moveHistory.collectAsState().value
                            val pgnText = moveHistory.toPGNString()
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f) // Kalan tüm boşluğu kapla
                                    .padding(16.dp)
                                    .verticalScroll(rememberScrollState())
                            ) {
                                Text(
                                    text = if (pgnText.isEmpty()) "Game started. Make a move!" else pgnText,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                            }
                        }
                    }
                }
            } // AndroChessTheme close
        } // setContent close
    } // onCreate close
} // MainActivity close