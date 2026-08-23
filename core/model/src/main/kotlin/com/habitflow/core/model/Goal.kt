package com.habitflow.core.model

import com.habitflow.core.model.enum.GoalMetricType
import com.habitflow.core.model.enum.GoalPeriodType

data class Goal(
    val id: String,
    val habitId: String,
    val name: String,
    val metricType: GoalMetricType,
    val periodType: GoalPeriodType,
    val targetValue: Double,
    val unit: String,
    val startEpochDay: Long,
    val endEpochDay: Long
)