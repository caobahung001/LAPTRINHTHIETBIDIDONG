package com.habitflow.app

import java.time.LocalDate
import android.Manifest
import androidx.compose.runtime.saveable.rememberSaveable
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.BorderStroke
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.clickable
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import java.time.format.DateTimeFormatter
import java.time.YearMonth
import java.time.DayOfWeek
import java.util.Locale
import kotlinx.coroutines.launch
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import com.habitflow.app.core.datastore.UserPreferencesDataSource
import com.habitflow.app.feature.settings.SettingsViewModel
import androidx.compose.foundation.shape.RoundedCornerShape

class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent { HabitFlowApp(viewModel) }
    }
}

@Composable
fun HabitFlowApp(viewModel: MainViewModel) {
    val context = LocalContext.current
    val settingsViewModel = remember {
        SettingsViewModel(UserPreferencesDataSource(context.applicationContext))
    }
    val settingsUiState by settingsViewModel.uiState.collectAsStateWithLifecycle()
    
    var hasShownGreeting by remember { mutableStateOf(false) }

    LaunchedEffect(settingsUiState) {
        if (!hasShownGreeting && settingsUiState is com.habitflow.app.feature.settings.SettingsUiState.Success) {
            val prefs = (settingsUiState as com.habitflow.app.feature.settings.SettingsUiState.Success).userPreferences
            if (prefs.greetingMessage.isNotBlank()) {
                android.widget.Toast.makeText(context, prefs.greetingMessage, android.widget.Toast.LENGTH_SHORT).show()
                hasShownGreeting = true
            }
        }
    }

    val isDarkTheme = when (val state = settingsUiState) {
        is com.habitflow.app.feature.settings.SettingsUiState.Success -> {
            when (state.userPreferences.appTheme) {
                com.habitflow.app.core.datastore.AppTheme.DARK -> true
                com.habitflow.app.core.datastore.AppTheme.LIGHT -> false
                com.habitflow.app.core.datastore.AppTheme.SYSTEM -> androidx.compose.foundation.isSystemInDarkTheme()
            }
        }
        else -> androidx.compose.foundation.isSystemInDarkTheme()
    }

    val colorScheme = if (isDarkTheme) {
        darkColorScheme(
            primary = Color(0xFF39FF14), // Neon Green
            onPrimary = Color.Black,
            secondary = Color(0xFF2C2C2E), // Dark Gray Block
            onSecondary = Color.White,
            surface = Color(0xFF121212),
            onSurface = Color.White
        )
    } else {
        lightColorScheme()
    }

    MaterialTheme(colorScheme = colorScheme) {
        var tab by rememberSaveable { mutableIntStateOf(0) }
        val labels = listOf("Hôm nay", "Thói quen", "Mục tiêu", "Thống kê", "Cài đặt")
        val colors = listOf(
            Color(0xFF2196F3), // Blue
            Color(0xFF4CAF50), // Green
            Color(0xFFFF9800), // Orange
            Color(0xFF9C27B0), // Purple
            Color(0xFF009688)  // Teal
        )
        Scaffold(
            modifier = Modifier.fillMaxSize().statusBarsPadding(),
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            bottomBar = {
                Surface(
                    modifier = Modifier.navigationBarsPadding(),
                    shadowElevation = 8.dp,
                    tonalElevation = 8.dp,
                    color = MaterialTheme.colorScheme.surface
                ) {
                Row(
                    modifier = Modifier.fillMaxWidth().height(70.dp)
                ) {
                    labels.forEachIndexed { index, label ->
                        val isSelected = tab == index
                        val baseColor = colors[index]
                        
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .background(if (isSelected) baseColor else Color.Transparent)
                                .clickable { tab = index }
                                .padding(4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = label.take(1),
                                    style = MaterialTheme.typography.titleMedium,
                                    color = if (isSelected) Color.White else baseColor
                                )
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (isSelected) Color.White else baseColor,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
            }
        }) { padding ->
            Box(Modifier.padding(padding)) {
                when (tab) {
                    0 -> TodayScreen(viewModel, onNavigateToHabits = { tab = 1 })
                    1 -> HabitsScreen(viewModel)
                    2 -> GoalsScreen(viewModel)
                    3 -> StatisticsScreen(viewModel)
                    else -> com.habitflow.app.feature.settings.SettingsScreen(
                        viewModel = settingsViewModel,
                        mainViewModel = viewModel
                    )
                }
            }
        }
    }
}

@Composable
private fun TodayScreen(vm: MainViewModel, onNavigateToHabits: () -> Unit) {
    val habits by vm.habits.collectAsStateWithLifecycle()
    val occurrences by vm.occurrences.collectAsStateWithLifecycle()
    val userStats by vm.userStats.collectAsStateWithLifecycle()
    val testOffset by vm.testDateOffset.collectAsStateWithLifecycle()
    var showLevelDetail by remember { mutableStateOf(false) }

    if (showLevelDetail && userStats != null) {
        LevelDetailDialog(
            stats = userStats!!,
            habits = habits,
            onSkipLevel = { vm.skipLevel() },
            onDismiss = { showLevelDetail = false }
        )
    }

    // Lấy ngày hôm nay và lọc thói quen
    val today = remember(testOffset) { LocalDate.now().plusDays(testOffset) }
    val todayEpochDay = remember(today) { today.toEpochDay() }
    val currentDayOfWeek = remember(today) { today.dayOfWeek.value }

    val dateFormatter = remember { DateTimeFormatter.ofPattern("EEEE, dd/MM/yyyy", Locale("vi")) }
    val dateString = remember(today) { today.format(dateFormatter) }.replaceFirstChar { it.uppercase() }

    val tomorrow = remember(today) { today.plusDays(1) }
    val tomorrowDayOfWeek = remember(tomorrow) { tomorrow.dayOfWeek.value }

    val filteredHabits = remember(habits, currentDayOfWeek) {
        habits.filter { habit ->
            habit.scheduledDays.isEmpty() || 
            habit.scheduledDays.split(",").contains(currentDayOfWeek.toString())
        }
    }

    val tomorrowHabits = remember(habits, tomorrowDayOfWeek) {
        habits.filter { habit ->
            habit.scheduledDays.isEmpty() || 
            habit.scheduledDays.split(",").contains(tomorrowDayOfWeek.toString())
        }
    }

    // Optimization: Pre-filter today's occurrences
    val todayOccurrences = remember(occurrences, todayEpochDay) {
        occurrences.filter { it.scheduledEpochDay == todayEpochDay }.associateBy { it.habitId }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // XP and Level Header
        userStats?.let { stats ->
            Card(
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp).clickable { showLevelDetail = true },
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier.size(40.dp).background(MaterialTheme.colorScheme.primary, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(stats.level.toString(), color = MaterialTheme.colorScheme.onPrimary, style = MaterialTheme.typography.titleMedium)
                    }
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Cấp độ ${stats.level}", style = MaterialTheme.typography.labelLarge)
                            Text("${stats.xp} XP", style = MaterialTheme.typography.labelMedium)
                        }
                        val xpForNext = GamificationManager.getXpForNextLevel(stats.level)
                        val xpForCurrent = GamificationManager.getXpForNextLevel(stats.level - 1)
                        val progress = (stats.xp - xpForCurrent).toFloat() / (xpForNext - xpForCurrent).toFloat()
                        LinearProgressIndicator(
                            progress = { progress.coerceIn(0f, 1f) },
                            modifier = Modifier.fillMaxWidth().height(8.dp),
                            strokeCap = StrokeCap.Round
                        )
                    }
                    if (stats.streakFreezes > 0) {
                        Spacer(Modifier.width(12.dp))
                        Text("❄️ ${stats.streakFreezes}", style = MaterialTheme.typography.titleMedium)
                    }
                }
            }
        }

        Text(dateString, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.secondary)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Hôm nay", style = MaterialTheme.typography.headlineMedium)
            Spacer(Modifier.weight(1f))
            TextButton(onClick = { vm.advanceTestDay() }) {
                Text("next day(test)", color = MaterialTheme.colorScheme.error)
            }
        }
        Spacer(Modifier.height(12.dp))

        if (filteredHabits.isEmpty()) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text("Không có thói quen nào cho hôm nay.", style = MaterialTheme.typography.bodyLarge)
                Button(onClick = onNavigateToHabits) {
                    Text("Thêm thói quen ngay")
                }
            }
        }

        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(filteredHabits, key = { it.id }) { habit ->
                val todayOccurrence = todayOccurrences[habit.id]

                val cardColor = when (todayOccurrence?.status) {
                    OccurrenceStatus.COMPLETED -> Color(0xFFC8E6C9)
                    OccurrenceStatus.SKIPPED -> Color(0xFFE1BEE7)
                    OccurrenceStatus.FROZEN -> Color(0xFFBBDEFB)
                    OccurrenceStatus.MISSED -> Color(0xFFFFCDD2)
                    else -> MaterialTheme.colorScheme.surfaceVariant
                }

                val cardContentColor = if (todayOccurrence != null) Color(0xFF1C1B1F) else contentColorFor(cardColor)

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = cardColor,
                        contentColor = cardContentColor
                    )
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(habit.name, style = MaterialTheme.typography.titleMedium)
                            if (habit.scheduledTime != null) {
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    habit.scheduledTime,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (todayOccurrence != null) Color(0xFF49454F) else MaterialTheme.colorScheme.secondary
                                )
                            }
                        }
                        if (habit.description.isNotBlank()) {
                            Text(habit.description)
                        }

                        // Hiển thị trạng thái nếu hôm nay đã bấm nút
                        if (todayOccurrence != null) {
                            val statusText = when (todayOccurrence.status) {
                                OccurrenceStatus.COMPLETED -> "Đã hoàn thành"
                                OccurrenceStatus.SKIPPED -> "Đã bỏ qua"
                                OccurrenceStatus.FROZEN -> "Đã đóng băng chuỗi ❄️"
                                OccurrenceStatus.MISSED -> "Đã bỏ lỡ"
                                OccurrenceStatus.PENDING -> "Đang chờ"
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = statusText,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = if (todayOccurrence.status == OccurrenceStatus.MISSED) Color.White else Color(0xFF1B5E20)
                                )
                                TextButton(
                                    onClick = { vm.unmark(habit.id, todayEpochDay) },
                                    colors = ButtonDefaults.textButtonColors(
                                        containerColor = MaterialTheme.colorScheme.secondary,
                                        contentColor = MaterialTheme.colorScheme.onSecondary
                                    ),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text("Thay đổi")
                                }
                            }
                        } else {
                            Spacer(Modifier.height(8.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Button(
                                    onClick = { vm.mark(habit.id, OccurrenceStatus.COMPLETED) },
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text("Xong")
                                }
                                OutlinedButton(
                                    onClick = { vm.mark(habit.id, OccurrenceStatus.SKIPPED) },
                                    shape = RoundedCornerShape(8.dp),
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                                ) {
                                    Text("Bỏ qua")
                                }
                                if ((userStats?.streakFreezes ?: 0) > 0) {
                                    OutlinedButton(
                                        onClick = { vm.useStreakFreeze(habit.id) },
                                        shape = RoundedCornerShape(8.dp),
                                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                                    ) {
                                        Text("❄️")
                                    }
                                }
                                if ((userStats?.skipsAvailable ?: 0) > 0) {
                                    OutlinedButton(
                                        onClick = { vm.useSkip(habit.id) },
                                        shape = RoundedCornerShape(8.dp),
                                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                                    ) {
                                        Text("⏭️")
                                    }
                                }
                            }
                        }
                    }
                }
            }

            if (tomorrowHabits.isNotEmpty()) {
                item {
                    Spacer(Modifier.height(16.dp))
                    Text("Ngày mai", style = MaterialTheme.typography.titleMedium)
                }
                items(tomorrowHabits, key = { "tomorrow_${it.id}" }) { habit ->
                    OutlinedCard(Modifier.fillMaxWidth()) {
                        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text(habit.name, style = MaterialTheme.typography.bodyLarge)
                            if (habit.scheduledTime != null) {
                                Spacer(Modifier.width(8.dp))
                                Text(habit.scheduledTime, style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }
                }
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
    val habits by vm.habits.collectAsStateWithLifecycle()
    val occurrences by vm.occurrences.collectAsStateWithLifecycle()
    val testOffset by vm.testDateOffset.collectAsStateWithLifecycle()

    val today = remember(testOffset) { LocalDate.now().plusDays(testOffset) }
    var selectedYearMonth by remember { mutableStateOf(YearMonth.from(today)) }
    
    val daysInMonth = selectedYearMonth.lengthOfMonth()
    val firstDayOfMonth = selectedYearMonth.atDay(1)
    val firstDayOfWeek = firstDayOfMonth.dayOfWeek.value // 1 (Mon) to 7 (Sun)

    // Optimization: Pre-calculate habits for each day of week
    val habitsByDayOfWeek = remember(habits) {
        (1..7).associateWith { dayNum ->
            habits.filter { habit ->
                habit.scheduledDays.isEmpty() || 
                habit.scheduledDays.split(",").contains(dayNum.toString())
            }
        }
    }

    // Optimization: Pre-calculate occurrences for each epoch day
    val occurrencesByEpochDay = remember(occurrences) {
        occurrences.groupBy { it.scheduledEpochDay }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Thống kê", style = MaterialTheme.typography.headlineMedium)
        
        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { selectedYearMonth = selectedYearMonth.minusMonths(1) }) {
                        Text("<")
                    }
                    Text(
                        text = selectedYearMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy", Locale("vi"))).replaceFirstChar { it.uppercase() },
                        style = MaterialTheme.typography.titleMedium
                    )
                    IconButton(onClick = { selectedYearMonth = selectedYearMonth.plusMonths(1) }) {
                        Text(">")
                    }
                }
                
                // Grid header: T2 to CN
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    listOf("T2", "T3", "T4", "T5", "T6", "T7", "CN").forEach { day ->
                        Text(day, style = MaterialTheme.typography.labelSmall, modifier = Modifier.weight(1f), textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                    }
                }

                // Calendar Grid
                val days = remember(selectedYearMonth, firstDayOfWeek, daysInMonth) {
                    val list = mutableListOf<LocalDate?>()
                    for (i in 1 until firstDayOfWeek) { list.add(null) }
                    for (i in 1..daysInMonth) { list.add(selectedYearMonth.atDay(i)) }
                    list
                }
                
                val chunks = remember(days) { days.chunked(7) }
                chunks.forEach { week ->
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        week.forEach { date ->
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .aspectRatio(1f)
                                    .padding(2.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                if (date != null) {
                                    val epochDay = date.toEpochDay()
                                    val dayOccurrences = occurrencesByEpochDay[epochDay] ?: emptyList()
                                    
                                    // Filter habits that existed on this day
                                    val dayHabits = habitsByDayOfWeek[date.dayOfWeek.value]?.filter { 
                                        it.createdAt / 86400000 <= epochDay 
                                    } ?: emptyList()
                                    
                                    val completed = dayOccurrences.count { it.status == OccurrenceStatus.COMPLETED }
                                    val frozen = dayOccurrences.any { it.status == OccurrenceStatus.FROZEN }
                                    val total = dayHabits.size
                                    
                                    val isFuture = epochDay > today.toEpochDay()
                                    
                                    val color = when {
                                        total == 0 || isFuture -> MaterialTheme.colorScheme.surfaceVariant
                                        frozen -> Color(0xFF2196F3) // Blue for Frozen
                                        completed * 2 >= total -> Color(0xFFC8E6C9) // Green (>= 50%)
                                        completed > 0 -> Color(0xFFFFF9C4) // Yellow (< 50%)
                                        else -> Color(0xFFFFCDD2) // Light red (0%)
                                    }

                                    val cellTextColor = if (total == 0 || isFuture) {
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                    } else {
                                        Color(0xFF1C1B1F) // Dark text for light colored cells
                                    }

                                    Surface(
                                        modifier = Modifier.fillMaxSize(),
                                        color = color,
                                        contentColor = cellTextColor,
                                        shape = androidx.compose.foundation.shape.RoundedCornerShape(4.dp),
                                        border = if (date == today) androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text(date.dayOfMonth.toString(), style = MaterialTheme.typography.labelSmall)
                                        }
                                    }
                                }
                            }
                        }
                        if (week.size < 7) {
                            repeat(7 - week.size) {
                                Spacer(Modifier.weight(1f))
                            }
                        }
                    }
                }
            }
        }

        Text("Tỷ lệ hoàn thành: ${"%.1f".format(stats.completionRate)}%")
        LinearProgressIndicator(progress = { (stats.completionRate / 100.0).toFloat() }, modifier = Modifier.fillMaxWidth())
        
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            StatCard("Hoàn thành", stats.completed.toString(), Modifier.weight(1f))
            StatCard("Bỏ lỡ", stats.missed.toString(), Modifier.weight(1f))
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            StatCard("Bỏ qua", stats.skipped.toString(), Modifier.weight(1f))
            StatCard("Đóng băng", stats.frozen.toString(), Modifier.weight(1f))
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            StatCard("Chuỗi hiện tại", stats.currentStreak.toString(), Modifier.weight(1f))
            StatCard("Chuỗi dài nhất", stats.longestStreak.toString(), Modifier.weight(1f))
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            StatCard("Tỷ lệ tuần", "${stats.weeklyCompletionRate.toInt()}%", Modifier.weight(1f))
            StatCard("Tỷ lệ tháng", "${stats.monthlyCompletionRate.toInt()}%", Modifier.weight(1f))
        }
    }
}

@Composable
private fun StatCard(label: String, value: String, modifier: Modifier = Modifier) {
    Card(modifier) {
        Column(Modifier.padding(12.dp)) {
            Text(label, style = MaterialTheme.typography.labelMedium)
            Text(value, style = MaterialTheme.typography.headlineSmall)
        }
    }
}

@Composable
fun LevelDetailDialog(
    stats: UserStatsEntity,
    habits: List<HabitEntity>,
    onSkipLevel: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { 
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Chi tiết cấp độ", style = MaterialTheme.typography.headlineSmall)
                TextButton(onClick = onSkipLevel) {
                    Text("Skip Level(Test)", color = MaterialTheme.colorScheme.error)
                }
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                // XP Progress
                val xpForNext = GamificationManager.getXpForNextLevel(stats.level)
                val xpForCurrent = GamificationManager.getXpForNextLevel(stats.level - 1)
                val progress = (stats.xp - xpForCurrent).toFloat() / (xpForNext - xpForCurrent).toFloat()
                
                Column {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Tiến trình XP", style = MaterialTheme.typography.labelLarge)
                        Text("${stats.xp} / $xpForNext XP", style = MaterialTheme.typography.labelMedium)
                    }
                    LinearProgressIndicator(
                        progress = { progress.coerceIn(0f, 1f) },
                        modifier = Modifier.fillMaxWidth().height(8.dp).padding(vertical = 4.dp),
                        strokeCap = StrokeCap.Round
                    )
                }

                // Skills Inventory
                Text("Kỹ năng khả dụng", style = MaterialTheme.typography.labelLarge)
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    SkillItem("❄️ Đóng băng", stats.streakFreezes.toString(), MaterialTheme.colorScheme.primaryContainer)
                    SkillItem("⏭️ Bỏ qua", stats.skipsAvailable.toString(), MaterialTheme.colorScheme.secondaryContainer)
                }

                // Habit Stats Summary
                Text("Tổng quan thói quen", style = MaterialTheme.typography.labelLarge)
                Text("Đang thực hiện: ${habits.size} thói quen", style = MaterialTheme.typography.bodyMedium)

                // Upcoming Rewards
                Text("Phần thưởng sắp tới", style = MaterialTheme.typography.labelLarge)
                val rewards = GamificationManager.getUpcomingRewards(stats.level)
                rewards.forEach { reward ->
                    Text("• $reward", style = MaterialTheme.typography.bodySmall)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Đóng") }
        }
    )
}

@Composable
private fun SkillItem(label: String, count: String, color: androidx.compose.ui.graphics.Color) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = color,
        modifier = Modifier.height(60.dp)
    ) {
        Column(
            Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(label, style = MaterialTheme.typography.labelSmall)
            Text(count, style = MaterialTheme.typography.titleMedium)
        }
    }
}
