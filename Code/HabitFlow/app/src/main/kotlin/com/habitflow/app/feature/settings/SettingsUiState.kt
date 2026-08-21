package com.habitflow.app.feature.settings

import com.habitflow.app.core.datastore.UserPreferences

sealed interface SettingsUiState {
    object Loading : SettingsUiState
    data class Success(val userPreferences: UserPreferences) : SettingsUiState
}