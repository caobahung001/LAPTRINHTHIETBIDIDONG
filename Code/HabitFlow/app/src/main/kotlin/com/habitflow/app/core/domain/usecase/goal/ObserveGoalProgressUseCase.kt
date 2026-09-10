package com.habitflow.app.domain.usecase.goal

import com.habitflow.app.GoalEntity
import com.habitflow.app.HabitRepository
import com.habitflow.app.core.model.Goal
import com.habitflow.app.core.model.toExternalModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ObserveGoalProgressUseCase(
    private val repository: HabitRepository
) {
    operator fun invoke(): Flow<List<Goal>> {
        return repository.goals.map { entities ->
            entities.map { it.toExternalModel() }
        }
    }
}