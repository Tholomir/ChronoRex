package com.dino.chronorex.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build

object ReminderNotificationChannel {
    const val CHANNEL_ID: String = "check_in_reminder"

    fun ensureCreated(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = context.getSystemService(NotificationManager::class.java) ?: return
        val existing = manager.getNotificationChannel(CHANNEL_ID)
        if (existing != null) return
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Daily Check-In",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Reminds you to capture your morning check-in."
            enableVibration(true)
        }
        manager.createNotificationChannel(channel)
    }
}
