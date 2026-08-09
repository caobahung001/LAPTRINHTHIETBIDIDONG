package com.habitflow.feature.today

import com.habitflow.core.domain.model.TodayHabitItem

data class TodayUiState(
    val isLoading: Boolean = true,
    val selectedDateISO: String = "",
    val habitItems: List<TodayHabitItem> = emptyList(),
    val completionRate: Double = 0.0,
    val errorMessage: String? = null
)
