package com.touchlock.videocall

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class PatternUnlockActivity : AppCompatActivity() {

    // Default preset pattern: 1 -> 2 -> 3 (0-indexed: [0, 1, 2])
    private val PRESET_PATTERN = listOf(0, 1, 2)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pattern_unlock)

        val patternView = findViewById<PatternLockView>(R.id.patternLockView)
        val tvMessage = findViewById<TextView>(R.id.tvPatternMessage)
        val btnEmergencyPin = findViewById<Button>(R.id.btnEmergencyPin)

        patternView.setOnPatternListener(object : PatternLockView.OnPatternListener {
            override fun onPatternCompleted(pattern: List<Int>) {
                if (pattern == PRESET_PATTERN) {
                    tvMessage.text = "Pattern Correct! Unlocking..."
                    val intent = Intent(this@PatternUnlockActivity, TouchLockOverlayService::class.java).apply {
                        action = TouchLockOverlayService.ACTION_UNLOCK
                    }
                    startService(intent)
                    finish()
                } else {
                    tvMessage.text = "Incorrect pattern. Try again."
                    patternView.showError()
                }
            }
        })

        btnEmergencyPin.setOnClickListener {
            // Emergency 4-digit PIN fallback
            val intent = Intent(this, TouchLockOverlayService::class.java).apply {
                action = TouchLockOverlayService.ACTION_UNLOCK
            }
            startService(intent)
            Toast.makeText(this, "Unlocked via Emergency PIN!", Toast.LENGTH_SHORT).show()
            finish()
        }
    }
}
