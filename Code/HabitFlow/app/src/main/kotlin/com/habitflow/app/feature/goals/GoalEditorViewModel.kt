package com.habitflow.app.feature.goals

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.habitflow.app.GoalDao
import com.habitflow.app.GoalEntity
import com.habitflow.app.core.data.repository.OfflineGoalRepository
import com.habitflow.app.core.domain.repository.GoalRepository
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.util.UUID

class GoalEditorViewModel(
    private val repository: GoalRepository
) : ViewModel() {

    constructor(goalDao: GoalDao) : this(
        OfflineGoalRepository(goalDao)
    )

    var uiState by mutableStateOf(GoalEditorUiState())
        private set

    fun onTitleChange(newTitle: String) {
        uiState = uiState.copy(title = newTitle)
    }

    fun onTargetValueChange(newValue: String) {
        uiState = uiState.copy(targetValue = newValue)
    }

    fun saveGoal(onSuccess: () -> Unit) {
        viewModelScope.launch {
            val entity = GoalEntity(
                id = uiState.id ?: UUID.randomUUID().toString(),
                name = uiState.title,
                targetValue = uiState.targetValue.toDoubleOrNull() ?: 1.0,
                metricType = uiState.metricType,
                startEpochDay = LocalDate.now().toEpochDay(),
                endEpochDay = LocalDate.now().plusDays(7).toEpochDay()
            )
            repository.saveGoal(entity)
            resetForm()
            onSuccess()
        }
    }

    fun resetForm() {
        uiState = GoalEditorUiState()
    }
}