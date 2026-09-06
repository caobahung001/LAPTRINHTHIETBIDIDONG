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

    private val habitId: String = savedStateHandle.get<String>("habitId") ?: ""

    fun resetState() {
        _uiState.value = GoalEditorUiState()
    }

    fun onNameChanged(name: String) {
        _uiState.update { it.copy(name = name, errorMessage = null) }
    }

    fun onMetricTypeChanged(metricType: GoalMetricType) {
        _uiState.update { currentState ->
            currentState.copy(
                metricType = metricType,
                // Nếu là COUNT thì gán mặc định "lần", nếu là VALUE thì xóa "lần" để user tự nhập (km, trang...)
                unit = if (metricType == GoalMetricType.COUNT) "lần" else if (currentState.unit == "lần") "" else currentState.unit
            )
        }
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

    // Hàm mới: Lưu ngày kết thúc tùy chỉnh từ DatePicker
    fun onCustomEndEpochDaySelected(epochDay: Long) {
        _uiState.update { it.copy(customEndEpochDay = epochDay, errorMessage = null) }
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

        // Validate ngày tùy chỉnh nếu chọn CUSTOM
        if (currentState.selectedPeriod == GoalPeriodType.CUSTOM && currentState.customEndEpochDay == null) {
            _uiState.update { it.copy(errorMessage = "Vui lòng chọn ngày kết thúc cho thời hạn tùy chỉnh") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val currentEpochDay = System.currentTimeMillis() / (1000 * 60 * 60 * 24)

                val calculatedEndEpochDay = when (currentState.selectedPeriod) {
                    GoalPeriodType.WEEKLY -> currentEpochDay + 7
                    GoalPeriodType.MONTHLY -> currentEpochDay + 30
                    GoalPeriodType.CUSTOM -> currentState.customEndEpochDay ?: (currentEpochDay + 30)
                }

                val goal = Goal(
                    id = currentState.id ?: UUID.randomUUID().toString(),
                    habitId = habitId,
                    name = currentState.name,
                    metricType = currentState.metricType,
                    periodType = currentState.selectedPeriod,
                    targetValue = target,
                    currentValue = 0.0,
                    unit = currentState.unit.ifBlank { if (currentState.metricType == GoalMetricType.COUNT) "lần" else "đơn vị" },
                    startEpochDay = currentEpochDay,
                    endEpochDay = calculatedEndEpochDay
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