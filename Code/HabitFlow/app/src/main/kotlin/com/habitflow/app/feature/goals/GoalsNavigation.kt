package com.habitflow.app.feature.goals

import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable

const val GOALS_ROUTE = "goals_route"

fun NavController.navigateToGoals() {
    this.navigate(GOALS_ROUTE)
}

fun NavGraphBuilder.goalsScreen(
    onAddGoalClick: () -> Unit,
    onGoalClick: (String) -> Unit,
    viewModel: GoalListViewModel
) {
    composable(route = GOALS_ROUTE) {
        val uiState by viewModel.uiState.collectAsStateWithLifecycle()

        GoalListScreen(
            goals = uiState.goals,
            onGoalClick = onGoalClick
        )
    }
}