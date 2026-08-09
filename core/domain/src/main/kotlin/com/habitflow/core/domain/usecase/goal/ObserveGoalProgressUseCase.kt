package com.example.habitflow.core.domain.usecase.goal

import com.example.habitflow.core.domain.model.Goal
import com.example.habitflow.core.domain.repository.GoalRepository
import kotlinx.coroutines.flow.Flow

class ObserveGoalProgressUseCase(private val repository: GoalRepository) {
    operator fun invoke(): Flow<List<Goal>> {
        return repository.getGoals()
    }
}