package com.example.habitflow.core.domain.usecase.goal

import com.example.habitflow.core.domain.repository.GoalRepository

class DeleteGoalUseCase(private val repository: GoalRepository) {
    suspend operator fun invoke(id: String) {
        repository.deleteGoal(id)
    }
}