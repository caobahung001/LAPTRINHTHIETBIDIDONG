package com.habitflow.core.domain.usecase.goal

import com.habitflow.core.domain.calculator.GoalProgressCalculator
import com.habitflow.core.domain.repository.GoalRepository
import com.habitflow.core.model.ProgressSummary
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ObserveGoalProgressUseCase @Inject constructor(
    private val goalRepository: GoalRepository
) {
    private val calculator = GoalProgressCalculator()

    operator fun invoke(): Flow<List<ProgressSummary>> {
        return goalRepository.getAllGoals().map { goals ->
            goals.map { goal ->
                val mockCount = 4
                val mockValue = 20.0
                calculator.calculate(goal, mockCount, mockValue)
            }
        }
    }

    operator fun invoke(goalId: String): Flow<ProgressSummary?> {
        return goalRepository.getGoalById(goalId).map { goal ->
            goal?.let { calculator.calculate(it, 4, 20.0) }
        }
    }
}