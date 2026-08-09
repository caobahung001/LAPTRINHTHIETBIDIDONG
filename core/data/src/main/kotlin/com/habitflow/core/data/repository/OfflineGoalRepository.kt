package com.habitflow.core.data.repository

import com.habitflow.core.data.mapper.toDomainModel
import com.habitflow.core.data.mapper.toEntity
import com.habitflow.core.database.dao.GoalDao
import com.habitflow.core.domain.repository.GoalRepository
import com.habitflow.core.model.Goal
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class OfflineGoalRepository(
    private val goalDao: GoalDao
) : GoalRepository {

    override fun getGoals(): Flow<List<Goal>> {
        return goalDao.observeAllGoals().map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    override fun getGoalById(id: String): Flow<Goal?> {
        return goalDao.observeGoalById(id).map { it?.toDomainModel() }
    }

    override suspend fun insertGoal(goal: Goal) {
        goalDao.insertGoal(goal.toEntity())
    }

    override suspend fun updateGoal(goal: Goal) {
        goalDao.updateGoal(goal.toEntity())
    }

    override suspend fun deleteGoal(id: String) {
        goalDao.deleteGoalById(id)
    }
}