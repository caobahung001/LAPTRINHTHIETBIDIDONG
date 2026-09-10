package com.habitflow.app.feature.goals

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.habitflow.app.GoalDao
import com.habitflow.app.core.data.repository.OfflineGoalRepository
import com.habitflow.app.core.domain.repository.GoalRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class GoalListViewModel(
    private val repository: GoalRepository
) : ViewModel() {
    constructor(goalDao: GoalDao) : this(
        OfflineGoalRepository(goalDao)
    )

    val uiState: StateFlow<GoalListUiState> = repository.getGoals()
        .map { GoalListUiState(goals = it) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = GoalListUiState(isLoading = true)
        )

    fun incrementProgress(id: String, value: Double = 1.0) {
        viewModelScope.launch {
            repository.incrementProgress(id, value)
        }
    }

    fun deleteGoal(id: String) {
        viewModelScope.launch {
            repository.deleteGoal(id)
        }
    }
}