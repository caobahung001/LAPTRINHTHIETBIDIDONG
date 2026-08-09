package com.habitflow.feature.goals

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable

fun NavGraphBuilder.goalsNavigation() {
    composable(route = "goals_list") {
        GoalListScreen()
    }
    composable(route = "goals_detail") {
        GoalDetailScreen()
    }
    composable(route = "goals_editor") {
        GoalEditorScreen()
    }
}