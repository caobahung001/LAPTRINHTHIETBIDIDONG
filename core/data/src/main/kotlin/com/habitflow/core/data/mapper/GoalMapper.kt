package com.habitflow.core.data.mapper

import com.habitflow.core.database.entity.GoalEntity
import com.habitflow.core.model.Goal

fun GoalEntity.toDomainModel(): Goal {
    return Goal(
        id = id,
        title = title,
        targetValue = targetValue.toInt(),
        currentValue = 0,
        progress = 0f
    )
}

fun Goal.toEntity(): GoalEntity {
    return GoalEntity(
        id = id,
        title = title,
        targetValue = targetValue.toDouble(),
        metricType = com.habitflow.core.model.enum.GoalMetricType.OCCURRENCE_COUNT,
        periodType = com.habitflow.core.model.enum.GoalPeriodType.WEEKLY,
        startDate = System.currentTimeMillis(),
        endDate = null
    )
}