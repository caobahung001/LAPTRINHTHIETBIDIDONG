package com.habitflow.core.data.mapper

import com.habitflow.core.database.entity.GoalEntity
import com.habitflow.core.model.Goal
import com.habitflow.core.model.enum.GoalMetricType
import com.habitflow.core.model.enum.GoalPeriodType

fun GoalEntity.toDomain(): Goal {
    return Goal(
        id = id,
        habitId = habitId,
        name = name,
        metricType = GoalMetricType.valueOf(metricType),
        periodType = GoalPeriodType.valueOf(periodType),
        targetValue = targetValue,
        currentValue = currentValue,
        unit = unit,
        startEpochDay = startEpochDay,
        endEpochDay = endEpochDay,
        isCompleted = isCompleted
    )
}

fun Goal.toEntity(): GoalEntity {
    return GoalEntity(
        id = id,
        habitId = habitId,
        name = name,
        metricType = metricType.name,
        periodType = periodType.name,
        targetValue = targetValue,
        currentValue = currentValue,
        unit = unit,
        startEpochDay = startEpochDay,
        endEpochDay = endEpochDay,
        isCompleted = isCompleted
    )
}