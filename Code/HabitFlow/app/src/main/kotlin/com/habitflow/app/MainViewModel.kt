package com.habitflow.app

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.glance.appwidget.updateAll
import com.habitflow.app.core.widget.HabitWidget
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = (application as HabitFlowApplication).repository

    private val _testDateOffset = MutableStateFlow(0L)
    val testDateOffset = _testDateOffset.asStateFlow()

    fun advanceTestDay() {
        _testDateOffset.value += 1
        updateWidget()
    }

    private fun updateWidget() = viewModelScope.launch {
        HabitWidget().updateAll(getApplication())
    }

    val habits = repository.habits.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    val goals = repository.goals.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    val occurrences = repository.occurrences.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    val userStats = repository.userStats.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), UserStatsEntity())

    val stats = combine(repository.occurrences, repository.habits, testDateOffset) { occurrences, habits, offset ->
        val todayEpochDay = LocalDate.now().plusDays(offset).toEpochDay()
        HabitStatisticsCalculator.calculate(occurrences, todayEpochDay)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HabitStats())

    fun addHabit(name: String, description: String = "", scheduledDays: String = "", scheduledTime: String? = null) = viewModelScope.launch { 
        repository.addHabit(name, description, scheduledDays, scheduledTime)
        updateWidget()
    }
    fun archiveHabit(id: String) = viewModelScope.launch { 
        repository.archiveHabit(id)
        updateWidget()
    }
    fun deleteHabit(id: String) = viewModelScope.launch { 
        repository.deleteHabit(id)
        updateWidget()
    }
    
    fun mark(id: String, status: OccurrenceStatus) = viewModelScope.launch { 
        val todayEpochDay = LocalDate.now().plusDays(_testDateOffset.value).toEpochDay()
        repository.mark(id, status, dateEpochDay = todayEpochDay)
        if (status == OccurrenceStatus.COMPLETED) {
            val habitStats = stats.value
            GamificationManager.processCompletion(repository, habitStats.currentStreak, todayEpochDay)
        }
        updateWidget()
    }

    fun useStreakFreeze(habitId: String) = viewModelScope.launch {
        val todayEpochDay = LocalDate.now().plusDays(_testDateOffset.value).toEpochDay()
        if (GamificationManager.useStreakFreeze(repository)) {
            repository.mark(habitId, OccurrenceStatus.FROZEN, dateEpochDay = todayEpochDay)
            updateWidget()
        }
    }

    fun useSkip(habitId: String) = viewModelScope.launch {
        val todayEpochDay = LocalDate.now().plusDays(_testDateOffset.value).toEpochDay()
        if (GamificationManager.useSkipCard(repository)) {
            repository.mark(habitId, OccurrenceStatus.SKIPPED, dateEpochDay = todayEpochDay)
            updateWidget()
        }
    }

    fun skipLevel() = viewModelScope.launch {
        GamificationManager.skipLevel(repository)
        updateWidget()
    }

    fun unmark(id: String, dateEpochDay: Long) = viewModelScope.launch { 
        repository.unmark(id, dateEpochDay)
        updateWidget()
    }
    fun addGoal(name: String, target: Double, type: GoalMetricType) = viewModelScope.launch { repository.addGoal(name, target, type) }
    fun addGoalProgress(goal: GoalEntity, value: Double) = viewModelScope.launch { repository.addGoalProgress(goal, value) }
    suspend fun exportJson(): String = repository.exportJson()
    suspend fun restoreJson(text: String) = repository.restoreJson(text)
}
