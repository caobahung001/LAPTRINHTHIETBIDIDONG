package com.habitflow.app.core.backup

import com.habitflow.app.GoalEntity
import com.habitflow.app.GoalMetricType
import com.habitflow.app.HabitEntity
import com.habitflow.app.OccurrenceEntity
import com.habitflow.app.OccurrenceStatus
import com.habitflow.app.ReminderEntity
import com.habitflow.app.UserStatsEntity

object BackupMapper {

    // === HABIT MAPPING ===
    fun HabitEntity.toDTO(): HabitDTO = HabitDTO(
        id = id,
        name = name,
        description = description,
        unit = unit,
        scheduledDays = scheduledDays,
        scheduledTime = scheduledTime,
        archived = archived,
        createdAt = createdAt
    )

    fun HabitDTO.toEntity(): HabitEntity = HabitEntity(
        id = id,
        name = name,
        description = description,
        unit = unit,
        scheduledDays = scheduledDays,
        scheduledTime = scheduledTime,
        archived = archived,
        createdAt = createdAt
    )

    // === OCCURRENCE MAPPING ===
    fun OccurrenceEntity.toDTO(): OccurrenceDTO = OccurrenceDTO(
        habitId = habitId,
        scheduledEpochDay = scheduledEpochDay,
        status = status.name,
        completedValue = completedValue,
        note = note,
        updatedAt = updatedAt
    )

    fun OccurrenceDTO.toEntity(): OccurrenceEntity = OccurrenceEntity(
        habitId = habitId,
        scheduledEpochDay = scheduledEpochDay,
        status = try {
            OccurrenceStatus.valueOf(status)
        } catch (e: IllegalArgumentException) {
            OccurrenceStatus.PENDING
        },
        completedValue = completedValue,
        note = note,
        updatedAt = updatedAt
    )

    // === GOAL MAPPING ===
    fun GoalEntity.toDTO(): GoalDTO = GoalDTO(
        id = id,
        name = name,
        metricType = metricType.name,
        targetValue = targetValue,
        currentValue = currentValue,
        unit = unit,
        startEpochDay = startEpochDay,
        endEpochDay = endEpochDay,
        archived = archived
    )

    fun GoalDTO.toEntity(): GoalEntity = GoalEntity(
        id = id,
        name = name,
        metricType = try {
            GoalMetricType.valueOf(metricType)
        } catch (e: IllegalArgumentException) {
            GoalMetricType.OCCURRENCE_COUNT
        },
        targetValue = targetValue,
        currentValue = currentValue,
        unit = unit,
        startEpochDay = startEpochDay,
        endEpochDay = endEpochDay,
        archived = archived
    )

    // === REMINDER MAPPING ===
    fun ReminderEntity.toDTO(): ReminderDTO = ReminderDTO(
        id = id,
        habitId = habitId,
        hour = hour,
        minute = minute,
        enabled = enabled,
        requestCode = requestCode
    )

    fun ReminderDTO.toEntity(): ReminderEntity = ReminderEntity(
        id = id,
        habitId = habitId,
        hour = hour,
        minute = minute,
        enabled = enabled,
        requestCode = requestCode
    )

    // === USER STATS MAPPING ===
    fun UserStatsEntity.toDTO(): UserStatsDTO = UserStatsDTO(
        id = id,
        xp = xp,
        level = level,
        streakFreezes = streakFreezes,
        lastAwardedStreakFreezeEpochDay = lastAwardedStreakFreezeEpochDay
    )

    fun UserStatsDTO.toEntity(): UserStatsEntity = UserStatsEntity(
        id = id,
        xp = xp,
        level = level,
        streakFreezes = streakFreezes,
        lastAwardedStreakFreezeEpochDay = lastAwardedStreakFreezeEpochDay
    )
}
