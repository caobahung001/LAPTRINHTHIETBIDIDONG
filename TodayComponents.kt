package com.habitflow.feature.today

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.habitflow.core.domain.model.OccurrenceStatus
import com.habitflow.core.domain.model.TodayHabitItem

@Composable
fun HabitItemCard(
    item: TodayHabitItem,
    onAction: (TodayAction) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth().padding(vertical = 6.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = item.title, style = MaterialTheme.typography.titleMedium)
                Text(
                    text = "Streak: ${item.currentStreak} ngay 🔥 (Ky luc: ${item.longestStreak})",
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                val currentStatus = item.currentOccurrence?.status ?: OccurrenceStatus.NONE

                if (currentStatus == OccurrenceStatus.NONE) {
                    Button(onClick = { onAction(TodayAction.Complete(item.habitId)) }) {
                        Text("Xong")
                    }
                    OutlinedButton(onClick = { onAction(TodayAction.Skip(item.habitId)) }) {
                        Text("Bo qua")
                    }
                } else {
                    TextButton(onClick = { onAction(TodayAction.Undo(item.habitId)) }) {
                        Text("Hoan tac (${currentStatus.name})")
                    }
                }
            }
        }
    }
}
