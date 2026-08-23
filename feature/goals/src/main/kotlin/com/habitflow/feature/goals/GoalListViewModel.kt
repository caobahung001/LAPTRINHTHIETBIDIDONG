package com.habitflow.feature.goals

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.habitflow.core.domain.usecase.goal.CreateGoalUseCase
import com.habitflow.core.domain.usecase.goal.DeleteGoalUseCase
import com.habitflow.core.domain.usecase.goal.ObserveGoalProgressUseCase
import com.habitflow.core.domain.usecase.goal.UpdateGoalUseCase
import com.habitflow.core.model.Goal
import com.habitflow.core.model.enum.GoalMetricType
import com.habitflow.core.model.enum.GoalPeriodType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class GoalListViewModel @Inject constructor(
    observeGoalProgressUseCase: ObserveGoalProgressUseCase,
    private val createGoalUseCase: CreateGoalUseCase,
    private val updateGoalUseCase: UpdateGoalUseCase,
    private val deleteGoalUseCase: DeleteGoalUseCase
) : ViewModel() {

    // 1. Observe danh sách tiến độ Goal
    val uiState: StateFlow<GoalListUiState> = observeGoalProgressUseCase()
        .map<_, GoalListUiState> { items ->
            GoalListUiState.Success(items)
        }
        .catch { throwable ->
            emit(GoalListUiState.Error(throwable.message ?: "Lỗi tải danh sách mục tiêu"))
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = GoalListUiState.Loading
        )

    // 2. Create Goal (Tạo mới mục tiêu)
    fun createGoal(
        habitId: String,
        name: String,
        targetValue: Double,
        unit: String = "lần",
        metricType: GoalMetricType = GoalMetricType.COUNT,
        periodType: GoalPeriodType = GoalPeriodType.WEEKLY,
        startEpochDay: Long = System.currentTimeMillis() / (1000 * 60 * 60 * 24),
        endEpochDay: Long = startEpochDay + 30
    ) {
        viewModelScope.launch {
            val newGoal = Goal(
                id = UUID.randomUUID().toString(),
                habitId = habitId,
                name = name,
                metricType = metricType,
                periodType = periodType,
                targetValue = targetValue,
                unit = unit,
                startEpochDay = startEpochDay,
                endEpochDay = endEpochDay
            )
            createGoalUseCase(newGoal)
        }
    }

    // 3. Update Goal (Cập nhật thông tin mục tiêu)
    fun updateGoal(goal: Goal) {
        viewModelScope.launch {
            updateGoalUseCase(goal)
        }
    }

    // 4. Delete Goal (Xóa mục tiêu)
    fun deleteGoal(goalId: String) {
        viewModelScope.launch {
            deleteGoalUseCase(goalId)
        }
    }
}