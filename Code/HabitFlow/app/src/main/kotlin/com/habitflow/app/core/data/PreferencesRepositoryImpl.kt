package com.habitflow.app.core.data

import com.habitflow.app.core.datastore.AppTheme
import com.habitflow.app.core.datastore.UserPreferences
import com.habitflow.app.core.datastore.UserPreferencesDataSource
import kotlinx.coroutines.flow.Flow

class PreferencesRepositoryImpl(
    private val userPreferencesDataSource: UserPreferencesDataSource
) : PreferencesRepository {

    override val userPreferencesStream: Flow<UserPreferences> =
        userPreferencesDataSource.userPreferencesStream

    override suspend fun updateAppTheme(theme: AppTheme) {
        userPreferencesDataSource.updateAppTheme(theme)
    }

    override suspend fun setNotificationEnabled(enabled: Boolean) {
        userPreferencesDataSource.setNotificationEnabled(enabled)
    }

    override suspend fun setGreetingMessage(message: String) {
        userPreferencesDataSource.setGreetingMessage(message)
    }
}
