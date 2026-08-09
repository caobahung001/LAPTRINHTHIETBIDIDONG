package com.example.habitflow.core.domain.usecase.goal

import com.example.habitflow.core.domain.model.Goal
import com.example.habitflow.core.domain.repository.GoalRepository

class CreateGoalUseCase(private val repository: GoalRepository) {
    suspend operator fun invoke(goal: Goal) {
        repository.insertGoal(goal)
    }
}