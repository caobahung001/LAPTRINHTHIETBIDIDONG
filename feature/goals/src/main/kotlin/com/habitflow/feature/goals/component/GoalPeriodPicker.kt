package com.habitflow.feature.goals.component

import androidx.compose.foundation.layout.Row
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.habitflow.core.model.enum.GoalPeriodType

@Composable
fun GoalPeriodPicker(
    selectedPeriod: GoalPeriodType,
    onPeriodSelected: (GoalPeriodType) -> Unit
) {
    Row {
        GoalPeriodType.values().forEach { period ->
            FilterChip(
                selected = period == selectedPeriod,
                onClick = { onPeriodSelected(period) },
                label = { Text(period.name) }
            )
        }
    }
}