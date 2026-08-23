package com.habitflow.core.data.repository

import com.habitflow.core.data.mapper.toDomain
import com.habitflow.core.data.mapper.toEntity
import com.habitflow.core.database.dao.GoalDao
import com.habitflow.core.domain.repository.GoalRepository
import com.habitflow.core.model.Goal
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class OfflineGoalRepository @Inject constructor(
    private val goalDao: GoalDao
) : GoalRepository {

    override fun getAllGoals(): Flow<List<Goal>> {
        return goalDao.getAllGoals().map { list -> list.map { it.toDomain() } }
    }

    override fun getGoalById(id: String): Flow<Goal?> {
        return goalDao.getGoalById(id).map { it?.toDomain() }
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