package com.touchlock.videocall

import android.content.Intent
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService

class TouchLockTileService : TileService() {

    override fun onStartListening() {
        super.onStartListening()
        val tile = qsTile ?: return
        tile.label = "Touch Lock"
        tile.updateTile()
    }

    override fun onClick() {
        super.onClick()
        val tile = qsTile ?: return
        val isActive = (tile.state == Tile.STATE_ACTIVE)

        if (isActive) {
            // Unlock request -> Open pattern dialog
            tile.state = Tile.STATE_INACTIVE
            tile.subtitle = "Touch Active"
            val intent = Intent(this, PatternUnlockActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            startActivityAndCollapse(intent)
        } else {
            // Lock Screen immediately
            tile.state = Tile.STATE_ACTIVE
            tile.subtitle = "Locked"
            val intent = Intent(this, TouchLockOverlayService::class.java).apply {
                action = TouchLockOverlayService.ACTION_LOCK
            }
            startService(intent)
        }
        tile.updateTile()
    }
}
