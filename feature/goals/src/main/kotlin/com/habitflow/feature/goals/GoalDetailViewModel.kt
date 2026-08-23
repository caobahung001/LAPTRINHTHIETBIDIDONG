package com.habitflow.feature.goals

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.habitflow.core.domain.usecase.goal.DeleteGoalUseCase
import com.habitflow.core.domain.usecase.goal.ObserveGoalProgressUseCase
import com.habitflow.core.model.ProgressSummary
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GoalDetailViewModel @Inject constructor(
    private val observeGoalProgressUseCase: ObserveGoalProgressUseCase,
    private val deleteGoalUseCase: DeleteGoalUseCase
) : ViewModel() {

    private val _summary = MutableStateFlow<ProgressSummary?>(null)
    val summary: StateFlow<ProgressSummary?> = _summary.asStateFlow()

    fun loadGoal(goalId: String) {
        viewModelScope.launch {
            observeGoalProgressUseCase(goalId).collect { progress ->
                _summary.value = progress
            }
        }
    }

    fun deleteGoal(goalId: String, onDeleted: () -> Unit) {
        viewModelScope.launch {
            deleteGoalUseCase(goalId)
            onDeleted()
        }
    }
}