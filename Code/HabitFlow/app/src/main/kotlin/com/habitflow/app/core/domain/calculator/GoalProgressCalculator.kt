package com.habitflow.app.core.domain.calculator

import com.habitflow.app.core.model.Goal

class GoalProgressCalculator {

    fun calculatePercentage(current: Int, target: Int): Float {
        if (target <= 0) return 0f
        return (current.toFloat() / target.toFloat()).coerceIn(0f, 1f)
    }

    fun isGoalCompleted(goal: Goal): Boolean {
        return goal.currentValue >= goal.targetValue
    }
}
