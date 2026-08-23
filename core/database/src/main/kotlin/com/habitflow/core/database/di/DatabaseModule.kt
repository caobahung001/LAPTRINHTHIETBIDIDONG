package com.habitflow.core.database.di

import android.content.Context
import androidx.room.Room
import com.habitflow.core.database.HabitFlowDatabase
import com.habitflow.core.database.dao.GoalDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideHabitFlowDatabase(
        @ApplicationContext context: Context
    ): HabitFlowDatabase {
        return Room.databaseBuilder(
            context,
            HabitFlowDatabase::class.java,
            "habitflow_database"
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    fun provideGoalDao(database: HabitFlowDatabase): GoalDao {
        return database.goalDao()
    }
}