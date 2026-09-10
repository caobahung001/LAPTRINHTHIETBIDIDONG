package com.habitflow.app.domain.usecase.goal

import com.habitflow.app.GoalEntity
import com.habitflow.app.GoalMetricType
import com.habitflow.app.HabitRepository

class CreateGoalUseCase(
    private val repository: HabitRepository
) {
    suspend operator fun invoke(
        title: String,
        targetValue: Double,
        metricType: GoalMetricType = GoalMetricType.OCCURRENCE_COUNT
    ) {
        repository.addGoal(
            name = title,
            target = targetValue,
            type = metricType
        )
    }
}