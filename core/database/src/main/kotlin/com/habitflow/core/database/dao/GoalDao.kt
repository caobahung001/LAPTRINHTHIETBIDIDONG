package com.habitflow.core.database.dao

import androidx.room.*
import com.habitflow.core.database.entity.GoalEntity
import com.habitflow.core.database.model.GoalWithHabit
import kotlinx.coroutines.flow.Flow

@Dao
interface GoalDao {
    @Query("SELECT * FROM goals")
    fun getAllGoals(): Flow<List<GoalEntity>>

    @Query("SELECT * FROM goals WHERE id = :id")
    fun getGoalById(id: String): Flow<GoalEntity?>

    @Transaction
    @Query("SELECT *, 'Thói quen liên kết' AS habitName FROM goals")
    fun getGoalsWithHabits(): Flow<List<GoalWithHabit>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGoal(goal: GoalEntity)

    @Update
    suspend fun updateGoal(goal: GoalEntity)

    @Query("DELETE FROM goals WHERE id = :id")
    suspend fun deleteGoalById(id: String)
}