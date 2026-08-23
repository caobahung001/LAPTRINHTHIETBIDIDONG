package com.habitflow.app.core.backup

import androidx.room.withTransaction
import com.habitflow.app.HabitFlowDatabase

class RestoreDataTransaction(
    private val database: HabitFlowDatabase
) {
    /**
     * Thực thi nạp dữ liệu trong một Transaction an toàn (ACID)
     * Tự động Rollback nếu có bất kỳ lỗi nào xảy ra trong quá trình nạp.
     */
    suspend fun execute(payload: HabitFlowBackupPayload) {
        database.withTransaction {
            // 1. Xóa toàn bộ dữ liệu cũ
            database.userStatsDao().clear()
            database.reminderDao().clear()
            database.goalDao().clear()
            database.occurrenceDao().clear()
            database.habitDao().clear()

            // 2. Chuyển đổi DTO sang Entities qua BackupMapper
            val habits = payload.habits.map { with(BackupMapper) { it.toEntity() } }
            val occurrences = payload.occurrences.map { with(BackupMapper) { it.toEntity() } }
            val goals = payload.goals.map { with(BackupMapper) { it.toEntity() } }
            val reminders = payload.reminders.map { with(BackupMapper) { it.toEntity() } }
            val userStats = payload.userStats?.let { with(BackupMapper) { it.toEntity() } }

            // 3. Nạp toàn bộ dữ liệu mới vào các bảng
            database.habitDao().upsertAll(habits)
            database.occurrenceDao().upsertAll(occurrences)
            database.goalDao().upsertAll(goals)
            database.reminderDao().upsertAll(reminders)
            userStats?.let { database.userStatsDao().upsert(it) }
        }
    }
}
