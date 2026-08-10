package com.habitflow.app.core.datastore

data class UserPreferences(
    val appTheme: AppTheme = AppTheme.SYSTEM,
    val useDynamicColor: Boolean = true,
    val isNotificationEnabled: Boolean = true
)