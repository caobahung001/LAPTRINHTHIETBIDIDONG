package com.habitflow.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.habitflow.core.model.enum.GoalMetricType
import com.habitflow.core.model.enum.GoalPeriodType

@Entity(tableName = "goals")
data class GoalEntity(
    @PrimaryKey
    val id: String,
    val title: String,
    val targetValue: Double,
    val metricType: GoalMetricType,
    val periodType: GoalPeriodType,
    val startDate: Long,
    val endDate: Long? = null
)