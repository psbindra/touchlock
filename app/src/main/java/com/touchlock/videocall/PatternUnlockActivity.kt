package com.touchlock.videocall

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

class PatternUnlockActivity : AppCompatActivity() {

    // Default preset pattern: 1 -> 2 -> 3 (0-indexed: [0, 1, 2])
    private val PRESET_PATTERN = listOf(0, 1, 2)

    // Emergency fallback PIN. Change this before distributing the app if desired.
    private val EMERGENCY_PIN = "1234"

    private var authenticated = false

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
                    unlockTouchLock("Unlocked successfully")
                } else {
                    tvMessage.text = "Incorrect pattern. Try again."
                    patternView.showError()
                }
            }
        })

        btnEmergencyPin.setOnClickListener {
            showEmergencyPinDialog()
        }
    }

    private fun showEmergencyPinDialog() {
        val pinInput = EditText(this).apply {
            hint = "4-digit PIN"
            inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_VARIATION_PASSWORD
            maxLines = 1
            setPadding(48, 24, 48, 24)
        }

        val dialog = AlertDialog.Builder(this)
            .setTitle("Emergency PIN")
            .setMessage("Enter the 4-digit parent PIN")
            .setView(pinInput)
            .setNegativeButton("Cancel", null)
            .setPositiveButton("Unlock", null)
            .create()

        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                val enteredPin = pinInput.text?.toString().orEmpty()
                if (enteredPin == EMERGENCY_PIN) {
                    dialog.dismiss()
                    unlockTouchLock("Unlocked via Emergency PIN")
                } else {
                    pinInput.error = "Incorrect PIN"
                    pinInput.selectAll()
                }
            }
        }

        dialog.show()
    }

    private fun unlockTouchLock(message: String) {
        authenticated = true
        val intent = Intent(this, TouchLockOverlayService::class.java).apply {
            action = TouchLockOverlayService.ACTION_UNLOCK
        }
        startService(intent)
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        finish()
    }

    override fun onStop() {
        super.onStop()

        // If the parent leaves/cancels the authentication screen, restore the
        // blocking overlay so TouchLock cannot be bypassed with Back/Home/Recents.
        if (!authenticated && !isChangingConfigurations) {
            val intent = Intent(this, TouchLockOverlayService::class.java).apply {
                action = TouchLockOverlayService.ACTION_RESUME_OVERLAY
            }
            startService(intent)
        }
    }
}
