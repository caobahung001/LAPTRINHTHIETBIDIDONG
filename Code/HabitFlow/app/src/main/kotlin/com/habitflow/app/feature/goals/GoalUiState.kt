package com.habitflow.app.feature.goals

import com.habitflow.app.GoalEntity
import com.habitflow.app.GoalMetricType

data class GoalListUiState(
    val isLoading: Boolean = false,
    val goals: List<GoalEntity> = emptyList(),
    val userMessage: String? = null
)

data class GoalEditorUiState(
    val id: String? = null,
    val title: String = "",
    val targetValue: String = "1.0",
    val metricType: GoalMetricType = GoalMetricType.OCCURRENCE_COUNT,
    val isSaving: Boolean = false,
    val error: String? = null
) {
    val isEditing: Boolean get() = id != null
}