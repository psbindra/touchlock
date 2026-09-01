package com.touchlock.videocall

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private val OVERLAY_PERMISSION_REQ_CODE = 101

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val btnStartService = findViewById<Button>(R.id.btnStartService)
        val btnPermission = findViewById<Button>(R.id.btnPermission)
        val tvStatus = findViewById<TextView>(R.id.tvStatus)

        btnPermission.setOnClickListener {
            checkOverlayPermission()
        }

        btnStartService.setOnClickListener {
            if (hasOverlayPermission()) {
                val intent = Intent(this, TouchLockOverlayService::class.java).apply {
                    action = TouchLockOverlayService.ACTION_TOGGLE
                }
                startService(intent)
                Toast.makeText(this, "TouchLock Active! Use Notification Shade Tile anytime.", Toast.LENGTH_LONG).show()
            } else {
                checkOverlayPermission()
            }
        }
    }

    private fun hasOverlayPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Settings.canDrawOverlays(this)
        } else true
    }

    private fun checkOverlayPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:$packageName")
            )
            startActivityForResult(intent, OVERLAY_PERMISSION_REQ_CODE)
        } else {
            Toast.makeText(this, "Overlay permission already granted!", Toast.LENGTH_SHORT).show()
        }
    }
}
