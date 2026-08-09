package com.example.habitflow.core.domain.calculator

class GoalProgressCalculator {
    fun calculateProgress(current: Int, target: Int): Float {
        if (target <= 0) return 0f
        val result = current.toFloat() / target.toFloat()
        return result.coerceAtMost(1.0f)
    }
}