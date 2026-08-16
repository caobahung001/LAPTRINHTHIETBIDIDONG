package com.habitflow.app.core.reminder

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.habitflow.app.MainActivity

object NotificationFactory {

    /**
     * Tạo Notification đối tượng để phát ra hệ thống
     */
    fun createReminderNotification(
        context: Context,
        habitName: String,
        reminderNote: String? = null
    ): Notification {
        // Intent điều hướng mở lại MainActivity khi người dùng chạm vào thông báo
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val contentText = if (!reminderNote.isNullOrBlank()) {
            reminderNote
        } else {
            "Đã đến giờ thực hiện thói quen: $habitName!"
        }

        return NotificationCompat.Builder(context, ChannelManager.HABIT_REMINDER_CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Nhắc nhở: $habitName")
            .setContentText(contentText)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()
    }

    /**
     * Bắn thông báo trực tiếp ra thanh trạng thái
     */
    fun showNotification(
        context: Context,
        notificationId: Int,
        habitName: String,
        note: String? = null
    ) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notification = createReminderNotification(context, habitName, note)
        notificationManager.notify(notificationId, notification)
    }
}
