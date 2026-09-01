package com.touchlock.videocall

import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import androidx.core.content.ContextCompat

class TouchLockTileService : TileService() {

    companion object {
        private const val PREFS = "touchlock_state"
        private const val KEY_LOCKED = "locked"
    }

    override fun onStartListening() {
        super.onStartListening()
        refreshTile()
    }

    override fun onClick() {
        super.onClick()

        val locked = getSharedPreferences(PREFS, MODE_PRIVATE)
            .getBoolean(KEY_LOCKED, false)

        if (locked) {
            // Keep the app logically locked, but remove the blocking overlay so
            // the parent authentication screen can receive touch input.
            val suspendIntent = Intent(this, TouchLockOverlayService::class.java).apply {
                action = TouchLockOverlayService.ACTION_SUSPEND_OVERLAY
            }
            ContextCompat.startForegroundService(this, suspendIntent)

            val unlockIntent = Intent(this, PatternUnlockActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                val pendingIntent = PendingIntent.getActivity(
                    this,
                    1001,
                    unlockIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                startActivityAndCollapse(pendingIntent)
            } else {
                @Suppress("DEPRECATION")
                startActivityAndCollapse(unlockIntent)
            }
        } else {
            val lockIntent = Intent(this, TouchLockOverlayService::class.java).apply {
                action = TouchLockOverlayService.ACTION_LOCK
            }
            ContextCompat.startForegroundService(this, lockIntent)
        }

        refreshTile()
    }

    private fun refreshTile() {
        val tile = qsTile ?: return
        val locked = getSharedPreferences(PREFS, MODE_PRIVATE)
            .getBoolean(KEY_LOCKED, false)

        tile.label = "Touch Lock"
        tile.state = if (locked) Tile.STATE_ACTIVE else Tile.STATE_INACTIVE
        tile.subtitle = if (locked) "Locked" else "Touch Active"
        tile.updateTile()
    }
}
