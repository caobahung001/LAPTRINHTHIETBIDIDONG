package com.habitflow.app.core.domain.usecase.goal

import com.habitflow.app.core.domain.repository.GoalRepository

class DeleteGoalUseCase(
    private val goalRepository: GoalRepository
) {
    suspend operator fun invoke(goalId: String) {
        goalRepository.deleteGoal(goalId)
    }
}