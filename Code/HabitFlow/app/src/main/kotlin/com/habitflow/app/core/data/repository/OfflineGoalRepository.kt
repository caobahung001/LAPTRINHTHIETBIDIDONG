package com.habitflow.app.core.data.repository

import com.habitflow.app.GoalDao
import com.habitflow.app.GoalEntity
import com.habitflow.app.core.domain.repository.GoalRepository
import kotlinx.coroutines.flow.Flow

class OfflineGoalRepository(
    private val goalDao: GoalDao
) : GoalRepository {

    override fun getGoals(): Flow<List<GoalEntity>> = goalDao.observeActive()

    override suspend fun saveGoal(goal: GoalEntity) {
        goalDao.upsert(goal)
    }

    override suspend fun incrementProgress(goalId: String, addedValue: Double) {
        val currentGoals = goalDao.all()
        val targetGoal = currentGoals.find { it.id == goalId }
        targetGoal?.let { goal ->
            val updatedGoal = goal.copy(
                currentValue = goal.currentValue + addedValue
            )
            goalDao.upsert(updatedGoal)
        }
    }

    override suspend fun deleteGoal(id: String) {
        val currentGoals = goalDao.all()
        val targetGoal = currentGoals.find { it.id == id }
        targetGoal?.let { goal ->
            val archivedGoal = goal.copy(archived = true)
            goalDao.upsert(archivedGoal)
        }
    }
}