package com.habitflow.app.core.reminder

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class AlarmReceiver : BroadcastReceiver() {

    companion object {
        const val EXTRA_NOTIFICATION_ID = "extra_notification_id"
        const val EXTRA_HABIT_NAME = "extra_habit_name"
        const val EXTRA_NOTE = "extra_note"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val notificationId = intent.getIntExtra(EXTRA_NOTIFICATION_ID, System.currentTimeMillis().toInt())
        val habitName = intent.getStringExtra(EXTRA_HABIT_NAME) ?: "Thói quen hàng ngày"
        val note = intent.getStringExtra(EXTRA_NOTE)

        // Phát thông báo ra hệ thống
        NotificationFactory.showNotification(
            context = context,
            notificationId = notificationId,
            habitName = habitName,
            note = note
        )
    }
}
