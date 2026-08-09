package com.example.habitflow.feature.goals

import com.example.habitflow.core.domain.model.Goal

data class GoalUiState(
    val isLoading: Boolean = false,
    val goals: List<Goal> = emptyList(),
    val errorMessage: String? = null
)