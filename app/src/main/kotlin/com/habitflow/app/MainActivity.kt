package com.habitflow.app

import android.Manifest
import androidx.compose.runtime.saveable.rememberSaveable
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { HabitFlowApp(viewModel) }
    }
}

@Composable
fun HabitFlowApp(viewModel: MainViewModel) {
    MaterialTheme {
        var tab by rememberSaveable { mutableIntStateOf(0) }
        val labels = listOf("Hôm nay", "Thói quen", "Mục tiêu", "Thống kê", "Cài đặt")
        Scaffold(bottomBar = {
            NavigationBar {
                labels.forEachIndexed { index, label ->
                    NavigationBarItem(selected = tab == index, onClick = { tab = index },
                        icon = { Text(label.take(1)) }, label = { Text(label) })
                }
            }
        }) { padding ->
            Box(Modifier.padding(padding)) {
                when (tab) {
                    0 -> TodayScreen(viewModel)
                    1 -> HabitsScreen(viewModel)
                    2 -> GoalsScreen(viewModel)
                    3 -> StatisticsScreen(viewModel)
                    else -> SettingsScreen(viewModel)
                }
            }
        }
    }
}

@Composable
private fun TodayScreen(vm: MainViewModel) {
    val habits by vm.habits.collectAsStateWithLifecycle()
    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text("Hôm nay", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(12.dp))
        if (habits.isEmpty()) Text("Chưa có thói quen. Mở tab Thói quen để tạo mới.")
        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(habits, key = { it.id }) { habit ->
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp)) {
                        Text(habit.name, style = MaterialTheme.typography.titleMedium)
                        if (habit.description.isNotBlank()) Text(habit.description)
                        Spacer(Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(onClick = { vm.mark(habit.id, OccurrenceStatus.COMPLETED) }) { Text("Xong") }
                            OutlinedButton(onClick = { vm.mark(habit.id, OccurrenceStatus.SKIPPED) }) { Text("Bỏ qua") }
                            OutlinedButton(onClick = { vm.mark(habit.id, OccurrenceStatus.MISSED) }) { Text("Bỏ lỡ") }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HabitsScreen(vm: MainViewModel) {
    val habits by vm.habits.collectAsStateWithLifecycle()
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text("Thói quen", style = MaterialTheme.typography.headlineMedium)
        OutlinedTextField(name, { name = it }, Modifier.fillMaxWidth(), label = { Text("Tên thói quen") })
        OutlinedTextField(description, { description = it }, Modifier.fillMaxWidth(), label = { Text("Mô tả") })
        Button(onClick = { if (name.isNotBlank()) { vm.addHabit(name, description); name = ""; description = "" } }, Modifier.padding(vertical = 8.dp)) { Text("Thêm thói quen") }
        LazyColumn {
            items(habits, key = { it.id }) { habit ->
                ListItem(headlineContent = { Text(habit.name) }, supportingContent = { Text(habit.description) },
                    trailingContent = { TextButton(onClick = { vm.archiveHabit(habit.id) }) { Text("Lưu trữ") } })
                HorizontalDivider()
            }
        }
    }
}

@Composable
private fun GoalsScreen(vm: MainViewModel) {
    val goals by vm.goals.collectAsStateWithLifecycle()
    var name by remember { mutableStateOf("") }
    var target by remember { mutableStateOf("10") }
    var type by remember { mutableStateOf(GoalMetricType.OCCURRENCE_COUNT) }
    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text("Mục tiêu", style = MaterialTheme.typography.headlineMedium)
        OutlinedTextField(name, { name = it }, Modifier.fillMaxWidth(), label = { Text("Tên mục tiêu") })
        OutlinedTextField(target, { target = it }, Modifier.fillMaxWidth(), label = { Text("Giá trị mục tiêu") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal))
        Row {
            FilterChip(selected = type == GoalMetricType.OCCURRENCE_COUNT, onClick = { type = GoalMetricType.OCCURRENCE_COUNT }, label = { Text("Theo số lần") })
            Spacer(Modifier.width(8.dp))
            FilterChip(selected = type == GoalMetricType.ACCUMULATED_VALUE, onClick = { type = GoalMetricType.ACCUMULATED_VALUE }, label = { Text("Theo giá trị") })
        }
        Button(onClick = { target.toDoubleOrNull()?.let { if (name.isNotBlank() && it > 0) { vm.addGoal(name, it, type); name = "" } } }, Modifier.padding(vertical = 8.dp)) { Text("Tạo mục tiêu") }
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(goals, key = { it.id }) { goal ->
                Card(Modifier.fillMaxWidth()) { Column(Modifier.padding(16.dp)) {
                    Text(goal.name, style = MaterialTheme.typography.titleMedium)
                    LinearProgressIndicator(progress = { (goal.currentValue / goal.targetValue).toFloat().coerceIn(0f, 1f) }, modifier = Modifier.fillMaxWidth())
                    Text("${goal.currentValue} / ${goal.targetValue} ${goal.unit}")
                    TextButton(onClick = { vm.addGoalProgress(goal, 1.0) }) { Text("+1 tiến độ") }
                } }
            }
        }
    }
}
@Composable
private fun StatisticsScreen(vm: MainViewModel) {
    val stats by vm.stats.collectAsStateWithLifecycle()
    Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Thống kê", style = MaterialTheme.typography.headlineMedium)
        Text("Tỷ lệ hoàn thành: ${"%.1f".format(stats.completionRate)}%")
        LinearProgressIndicator(progress = { (stats.completionRate / 100.0).toFloat() }, modifier = Modifier.fillMaxWidth())
        Text("Hoàn thành: ${stats.completed}")
        Text("Bỏ lỡ: ${stats.missed}")
        Text("Bỏ qua: ${stats.skipped}")
        Text("Chuỗi hiện tại: ${stats.currentStreak}")
        Text("Chuỗi dài nhất: ${stats.longestStreak}")
    }
}

@Composable
private fun SettingsScreen(vm: MainViewModel) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var message by remember { mutableStateOf("") }
    val createDocument = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri ->
        if (uri != null) scope.launch {
            runCatching { context.contentResolver.openOutputStream(uri)?.use { it.write(vm.exportJson().toByteArray()) } }
                .onSuccess { message = "Đã xuất dữ liệu" }.onFailure { message = it.message ?: "Xuất thất bại" }
        }
    }
    val openDocument = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) scope.launch {
            runCatching { val text = context.contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() } ?: error("Không đọc được tệp"); vm.restoreJson(text) }
                .onSuccess { message = "Đã khôi phục dữ liệu" }.onFailure { message = it.message ?: "Khôi phục thất bại" }
        }
    }
    val notificationPermission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted -> message = if (granted) "Đã cấp quyền thông báo" else "Chưa cấp quyền thông báo" }
    Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Cài đặt", style = MaterialTheme.typography.headlineMedium)
        Button(onClick = { createDocument.launch("habitflow-backup.json") }) { Text("Xuất JSON") }
        OutlinedButton(onClick = { openDocument.launch(arrayOf("application/json", "text/plain")) }) { Text("Khôi phục JSON") }
        OutlinedButton(onClick = { if (Build.VERSION.SDK_INT >= 33) notificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS) else message = "Thiết bị không cần cấp quyền runtime" }) { Text("Cấp quyền thông báo") }
        if (message.isNotBlank()) Text(message)
    }
}
