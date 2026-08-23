package com.habitflow.feature.goals

import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument

const val GOALS_ROUTE = "goals_route"
const val GOAL_EDITOR_ROUTE = "goal_editor_route"

fun NavController.navigateToGoals(navOptions: NavOptions? = null) {
    this.navigate(GOALS_ROUTE, navOptions)
}

fun NavController.navigateToGoalEditor(goalId: String? = null) {
    val route = if (goalId != null) {
        "$GOAL_EDITOR_ROUTE?goalId=$goalId"
    } else {
        GOAL_EDITOR_ROUTE
    }
    this.navigate(route)
}

fun NavGraphBuilder.goalsScreen(
    onGoalClick: (String) -> Unit,
    onAddGoalClick: () -> Unit
) {
    composable(route = GOALS_ROUTE) {
        val viewModel: GoalListViewModel = hiltViewModel()
        val uiState by viewModel.uiState.collectAsStateWithLifecycle()

        GoalListScreen(
            uiState = uiState,
            onGoalClick = onGoalClick,
            onAddGoalClick = onAddGoalClick,
            onIncrementProgress = { goalId ->
                onGoalClick(goalId)
            },
            onDeleteGoal = { goalId ->
                viewModel.deleteGoal(goalId)
            }
        )
    }
}

fun NavGraphBuilder.goalEditorScreen(
    onBackClick: () -> Unit
) {
    composable(
        route = "$GOAL_EDITOR_ROUTE?goalId={goalId}",
        arguments = listOf(
            navArgument("goalId") {
                type = NavType.StringType
                nullable = true
                defaultValue = null
            }
        )
    ) {
        val viewModel: GoalEditorViewModel = hiltViewModel()
        val uiState by viewModel.uiState.collectAsStateWithLifecycle()

        GoalEditorScreen(
            uiState = uiState,
            onNameChanged = viewModel::onNameChanged,
            onTargetValueChanged = viewModel::onTargetValueChanged,
            onUnitChanged = viewModel::onUnitChanged,
            onPeriodSelected = viewModel::onPeriodSelected,
            onSaveClick = viewModel::saveGoal,
            onBackClick = onBackClick
        )
    }
}