package com.habitflow.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.habitflow.core.database.dao.GoalDao
import com.habitflow.core.database.entity.GoalEntity

@Database(
    entities = [GoalEntity::class],
    version = 1,
    exportSchema = false
)
abstract class HabitFlowDatabase : RoomDatabase() {
    abstract fun goalDao(): GoalDao
}