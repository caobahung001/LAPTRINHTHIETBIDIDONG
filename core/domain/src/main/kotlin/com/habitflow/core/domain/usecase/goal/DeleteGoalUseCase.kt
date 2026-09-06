package com.habitflow.core.domain.usecase.goal

import com.habitflow.core.domain.repository.GoalRepository
import javax.inject.Inject

class DeleteGoalUseCase @Inject constructor(
    private val repository: GoalRepository
) {
    suspend operator fun invoke(goalId: String) = repository.deleteGoal(goalId)
}