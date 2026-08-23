package com.habitflow.app.core.reminder

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.habitflow.app.HabitFlowDatabase
import com.habitflow.app.core.domain.scheduler.AndroidReminderScheduler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED || intent.action == "android.intent.action.QUICKBOOT_POWERON") {
            val scheduler = AndroidReminderScheduler(context)
            val database = HabitFlowDatabase.get(context)

            // Lập lịch lại tất cả báo thức đang kích hoạt sau khi khởi động lại máy
            CoroutineScope(Dispatchers.IO).launch {
                val habits = database.habitDao().all().associateBy { it.id }
                val reminders = database.reminderDao().all()

                reminders.filter { it.enabled }.forEach { reminder ->
                    val habitName = habits[reminder.habitId]?.name ?: "Thói quen hàng ngày"
                    scheduler.schedule(
                        reminder = reminder,
                        habitName = habitName
                    )
                }
            }
        }
    }
}
