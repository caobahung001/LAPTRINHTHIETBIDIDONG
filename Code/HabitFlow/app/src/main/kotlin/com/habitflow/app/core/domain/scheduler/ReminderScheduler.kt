package com.habitflow.app.core.domain.scheduler

import com.habitflow.app.ReminderEntity

interface ReminderScheduler {
    fun schedule(reminder: ReminderEntity, habitName: String, note: String? = null)
    fun cancel(reminder: ReminderEntity)
}
