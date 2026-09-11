package com.habitflow.app.core.model

import com.habitflow.app.GoalEntity
import com.habitflow.app.GoalMetricType

fun GoalEntity.toExternalModel(): Goal = Goal(
    id = id,
    title = name,
    targetValue = targetValue,
    currentValue = currentValue,
    metricType = metricType,
    startEpochDay = startEpochDay,
    endEpochDay = endEpochDay
)