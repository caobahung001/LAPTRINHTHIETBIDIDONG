package com.habitflow.feature.goals

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.habitflow.core.domain.usecase.goal.CreateGoalUseCase
import com.habitflow.core.domain.usecase.goal.UpdateGoalUseCase
import com.habitflow.core.model.Goal
import com.habitflow.core.model.enum.GoalMetricType
import com.habitflow.core.model.enum.GoalPeriodType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class GoalEditorViewModel @Inject constructor(
    private val createGoalUseCase: CreateGoalUseCase,
    private val updateGoalUseCase: UpdateGoalUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(GoalEditorUiState())
    val uiState: StateFlow<GoalEditorUiState> = _uiState.asStateFlow()

    // Lấy habitId truyền qua Navigation argument (nếu không có thì để rỗng hoặc default)
    private val habitId: String = savedStateHandle.get<String>("habitId") ?: ""

    fun onNameChanged(name: String) {
        _uiState.update { it.copy(name = name, errorMessage = null) }
    }

    fun onTargetValueChanged(targetValue: String) {
        _uiState.update { it.copy(targetValue = targetValue, errorMessage = null) }
    }

    fun onUnitChanged(unit: String) {
        _uiState.update { it.copy(unit = unit) }
    }

    fun onPeriodSelected(period: GoalPeriodType) {
        _uiState.update { it.copy(selectedPeriod = period) }
    }

    fun saveGoal() {
        val currentState = _uiState.value

        if (currentState.name.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Tên mục tiêu không được để trống") }
            return
        }

        val target = currentState.targetValue.toDoubleOrNull()
        if (target == null || target <= 0) {
            _uiState.update { it.copy(errorMessage = "Chỉ tiêu phải là số lớn hơn 0") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val currentEpochDay = System.currentTimeMillis() / (1000 * 60 * 60 * 24)

                val metricType = if (currentState.unit.lowercase() == "lần") {
                    GoalMetricType.COUNT
                } else {
                    GoalMetricType.VALUE
                }

                val goal = Goal(
                    id = currentState.id ?: UUID.randomUUID().toString(),
                    habitId = habitId,
                    name = currentState.name,
                    metricType = metricType,
                    periodType = currentState.selectedPeriod,
                    targetValue = target,
                    unit = currentState.unit,
                    startEpochDay = currentEpochDay,
                    endEpochDay = currentEpochDay + when (currentState.selectedPeriod) {
                        GoalPeriodType.WEEKLY -> 7
                        GoalPeriodType.MONTHLY -> 30
                        GoalPeriodType.CUSTOM -> 90
                    }
                )

                if (currentState.id == null) {
                    createGoalUseCase(goal)
                } else {
                    updateGoalUseCase(goal)
                }

                _uiState.update { it.copy(isLoading = false, isSaved = true) }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isLoading = false, errorMessage = e.message ?: "Lỗi lưu mục tiêu")
                }
            }
        }
    }
}