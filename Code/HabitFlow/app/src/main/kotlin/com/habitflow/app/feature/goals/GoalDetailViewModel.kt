package com.habitflow.app.feature.goals

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.habitflow.app.GoalEntity
import com.habitflow.app.core.domain.repository.GoalRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class GoalDetailUiState(
    val isLoading: Boolean = true,
    val goal: GoalEntity? = null,
    val progressPercentage: Float = 0f
)

class GoalDetailViewModel(
    savedStateHandle: SavedStateHandle,
    private val repository: GoalRepository
) : ViewModel() {

    // Lấy goalId được truyền từ Navigation argument
    private val goalId: String = checkNotNull(savedStateHandle["goalId"])

    val uiState: StateFlow<GoalDetailUiState> = repository.getGoals()
        .map { goals ->
            val targetGoal = goals.find { it.id == goalId }
            if (targetGoal != null) {
                val percentage = if (targetGoal.targetValue > 0) {
                    (targetGoal.currentValue / targetGoal.targetValue).toFloat().coerceIn(0f, 1f)
                } else 0f

                GoalDetailUiState(
                    isLoading = false,
                    goal = targetGoal,
                    progressPercentage = percentage
                )
            } else {
                GoalDetailUiState(isLoading = false, goal = null)
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = GoalDetailUiState(isLoading = true)
        )

    fun incrementProgress(value: Double = 1.0) {
        viewModelScope.launch {
            repository.incrementProgress(goalId, value)
        }
    }

    fun deleteGoal(onDeleted: () -> Unit) {
        viewModelScope.launch {
            repository.deleteGoal(goalId)
            onDeleted()
        }
    }
}