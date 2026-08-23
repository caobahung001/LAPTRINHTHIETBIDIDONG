package com.habitflow.app.core.backup

sealed interface ValidationResult {
    data object Valid : ValidationResult
    data class Invalid(val reason: String) : ValidationResult
}

object BackupValidator {

    const val CURRENT_SUPPORTED_VERSION = 2

    /**
     * Kiểm tra tính toàn vẹn của dữ liệu sao lưu trước khi cho phép khôi phục vào Database
     */
    fun validate(payload: HabitFlowBackupPayload): ValidationResult {
        // 1. Kiểm tra phiên bản version
        if (payload.metadata.version > CURRENT_SUPPORTED_VERSION) {
            return ValidationResult.Invalid(
                "Phiên bản sao lưu (v${payload.metadata.version}) mới hơn phiên bản ứng dụng hiện tại (v$CURRENT_SUPPORTED_VERSION). Vui lòng cập nhật ứng dụng!"
            )
        }

        // 2. Kiểm tra danh sách thói quen có ID rỗng không
        val invalidHabit = payload.habits.find { it.id.isBlank() || it.name.isBlank() }
        if (invalidHabit != null) {
            return ValidationResult.Invalid("Dữ liệu thói quen không hợp lệ: chứa ID hoặc tên rỗng.")
        }

        // 3. Kiểm tra tính toàn vẹn khóa ngoại (Foreign key integrity check)
        val validHabitIds = payload.habits.map { it.id }.toSet()

        val orphanOccurrence = payload.occurrences.find { it.habitId !in validHabitIds }
        if (orphanOccurrence != null && payload.habits.isNotEmpty()) {
            return ValidationResult.Invalid("Dữ liệu lịch sử chứa bản ghi không thuộc bất kỳ thói quen nào.")
        }

        val orphanReminder = payload.reminders.find { it.habitId !in validHabitIds }
        if (orphanReminder != null && payload.habits.isNotEmpty()) {
            return ValidationResult.Invalid("Dữ liệu nhắc nhở chứa bản ghi không thuộc bất kỳ thói quen nào.")
        }

        // 4. Kiểm tra mốc thời gian nhắc nhở hợp lệ (0..23h, 0..59m)
        val invalidReminderTime = payload.reminders.find { it.hour !in 0..23 || it.minute !in 0..59 }
        if (invalidReminderTime != null) {
            return ValidationResult.Invalid("Dữ liệu nhắc nhở chứa mốc thời gian không hợp lệ.")
        }

        return ValidationResult.Valid
    }
}
