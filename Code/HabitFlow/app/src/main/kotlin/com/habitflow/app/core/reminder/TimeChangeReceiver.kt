package com.habitflow.app.core.reminder

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.habitflow.app.HabitFlowDatabase
import com.habitflow.app.core.domain.scheduler.AndroidReminderScheduler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class TimeChangeReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        if (action == Intent.ACTION_TIME_CHANGED || action == Intent.ACTION_TIMEZONE_CHANGED) {
            val scheduler = AndroidReminderScheduler(context)
            val database = HabitFlowDatabase.get(context)

            // Đồng bộ lại mốc thời gian báo thức khi người dùng đổi giờ hoặc múi giờ hệ thống
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
