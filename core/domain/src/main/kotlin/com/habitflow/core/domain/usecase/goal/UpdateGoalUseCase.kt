package com.habitflow.core.domain.usecase.goal

import com.habitflow.core.domain.repository.GoalRepository
import com.habitflow.core.model.Goal
import javax.inject.Inject

class UpdateGoalUseCase @Inject constructor(
    private val repository: GoalRepository
) {
    suspend operator fun invoke(goal: Goal) = repository.updateGoal(goal)
}