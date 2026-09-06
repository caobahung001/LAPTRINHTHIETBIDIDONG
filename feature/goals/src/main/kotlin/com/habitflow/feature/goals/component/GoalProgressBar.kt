package com.habitflow.feature.goals.component

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun GoalProgressBar(currentValue: Double, targetValue: Double, unit: String) {
    val percentage = if (targetValue > 0) ((currentValue / targetValue) * 100).coerceAtMost(100.0) else 0.0
    Column(modifier = Modifier.fillMaxWidth()) {
        LinearProgressIndicator(
            progress = { (percentage / 100).toFloat() },
            modifier = Modifier.fillMaxWidth().height(8.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = "$currentValue / $targetValue $unit (${percentage.toInt()}%)", style = MaterialTheme.typography.bodyMedium)
    }
}