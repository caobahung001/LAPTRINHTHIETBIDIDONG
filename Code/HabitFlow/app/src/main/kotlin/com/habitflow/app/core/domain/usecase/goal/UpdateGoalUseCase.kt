package com.habitflow.app.domain.usecase.goal

import com.habitflow.app.core.domain.repository.GoalRepository

class UpdateGoalUseCase(
    private val goalRepository: GoalRepository
) {
    suspend operator fun invoke(goalId: String, addedValue: Double) {
        goalRepository.incrementProgress(goalId, addedValue)
    }
}