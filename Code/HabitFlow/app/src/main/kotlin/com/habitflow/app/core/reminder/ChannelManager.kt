package com.habitflow.app.core.reminder

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build

object ChannelManager {
    const val HABIT_REMINDER_CHANNEL_ID = "habit_reminder_channel"
    private const val CHANNEL_NAME = "Nhắc nhở thói quen"
    private const val CHANNEL_DESCRIPTION = "Kênh thông báo nhắc nhở thực hiện thói quen hàng ngày"

    /**
     * Khởi tạo Notification Channel trên Android 8.0 (API 26) trở lên
     */
    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(
                HABIT_REMINDER_CHANNEL_ID,
                CHANNEL_NAME,
                importance
            ).apply {
                description = CHANNEL_DESCRIPTION
                enableVibration(true)
                enableLights(true)
            }

            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}
