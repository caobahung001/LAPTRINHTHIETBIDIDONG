package com.habitflow.app

import android.app.AlarmManager
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Calendar

// 1. QUẢN LÝ THÔNG BÁO (NOTIFICATION HELPER)
object NotificationHelper {
    const val CHANNEL_ID = "habit_reminder_channel"
    const val CHANNEL_NAME = "Nhắc nhở thói quen"
    const val CHANNEL_DESC = "Thông báo nhắc nhở thực hiện thói quen hàng ngày"

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = CHANNEL_DESC
                enableVibration(true)
                enableLights(true)
            }
            val manager = context.getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
        }
    }

    fun createReminderNotification(
        context: Context,
        notificationId: Int,
        habitName: String,
        note: String? = null
    ): Notification {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_popup_reminder)
            .setContentTitle("Nhắc nhở: $habitName")
            .setContentText(if (!note.isNullOrBlank()) note else "Đã đến giờ thực hiện thói quen của bạn!")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()
    }

    fun showNotification(
        context: Context,
        notificationId: Int,
        habitName: String,
        note: String? = null
    ) {
        val notification = createReminderNotification(context, notificationId, habitName, note)
        val manager = context.getSystemService(NotificationManager::class.java)
        manager?.notify(notificationId, notification)
    }
}

// 2. BỘ ĐIỀU PHỐI BÁO THỨC (REMINDER SCHEDULER)
object ReminderScheduler {
    fun schedule(
        context: Context,
        reminder: ReminderEntity,
        habitName: String,
        note: String = ""
    ) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return

        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra(AlarmReceiver.EXTRA_NOTIFICATION_ID, reminder.requestCode)
            putExtra(AlarmReceiver.EXTRA_HABIT_NAME, habitName)
            putExtra(AlarmReceiver.EXTRA_NOTE, note)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            reminder.requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, reminder.hour)
            set(Calendar.MINUTE, reminder.minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)

            if (timeInMillis <= System.currentTimeMillis()) {
                add(Calendar.DAY_OF_YEAR, 1)
            }
        }

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        calendar.timeInMillis,
                        pendingIntent
                    )
                } else {
                    alarmManager.setAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        calendar.timeInMillis,
                        pendingIntent
                    )
                }
            } else {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    pendingIntent
                )
            }
        } catch (e: SecurityException) {
            alarmManager.setAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                pendingIntent
            )
        }
    }

    fun cancel(context: Context, reminder: ReminderEntity) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val intent = Intent(context, AlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            reminder.requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
    }
}

// 3. CÁC BROADCAST RECEIVERS HỆ THỐNG
class AlarmReceiver : BroadcastReceiver() {
    companion object {
        const val EXTRA_NOTIFICATION_ID = "extra_notification_id"
        const val EXTRA_HABIT_NAME = "extra_habit_name"
        const val EXTRA_NOTE = "extra_note"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val notificationId = intent.getIntExtra(EXTRA_NOTIFICATION_ID, 1)
        val habitName = intent.getStringExtra(EXTRA_HABIT_NAME) ?: "Thói quen hàng ngày"
        val note = intent.getStringExtra(EXTRA_NOTE)

        NotificationHelper.showNotification(
            context = context,
            notificationId = notificationId,
            habitName = habitName,
            note = note
        )
    }
}

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED || intent.action == "android.intent.action.QUICKBOOT_POWERON") {
            val pendingResult = goAsync()
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val db = HabitFlowDatabase.get(context)
                    val activeReminders = db.reminderDao().all().filter { it.enabled }
                    val habits = db.habitDao().all().associateBy { it.id }

                    activeReminders.forEach { reminder ->
                        val habit = habits[reminder.habitId]
                        val habitName = habit?.name ?: "Thói quen hàng ngày"
                        ReminderScheduler.schedule(context, reminder, habitName)
                    }
                } finally {
                    pendingResult.finish()
                }
            }
        }
    }
}

class TimeChangeReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_TIME_CHANGED || intent.action == Intent.ACTION_TIMEZONE_CHANGED) {
            val pendingResult = goAsync()
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val db = HabitFlowDatabase.get(context)
                    val activeReminders = db.reminderDao().all().filter { it.enabled }
                    val habits = db.habitDao().all().associateBy { it.id }

                    activeReminders.forEach { reminder ->
                        val habit = habits[reminder.habitId]
                        val habitName = habit?.name ?: "Thói quen hàng ngày"
                        ReminderScheduler.schedule(context, reminder, habitName)
                    }
                } finally {
                    pendingResult.finish()
                }
            }
        }
    }
}
