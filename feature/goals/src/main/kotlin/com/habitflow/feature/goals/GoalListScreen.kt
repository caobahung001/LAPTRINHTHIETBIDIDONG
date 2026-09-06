package com.habitflow.feature.goals

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.habitflow.core.model.ProgressSummary

@OptIn(ExperimentalMaterial3Api::class)
@Suppress("SpellCheckingInspection")
@Composable
fun GoalListScreen(
    uiState: GoalListUiState,
    onGoalClick: (String) -> Unit,
    onAddGoalClick: () -> Unit,
    modifier: Modifier = Modifier,
    onIncrementProgress: (String, Double) -> Unit = { _, _ -> },
    onDeleteGoal: (String) -> Unit = {}
) {
    var selectedGoalForInput by remember { mutableStateOf<String?>(null) }
    var inputValue by remember { mutableStateOf("") }
    var showHistoryDialog by remember { mutableStateOf(false) }

    val currentEpochDay = System.currentTimeMillis() / (1000 * 60 * 60 * 24)

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("Mục tiêu của tôi") },
                actions = {
                    IconButton(onClick = { showHistoryDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.DateRange,
                            contentDescription = "Lịch sử mục tiêu"
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddGoalClick) {
                Icon(Icons.Default.Add, contentDescription = "Thêm mục tiêu")
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (uiState) {
                is GoalListUiState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                is GoalListUiState.Success -> {
                    val activeGoals = uiState.items.filter { item ->
                        val isCompleted = item.currentProgress >= item.targetValue
                        !isCompleted
                    }

                    if (activeGoals.isEmpty()) {
                        Text(
                            text = "Chưa có mục tiêu nào đang thực hiện",
                            modifier = Modifier.align(Alignment.Center)
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(
                                items = activeGoals,
                                key = { item -> item.goalId }
                            ) { item ->
                                GoalItemCard(
                                    item = item,
                                    onGoalClick = onGoalClick,
                                    onDeleteGoal = onDeleteGoal,
                                    onIncrementClick = { goalId ->
                                        val isValueType = item.unit.lowercase() != "lần"
                                        if (isValueType) {
                                            selectedGoalForInput = goalId
                                            inputValue = ""
                                        } else {
                                            onIncrementProgress(goalId, 1.0)
                                        }
                                    }
                                )
                            }
                        }
                    }

                    if (showHistoryDialog) {
                        val historyGoals = uiState.items.filter { item ->
                            item.currentProgress >= item.targetValue
                        }

                        AlertDialog(
                            onDismissRequest = { showHistoryDialog = false },
                            title = { Text("Lịch sử mục tiêu") },
                            text = {
                                if (historyGoals.isEmpty()) {
                                    Text("Chưa có mục tiêu nào hoàn thành.")
                                } else {
                                    LazyColumn(
                                        verticalArrangement = Arrangement.spacedBy(8.dp),
                                        modifier = Modifier.height(350.dp)
                                    ) {
                                        items(historyGoals) { item ->
                                            GoalItemCard(
                                                item = item,
                                                onGoalClick = {},
                                                onDeleteGoal = onDeleteGoal,
                                                onIncrementClick = {}
                                            )
                                        }
                                    }
                                }
                            },
                            confirmButton = {
                                TextButton(onClick = { showHistoryDialog = false }) {
                                    Text("Đóng")
                                }
                            }
                        )
                    }
                }
                is GoalListUiState.Error -> {
                    Text(
                        text = uiState.message,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            }
        }
    }

    selectedGoalForInput?.let { goalId ->
        AlertDialog(
            onDismissRequest = { selectedGoalForInput = null },
            title = { Text("Cộng dồn tiến độ") },
            text = {
                OutlinedTextField(
                    value = inputValue,
                    onValueChange = { inputValue = it },
                    label = { Text("Nhập giá trị (ví dụ: 2, 5...)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        val addedValue = inputValue.toDoubleOrNull() ?: 0.0
                        if (addedValue > 0) {
                            onIncrementProgress(goalId, addedValue)
                        }
                        selectedGoalForInput = null
                    }
                ) {
                    Text("Xác nhận")
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedGoalForInput = null }) {
                    Text("Hủy")
                }
            }
        )
    }
}

@Composable
private fun GoalItemCard(
    item: ProgressSummary,
    onGoalClick: (String) -> Unit,
    onDeleteGoal: (String) -> Unit,
    onIncrementClick: (String) -> Unit
) {
    val progressFraction = (item.percentage / 100.0).toFloat().coerceIn(0f, 1f)
    val isCompleted = item.currentProgress >= item.targetValue
    val isValueType = item.unit.lowercase() != "lần"

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onGoalClick(item.goalId) },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = item.goalName,
                    style = MaterialTheme.typography.titleMedium
                )
                IconButton(onClick = { onDeleteGoal(item.goalId) }) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Xóa mục tiêu",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            LinearProgressIndicator(
                progress = { progressFraction },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(MaterialTheme.shapes.small)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Tiến độ: ${item.percentage.toInt()}% (${item.currentProgress.toInt()}/${item.targetValue.toInt()} ${item.unit})",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                if (isCompleted) {
                    FilterChip(
                        selected = true,
                        onClick = {},
                        label = { Text("Đã hoàn thành") },
                        leadingIcon = { Icon(Icons.Default.Check, contentDescription = null) }
                    )
                } else {
                    Button(onClick = { onIncrementClick(item.goalId) }) {
                        Text(if (isValueType) "Cộng thêm" else "+1")
                    }
                }
            }
        }
    }
}