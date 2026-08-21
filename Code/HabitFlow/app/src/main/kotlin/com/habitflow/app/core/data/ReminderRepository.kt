package com.habitflow.app.core.data

import com.habitflow.app.ReminderEntity
import kotlinx.coroutines.flow.Flow

interface ReminderRepository {
    fun observeAllEnabled(): Flow<List<ReminderEntity>>
    fun observeByHabit(habitId: String): Flow<List<ReminderEntity>>
    suspend fun getById(id: String): ReminderEntity?
    suspend fun saveReminder(reminder: ReminderEntity)
    suspend fun deleteReminder(id: String)
}
