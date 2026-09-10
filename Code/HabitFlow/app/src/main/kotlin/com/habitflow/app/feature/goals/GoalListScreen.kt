package com.habitflow.app.feature.goals

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.habitflow.app.GoalEntity
import com.habitflow.app.core.model.toExternalModel

@Composable
fun GoalListScreen(
    goals: List<GoalEntity>,
    onGoalClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(goals, key = { it.id }) { goalEntity ->

            val domainGoal = goalEntity.toExternalModel()

            GoalItemCard(
                goal = domainGoal,
                onClick = { onGoalClick(domainGoal.id) }
            )
        }
    }
}

@Composable
fun GoalItemCard(
    goal: com.habitflow.app.core.model.Goal,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = goal.title,
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Tiến độ: ${goal.currentValue} / ${goal.targetValue}",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}