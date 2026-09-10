package com.habitflow.app.feature.goals.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.habitflow.app.GoalPeriodType

@Composable
fun GoalPeriodPicker(
    selectedPeriod: GoalPeriodType,
    onPeriodSelected: (GoalPeriodType) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = "Chu kỳ mục tiêu",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            GoalPeriodType.entries.forEach { period ->
                val label = when (period) {
                    GoalPeriodType.WEEKLY -> "Hàng tuần"
                    GoalPeriodType.MONTHLY -> "Hàng tháng"
                    GoalPeriodType.CUSTOM -> "Tùy chỉnh"
                }

                FilterChip(
                    selected = (period == selectedPeriod),
                    onClick = { onPeriodSelected(period) },
                    label = { Text(label) }
                )
            }
        }
    }
}