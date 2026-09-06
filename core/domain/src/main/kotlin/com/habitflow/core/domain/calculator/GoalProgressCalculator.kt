package com.habitflow.core.domain.calculator

import com.habitflow.core.model.Goal
import com.habitflow.core.model.ProgressSummary
import com.habitflow.core.model.enum.GoalMetricType

class GoalProgressCalculator {

    fun calculate(goal: Goal): ProgressSummary {
        val currentValue = goal.currentValue

        val percentage = if (goal.targetValue > 0) {
            (currentValue / goal.targetValue) * 100.0
        } else {
            0.0
        }

        return ProgressSummary(
            goalId = goal.id,
            goalName = goal.name,
            currentProgress = currentValue,
            targetValue = goal.targetValue,
            unit = goal.unit,
            percentage = percentage.coerceAtMost(100.0),
            isAchieved = currentValue >= goal.targetValue
        )
    }
}