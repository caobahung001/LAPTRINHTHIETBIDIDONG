package com.habitflow.core.data.di

import com.habitflow.core.data.repository.OfflineGoalRepository
import com.habitflow.core.domain.repository.GoalRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindGoalRepository(
        impl: OfflineGoalRepository
    ): GoalRepository
}