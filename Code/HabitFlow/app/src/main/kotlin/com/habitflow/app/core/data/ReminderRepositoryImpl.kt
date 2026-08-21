package com.habitflow.app.core.data

import com.habitflow.app.ReminderDao
import com.habitflow.app.ReminderEntity
import kotlinx.coroutines.flow.Flow

class ReminderRepositoryImpl(
    private val reminderDao: ReminderDao
) : ReminderRepository {

    override fun observeAllEnabled(): Flow<List<ReminderEntity>> =
        reminderDao.observeAllEnabled()

    override fun observeByHabit(habitId: String): Flow<List<ReminderEntity>> =
        reminderDao.observeByHabit(habitId)

    override suspend fun getById(id: String): ReminderEntity? =
        reminderDao.getById(id)

    override suspend fun saveReminder(reminder: ReminderEntity) {
        reminderDao.upsert(reminder)
    }

    override suspend fun deleteReminder(id: String) {
        reminderDao.delete(id)
    }
}
