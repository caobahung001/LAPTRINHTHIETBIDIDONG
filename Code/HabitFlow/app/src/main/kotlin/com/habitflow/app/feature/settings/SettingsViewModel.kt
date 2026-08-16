package com.habitflow.app.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.habitflow.app.core.data.PreferencesRepository
import com.habitflow.app.core.data.PreferencesRepositoryImpl
import com.habitflow.app.core.datastore.AppTheme
import com.habitflow.app.core.datastore.UserPreferencesDataSource
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val preferencesRepository: PreferencesRepository
) : ViewModel() {

    constructor(userPreferencesDataSource: UserPreferencesDataSource) : this(
        PreferencesRepositoryImpl(userPreferencesDataSource)
    )

    // Chuyển Flow từ DataSource thành StateFlow cho UI thu thập
    val uiState: StateFlow<SettingsUiState> = preferencesRepository.userPreferencesStream
        .map { preferences ->
            SettingsUiState.Success(userPreferences = preferences)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = SettingsUiState.Loading
        )

    // Các hàm xử lý sự kiện người dùng tương tác trên UI
    fun onThemeSelected(theme: AppTheme) {
        viewModelScope.launch {
            preferencesRepository.updateAppTheme(theme)
        }
    }

    fun onNotificationToggled(enabled: Boolean) {
        viewModelScope.launch {
            preferencesRepository.setNotificationEnabled(enabled)
        }
    }
}