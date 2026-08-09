package com.habitflow.feature.today

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.habitflow.core.domain.calculator.CompletionRateCalculator
import com.habitflow.core.domain.usecase.occurrence.*
import com.habitflow.core.domain.repository.OccurrenceRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate

class TodayViewModel(
    private val occurrenceRepository: OccurrenceRepository,
    private val completeHabitUseCase: CompleteHabitUseCase,
    private val skipHabitUseCase: SkipHabitUseCase,
    private val undoHabitUseCase: UndoHabitUseCase,
    private val addValueHabitUseCase: AddValueHabitUseCase,
    private val completionRateCalculator: CompletionRateCalculator
) : ViewModel() {

    private val _uiState = MutableStateFlow(TodayUiState(selectedDateISO = LocalDate.now().toString()))
    val uiState: StateFlow<TodayUiState> = _uiState.asStateFlow()

    init {
        loadTodayHabits()
    }

    fun onAction(action: TodayAction) {
        viewModelScope.launch {
            when (action) {
                is TodayAction.Complete -> completeHabitUseCase(action.habitId)
                is TodayAction.Skip -> skipHabitUseCase(action.habitId)
                is TodayAction.Undo -> undoHabitUseCase(action.habitId)
                is TodayAction.AddValue -> addValueHabitUseCase(
                    action.habitId, action.value, action.targetValue
                )
                is TodayAction.SelectDate -> {
                    _uiState.update { it.copy(selectedDateISO = action.dateISO) }
                    loadTodayHabits()
                }
            }
        }
    }

    private fun loadTodayHabits() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            occurrenceRepository.getOccurrencesByDate(_uiState.value.selectedDateISO)
                .collect { occurrences ->
                    val completedCount = occurrences.count { it.status.name == "COMPLETED" }
                    val totalCount = occurrences.size
                    val rate = completionRateCalculator.calculateRate(completedCount, totalCount)

                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            completionRate = rate
                        )
                    }
                }
        }
    }
}
