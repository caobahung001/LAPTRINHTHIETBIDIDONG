package com.habitflow.app.core.data

import com.habitflow.app.core.datastore.AppTheme
import com.habitflow.app.core.datastore.UserPreferences
import kotlinx.coroutines.flow.Flow

interface PreferencesRepository {
    val userPreferencesStream: Flow<UserPreferences>
    suspend fun updateAppTheme(theme: AppTheme)
    suspend fun setNotificationEnabled(enabled: Boolean)
    suspend fun setGreetingMessage(message: String)
}
