package com.habitflow.app.core.backup

import com.habitflow.app.HabitFlowDatabase
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class BackupManager(
    private val database: HabitFlowDatabase,
    private val restoreTransaction: RestoreDataTransaction = RestoreDataTransaction(database)
) {
    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    /**
     * Xuất toàn bộ dữ liệu hiện tại thành chuỗi JSON có định dạng đẹp mắt
     */
    suspend fun exportBackup(): String {
        val habits = database.habitDao().all().map { with(BackupMapper) { it.toDTO() } }
        val occurrences = database.occurrenceDao().all().map { with(BackupMapper) { it.toDTO() } }
        val goals = database.goalDao().all().map { with(BackupMapper) { it.toDTO() } }
        val reminders = database.reminderDao().all().map { with(BackupMapper) { it.toDTO() } }
        val userStats = database.userStatsDao().get()?.let { with(BackupMapper) { it.toDTO() } }

        val payload = HabitFlowBackupPayload(
            metadata = BackupMetadata(version = BackupValidator.CURRENT_SUPPORTED_VERSION),
            habits = habits,
            occurrences = occurrences,
            goals = goals,
            reminders = reminders,
            userStats = userStats
        )

        return json.encodeToString(payload)
    }

    /**
     * Khôi phục chuỗi JSON vào Database sau khi đã qua kiểm thực an toàn
     */
    suspend fun restoreBackup(jsonText: String) {
        val payload = json.decodeFromString<HabitFlowBackupPayload>(jsonText)

        // Kiểm tra tính toàn vẹn dữ liệu
        when (val validation = BackupValidator.validate(payload)) {
            is ValidationResult.Invalid -> error("Dữ liệu sao lưu không hợp lệ: ${validation.reason}")
            is ValidationResult.Valid -> restoreTransaction.execute(payload)
        }
    }
}
