package com.example.androchess.ui

import android.content.Context
import android.media.SoundPool
import com.example.androchess.R

class SoundManager(context: Context) {

    // For short, overlapping sound effects (like katanas clashing)
    private val soundPool = SoundPool.Builder().setMaxStreams(5).build()

    // Load the sound effects into memory and keep their IDs
    private val moveSoundId = soundPool.load(context, R.raw.sfx_move, 1)
    private val captureSoundId = soundPool.load(context, R.raw.sfx_capture, 1)

    fun playMoveSound() {
        soundPool.play(moveSoundId, 1f, 1f, 1, 0, 1f)
    }

    fun playCaptureSound() {
        soundPool.play(captureSoundId, 1f, 1f, 1, 0, 1f)
    }

    // Clean up resources when done
    fun release() {
        soundPool.release()
    }
}

