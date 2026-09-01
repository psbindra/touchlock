package com.touchlock.videocall

import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.view.*
import android.widget.Toast
import androidx.core.app.NotificationCompat

class TouchLockOverlayService : Service() {

    private var windowManager: WindowManager? = null
    private var overlayTouchView: View? = null
    private var isLocked = false

    companion object {
        const val CHANNEL_ID = "touch_lock_channel"
        const val NOTIFICATION_ID = 2001
        const val ACTION_TOGGLE = "com.touchlock.action.TOGGLE"
        const val ACTION_LOCK = "com.touchlock.action.LOCK"
        const val ACTION_UNLOCK = "com.touchlock.action.UNLOCK"
        const val ACTION_SUSPEND_OVERLAY = "com.touchlock.action.SUSPEND_OVERLAY"
        const val ACTION_RESUME_OVERLAY = "com.touchlock.action.RESUME_OVERLAY"
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        createNotificationChannel()
        startForeground(NOTIFICATION_ID, buildNotification())
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_TOGGLE -> if (isLocked) disableLock() else enableLock()
            ACTION_LOCK -> enableLock()
            ACTION_UNLOCK -> disableLock()
            ACTION_SUSPEND_OVERLAY -> suspendOverlay()
            ACTION_RESUME_OVERLAY -> resumeOverlay()
        }
        return START_STICKY
    }

    private fun enableLock() {
        if (!isLocked) {
            isLocked = true
        }
        showOverlay()
    }

    private fun showOverlay() {
        if (!isLocked || overlayTouchView != null) return

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            else
                @Suppress("DEPRECATION")
                WindowManager.LayoutParams.TYPE_PHONE,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT
        )

        val inflater = LayoutInflater.from(this)
        overlayTouchView = inflater.inflate(R.layout.touch_lock_overlay, null)

        // Intercept and absorb touches that reach the blocking overlay.
        overlayTouchView?.setOnTouchListener { _, _ -> true }

        val unlockBadge = overlayTouchView?.findViewById<View>(R.id.btnUnlockFloating)
        unlockBadge?.setOnClickListener {
            // Remove only the blocking overlay so the parent unlock UI can receive touch.
            // The service remains logically locked until authentication succeeds.
            suspendOverlay()

            val patternIntent = Intent(this, PatternUnlockActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            }

            try {
                startActivity(patternIntent)
            } catch (e: Exception) {
                // If the unlock activity cannot open, immediately restore protection.
                resumeOverlay()
                Toast.makeText(this, "Unable to open unlock screen", Toast.LENGTH_SHORT).show()
            }
        }

        windowManager?.addView(overlayTouchView, params)
        Toast.makeText(this, "Screen Locked for Kids 👶", Toast.LENGTH_SHORT).show()
    }

    private fun suspendOverlay() {
        overlayTouchView?.let { view ->
            try {
                windowManager?.removeView(view)
            } catch (_: Exception) {
                // View may already have been detached.
            }
        }
        overlayTouchView = null
    }

    private fun resumeOverlay() {
        if (isLocked) {
            showOverlay()
        }
    }

    fun disableLock() {
        if (!isLocked && overlayTouchView == null) return
        isLocked = false
        suspendOverlay()
        Toast.makeText(this, "Touch Restored!", Toast.LENGTH_SHORT).show()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "TouchLock Screen Protection",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Keeps TouchLock active during video calls"
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    private fun buildNotification(): Notification {
        val pendingIntent = PendingIntent.getActivity(
            this, 0, Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("TouchLock is Active")
            .setContentText("Screen touch is locked. Tap Quick Tile to unlock.")
            .setSmallIcon(R.drawable.ic_lock_touch)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()
    }

    override fun onDestroy() {
        isLocked = false
        suspendOverlay()
        super.onDestroy()
    }
}
