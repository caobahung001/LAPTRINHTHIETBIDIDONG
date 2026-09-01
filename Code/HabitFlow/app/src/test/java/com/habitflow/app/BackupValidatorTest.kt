package com.habitflow.app

import com.habitflow.app.core.backup.*
import org.junit.Assert.assertTrue
import org.junit.Test

class BackupValidatorTest {

    @Test
    fun testValidPayloadReturnsValid() {
        val payload = HabitFlowBackupPayload(
            metadata = BackupMetadata(version = 2),
            habits = listOf(HabitDTO(id = "h1", name = "Uống nước")),
            occurrences = listOf(OccurrenceDTO(habitId = "h1", scheduledEpochDay = 19500, status = "COMPLETED")),
            reminders = listOf(ReminderDTO(id = "r1", habitId = "h1", hour = 8, minute = 0, requestCode = 1))
        )
        val result = BackupValidator.validate(payload)
        assertTrue(result is ValidationResult.Valid)
    }

    @Test
    fun testFutureVersionReturnsInvalid() {
        val payload = HabitFlowBackupPayload(
            metadata = BackupMetadata(version = 999)
        )
        val result = BackupValidator.validate(payload)
        assertTrue(result is ValidationResult.Invalid)
    }

    @Test
    fun testEmptyHabitNameReturnsInvalid() {
        val payload = HabitFlowBackupPayload(
            habits = listOf(HabitDTO(id = "h1", name = ""))
        )
        val result = BackupValidator.validate(payload)
        assertTrue(result is ValidationResult.Invalid)
    }

    @Test
    fun testOrphanOccurrenceReturnsInvalid() {
        val payload = HabitFlowBackupPayload(
            habits = listOf(HabitDTO(id = "h1", name = "Đọc sách")),
            occurrences = listOf(OccurrenceDTO(habitId = "h_other", scheduledEpochDay = 19500, status = "COMPLETED"))
        )
        val result = BackupValidator.validate(payload)
        assertTrue(result is ValidationResult.Invalid)
    }

    @Test
    fun testOrphanReminderReturnsInvalid() {
        val payload = HabitFlowBackupPayload(
            habits = listOf(HabitDTO(id = "h1", name = "Tập thể dục")),
            reminders = listOf(ReminderDTO(id = "r1", habitId = "h_non_existent", hour = 7, minute = 30, requestCode = 2))
        )
        val result = BackupValidator.validate(payload)
        assertTrue(result is ValidationResult.Invalid)
    }

    @Test
    fun testInvalidReminderHourReturnsInvalid() {
        val payload = HabitFlowBackupPayload(
            habits = listOf(HabitDTO(id = "h1", name = "Ngủ sớm")),
            reminders = listOf(ReminderDTO(id = "r1", habitId = "h1", hour = 25, minute = 70, requestCode = 3))
        )
        val result = BackupValidator.validate(payload)
        assertTrue(result is ValidationResult.Invalid)
    }
}
