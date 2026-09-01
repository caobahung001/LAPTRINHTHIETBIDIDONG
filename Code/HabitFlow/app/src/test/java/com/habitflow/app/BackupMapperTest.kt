package com.habitflow.app

import com.habitflow.app.core.backup.BackupMapper.toDTO
import com.habitflow.app.core.backup.BackupMapper.toEntity
import org.junit.Assert.assertEquals
import org.junit.Test

class BackupMapperTest {

    @Test
    fun testHabitEntityMapping() {
        val entity = HabitEntity(id = "h1", name = "Chạy bộ", unit = "km")
        val dto = entity.toDTO()
        val mappedEntity = dto.toEntity()

        assertEquals(entity.id, mappedEntity.id)
        assertEquals(entity.name, mappedEntity.name)
        assertEquals(entity.unit, mappedEntity.unit)
    }

    @Test
    fun testReminderEntityMapping() {
        val entity = ReminderEntity(id = "r1", habitId = "h1", hour = 6, minute = 30, enabled = true, requestCode = 101)
        val dto = entity.toDTO()
        val mappedEntity = dto.toEntity()

        assertEquals(entity.id, mappedEntity.id)
        assertEquals(entity.hour, mappedEntity.hour)
        assertEquals(entity.minute, mappedEntity.minute)
        assertEquals(entity.requestCode, mappedEntity.requestCode)
    }

    @Test
    fun testGoalEntityMapping() {
        val entity = GoalEntity(id = "g1", name = "Đọc 10 cuốn sách", metricType = GoalMetricType.OCCURRENCE_COUNT, targetValue = 10.0, startEpochDay = 19500)
        val dto = entity.toDTO()
        val mappedEntity = dto.toEntity()

        assertEquals(entity.id, mappedEntity.id)
        assertEquals(entity.targetValue, mappedEntity.targetValue, 0.001)
    }
}
