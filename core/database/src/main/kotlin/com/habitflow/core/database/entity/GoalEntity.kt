package com.habitflow.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "goals")
data class GoalEntity(
    @PrimaryKey
    val id: String,
    val habitId: String? = null,
    val name: String,
    val metricType: String,     // Lưu tên Enum: GoalMetricType.name
    val periodType: String,     // Lưu tên Enum: GoalPeriodType.name
    val targetValue: Double,
    val currentValue: Double = 0.0,
    val unit: String,
    val startEpochDay: Long,
    val endEpochDay: Long? = null,
    val isCompleted: Boolean = false
)