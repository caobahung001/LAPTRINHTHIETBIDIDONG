package com.habitflow.feature.goals

import com.habitflow.core.model.ProgressSummary
import com.habitflow.core.model.enum.GoalPeriodType

sealed interface GoalListUiState {
    data object Loading : GoalListUiState
    data class Success(val items: List<ProgressSummary>) : GoalListUiState
    data class Error(val message: String) : GoalListUiState
}

data class GoalEditorUiState(
    val id: String? = null,
    val name: String = "",
    val targetValue: String = "",
    val unit: String = "lần",
    val selectedPeriod: GoalPeriodType = GoalPeriodType.MONTHLY,
    val isLoading: Boolean = false,
    val isSaved: Boolean = false,
    val errorMessage: String? = null
)