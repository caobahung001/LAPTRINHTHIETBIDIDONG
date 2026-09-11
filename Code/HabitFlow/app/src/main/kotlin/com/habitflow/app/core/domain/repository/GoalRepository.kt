package com.habitflow.app.core.domain.repository

import com.habitflow.app.GoalEntity
import kotlinx.coroutines.flow.Flow

interface GoalRepository {
    fun getGoals(): Flow<List<GoalEntity>>
    suspend fun saveGoal(goal: GoalEntity)
    suspend fun incrementProgress(goalId: String, addedValue: Double)
    suspend fun deleteGoal(id: String)
}