package com.habitflow.app.core.model

import com.habitflow.app.GoalMetricType

data class Goal(
    val id: String,
    val title: String,
    val targetValue: Double,
    val currentValue: Double = 0.0,
    val metricType: GoalMetricType = GoalMetricType.OCCURRENCE_COUNT,
    val startEpochDay: Long,
    val endEpochDay: Long? = null
)