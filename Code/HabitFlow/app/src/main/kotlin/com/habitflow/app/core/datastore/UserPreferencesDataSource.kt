package com.habitflow.app.core.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

// Khai báo DataStore với tên file "user_preferences"
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

class UserPreferencesDataSource(private val context: Context) {

    // Định nghĩa các Keys để lưu trữ
    private object PreferencesKeys {
        val APP_THEME = stringPreferencesKey("app_theme")
        val DYNAMIC_COLOR = booleanPreferencesKey("dynamic_color")
        val NOTIFICATION_ENABLED = booleanPreferencesKey("notification_enabled")
        val GREETING_MESSAGE = stringPreferencesKey("greeting_message")
    }

    val userPreferencesStream: Flow<UserPreferences> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences()) // Trả về empty preferences nếu đọc file lỗi
            } else {
                throw exception
            }
        }
        .map { preferences ->
            val themeName = preferences[PreferencesKeys.APP_THEME] ?: AppTheme.DARK.name
            val theme = try {
                AppTheme.valueOf(themeName)
            } catch (e: IllegalArgumentException) {
                AppTheme.DARK
            }

            UserPreferences(
                appTheme = theme,
                useDynamicColor = preferences[PreferencesKeys.DYNAMIC_COLOR] ?: true,
                isNotificationEnabled = preferences[PreferencesKeys.NOTIFICATION_ENABLED] ?: true,
                greetingMessage = preferences[PreferencesKeys.GREETING_MESSAGE] ?: "Ngày mới lại bắt đầu rồi"
            )
        }

    suspend fun updateAppTheme(theme: AppTheme) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.APP_THEME] = theme.name
        }
    }

    suspend fun setGreetingMessage(message: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.GREETING_MESSAGE] = message
        }
    }

    suspend fun setDynamicColor(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.DYNAMIC_COLOR] = enabled
        }
    }

    suspend fun setNotificationEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.NOTIFICATION_ENABLED] = enabled
        }
    }
}