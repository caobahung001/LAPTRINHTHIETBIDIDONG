package com.habitflow.app.feature.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.habitflow.app.ReminderEntity
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReminderEditorDialog(
    habitId: String,
    habitName: String,
    existingReminder: ReminderEntity? = null,
    onDismiss: () -> Unit,
    onSave: (ReminderEntity) -> Unit,
    onDelete: ((String) -> Unit)? = null
) {
    var hour by remember { mutableIntStateOf(existingReminder?.hour ?: 8) }
    var minute by remember { mutableIntStateOf(existingReminder?.minute ?: 0) }
    var isEnabled by remember { mutableStateOf(existingReminder?.enabled ?: true) }

    val timePickerState = rememberTimePickerState(
        initialHour = hour,
        initialMinute = minute,
        is24Hour = true
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (existingReminder == null) "Thêm nhắc nhở cho: $habitName" else "Sửa nhắc nhở: $habitName",
                style = MaterialTheme.typography.titleMedium
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 1. Time Picker Material 3
                TimeInput(state = timePickerState)

                // 2. Switch Bật / Tắt
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Kích hoạt nhắc nhở", style = MaterialTheme.typography.bodyMedium)
                    Switch(
                        checked = isEnabled,
                        onCheckedChange = { isEnabled = it }
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val reminder = ReminderEntity(
                        id = existingReminder?.id ?: UUID.randomUUID().toString(),
                        habitId = habitId,
                        hour = timePickerState.hour,
                        minute = timePickerState.minute,
                        enabled = isEnabled,
                        requestCode = existingReminder?.requestCode ?: (System.currentTimeMillis() % 100000).toInt()
                    )
                    onSave(reminder)
                    onDismiss()
                }
            ) {
                Text("Lưu nhắc nhở")
            }
        },
        dismissButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (existingReminder != null && onDelete != null) {
                    TextButton(
                        onClick = {
                            onDelete(existingReminder.id)
                            onDismiss()
                        },
                        colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text("Xóa")
                    }
                }
                TextButton(onClick = onDismiss) {
                    Text("Hủy")
                }
            }
        }
    )
}
