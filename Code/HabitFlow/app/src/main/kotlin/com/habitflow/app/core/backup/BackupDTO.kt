package com.habitflow.app.core.backup

import kotlinx.serialization.Serializable

@Serializable
data class BackupMetadata(
    val version: Int = 2,
    val exportedAt: Long = System.currentTimeMillis(),
    val appVersion: String = "1.0.0"
)

@Serializable
data class HabitDTO(
    val id: String,
    val name: String,
    val description: String = "",
    val unit: String = "lần",
    val scheduledDays: String = "",
    val scheduledTime: String? = null,
    val archived: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@Serializable
data class OccurrenceDTO(
    val habitId: String,
    val scheduledEpochDay: Long,
    val status: String,
    val completedValue: Double? = null,
    val note: String? = null,
    val updatedAt: Long = System.currentTimeMillis()
)

@Serializable
data class GoalDTO(
    val id: String,
    val name: String,
    val metricType: String,
    val targetValue: Double,
    val currentValue: Double = 0.0,
    val unit: String = "lần",
    val startEpochDay: Long,
    val endEpochDay: Long? = null,
    val archived: Boolean = false
)

@Serializable
data class ReminderDTO(
    val id: String,
    val habitId: String,
    val hour: Int,
    val minute: Int,
    val enabled: Boolean = true,
    val requestCode: Int
)

@Serializable
data class UserStatsDTO(
    val id: String = "current_user",
    val xp: Long = 0,
    val level: Int = 1,
    val streakFreezes: Int = 0,
    val lastAwardedStreakFreezeEpochDay: Long = 0
)

@Serializable
data class HabitFlowBackupPayload(
    val metadata: BackupMetadata = BackupMetadata(),
    val habits: List<HabitDTO> = emptyList(),
    val occurrences: List<OccurrenceDTO> = emptyList(),
    val goals: List<GoalDTO> = emptyList(),
    val reminders: List<ReminderDTO> = emptyList(),
    val userStats: UserStatsDTO? = null
)
