package com.habitflow.app.core.datastore

data class UserPreferences(
    val appTheme: AppTheme = AppTheme.LIGHT,
    val useDynamicColor: Boolean = true,
    val isNotificationEnabled: Boolean = true,
    val greetingMessage: String = "Ngày mới lại bắt đầu rồi"
)
