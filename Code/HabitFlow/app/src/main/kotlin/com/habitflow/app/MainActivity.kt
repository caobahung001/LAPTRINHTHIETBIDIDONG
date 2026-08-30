package com.habitflow.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.habitflow.app.core.datastore.AppTheme
import com.habitflow.app.core.datastore.UserPreferencesDataSource
import com.habitflow.app.feature.settings.SettingsScreen
import com.habitflow.app.feature.settings.SettingsUiState
import com.habitflow.app.feature.settings.SettingsViewModel
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent { HabitFlowApp(viewModel) }
    }
}

private val LightColors = lightColorScheme(
    primary = Color(0xFF5B67F1),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE9EAFF),
    onPrimaryContainer = Color(0xFF202766),
    secondary = Color(0xFF0F9D8A),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFD8F5EF),
    onSecondaryContainer = Color(0xFF0B4B42),
    tertiary = Color(0xFFD97706),
    tertiaryContainer = Color(0xFFFFE8C2),
    onTertiaryContainer = Color(0xFF6D3A00),
    background = Color(0xFFF7F8FC),
    onBackground = Color(0xFF191B22),
    surface = Color.White,
    onSurface = Color(0xFF191B22),
    surfaceVariant = Color(0xFFF0F2F7),
    onSurfaceVariant = Color(0xFF626777),
    outline = Color(0xFFD7DAE5),
    errorContainer = Color(0xFFFFE2E2),
    onErrorContainer = Color(0xFF7D1D1D)
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFFB8BEFF),
    onPrimary = Color(0xFF202766),
    primaryContainer = Color(0xFF343B77),
    onPrimaryContainer = Color(0xFFE8EAFF),
    secondary = Color(0xFF63DDC9),
    onSecondary = Color(0xFF063C34),
    secondaryContainer = Color(0xFF174F47),
    onSecondaryContainer = Color(0xFFD8F5EF),
    tertiary = Color(0xFFFFC76F),
    tertiaryContainer = Color(0xFF61451A),
    onTertiaryContainer = Color(0xFFFFE8C2),
    background = Color(0xFF0F1117),
    onBackground = Color(0xFFE7E8EF),
    surface = Color(0xFF171A22),
    onSurface = Color(0xFFE7E8EF),
    surfaceVariant = Color(0xFF222631),
    onSurfaceVariant = Color(0xFFB7BBC8),
    outline = Color(0xFF3A3F4B),
    errorContainer = Color(0xFF5A2528),
    onErrorContainer = Color(0xFFFFDAD9)
)

@Composable
fun HabitFlowApp(viewModel: MainViewModel) {
    val context = LocalContext.current
    val settingsViewModel = remember {
        SettingsViewModel(UserPreferencesDataSource(context.applicationContext))
    }
    val settingsUiState by settingsViewModel.uiState.collectAsStateWithLifecycle()

    var greetingShown by remember { mutableStateOf(false) }
    LaunchedEffect(settingsUiState) {
        val state = settingsUiState as? SettingsUiState.Success
        if (!greetingShown && state != null && state.userPreferences.greetingMessage.isNotBlank()) {
            android.widget.Toast.makeText(
                context,
                state.userPreferences.greetingMessage,
                android.widget.Toast.LENGTH_SHORT
            ).show()
            greetingShown = true
        }
    }

    val darkTheme = when (val state = settingsUiState) {
        is SettingsUiState.Success -> when (state.userPreferences.appTheme) {
            AppTheme.DARK -> true
            AppTheme.LIGHT -> false
            AppTheme.SYSTEM -> androidx.compose.foundation.isSystemInDarkTheme()
        }
        else -> androidx.compose.foundation.isSystemInDarkTheme()
    }

    MaterialTheme(colorScheme = if (darkTheme) DarkColors else LightColors) {
        var tab by rememberSaveable { mutableIntStateOf(0) }
        val navItems = listOf(
            NavItem("✓", "Hôm nay"),
            NavItem("◎", "Thói quen"),
            NavItem("◆", "Mục tiêu"),
            NavItem("▥", "Thống kê"),
            NavItem("⚙", "Cài đặt")
        )

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = MaterialTheme.colorScheme.background,
            bottomBar = {
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 2.dp,
                    shadowElevation = 10.dp
                ) {
                    NavigationBar(
                        modifier = Modifier.navigationBarsPadding(),
                        containerColor = MaterialTheme.colorScheme.surface,
                        tonalElevation = 0.dp
                    ) {
                        navItems.forEachIndexed { index, item ->
                            NavigationBarItem(
                                selected = tab == index,
                                onClick = { tab = index },
                                icon = {
                                    Text(
                                        item.icon,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                },
                                label = { Text(item.label, maxLines = 1) },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                    selectedTextColor = MaterialTheme.colorScheme.primary,
                                    indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )
                        }
                    }
                }
            }
        ) { innerPadding ->
            Box(
                Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                when (tab) {
                    0 -> TodayScreen(viewModel, onNavigateToHabits = { tab = 1 })
                    1 -> HabitsScreen(viewModel)
                    2 -> GoalsScreen(viewModel)
                    3 -> StatisticsScreen(viewModel)
                    else -> SettingsScreen(
                        viewModel = settingsViewModel,
                        mainViewModel = viewModel
                    )
                }
            }
        }
    }
}

private data class NavItem(val icon: String, val label: String)

@Composable
private fun ScreenHeader(
    eyebrow: String,
    title: String,
    subtitle: String? = null,
    trailing: (@Composable () -> Unit)? = null
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text(
                text = eyebrow.uppercase(),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            if (!subtitle.isNullOrBlank()) {
                Spacer(Modifier.height(4.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        trailing?.invoke()
    }
}

@Composable
private fun TodayScreen(vm: MainViewModel, onNavigateToHabits: () -> Unit) {
    val habits by vm.habits.collectAsStateWithLifecycle()
    val occurrences by vm.occurrences.collectAsStateWithLifecycle()
    val userStats by vm.userStats.collectAsStateWithLifecycle()
    val testOffset by vm.testDateOffset.collectAsStateWithLifecycle()
    var showLevelDetail by remember { mutableStateOf(false) }

    val today = remember(testOffset) { LocalDate.now().plusDays(testOffset) }
    val todayEpochDay = remember(today) { today.toEpochDay() }
    val currentDayOfWeek = remember(today) { today.dayOfWeek.value }
    val tomorrow = remember(today) { today.plusDays(1) }
    val tomorrowDayOfWeek = remember(tomorrow) { tomorrow.dayOfWeek.value }

    val filteredHabits = remember(habits, currentDayOfWeek) {
        habits.filter { it.scheduledDays.isEmpty() || it.scheduledDays.split(",").contains(currentDayOfWeek.toString()) }
    }
    val tomorrowHabits = remember(habits, tomorrowDayOfWeek) {
        habits.filter { it.scheduledDays.isEmpty() || it.scheduledDays.split(",").contains(tomorrowDayOfWeek.toString()) }
    }
    val todayOccurrences = remember(occurrences, todayEpochDay) {
        occurrences.filter { it.scheduledEpochDay == todayEpochDay }.associateBy { it.habitId }
    }
    val completedToday = filteredHabits.count {
        todayOccurrences[it.id]?.status == OccurrenceStatus.COMPLETED
    }
    val dailyProgress = if (filteredHabits.isEmpty()) 0f else completedToday.toFloat() / filteredHabits.size
    val dateText = remember(today) {
        today.format(DateTimeFormatter.ofPattern("EEEE, dd 'tháng' MM", Locale("vi")))
            .replaceFirstChar { it.uppercase() }
    }

    if (showLevelDetail && userStats != null) {
        LevelDetailDialog(
            stats = userStats!!,
            habits = habits,
            onSkipLevel = { vm.skipLevel() },
            onDismiss = { showLevelDetail = false }
        )
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 18.dp, vertical = 18.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            ScreenHeader(
                eyebrow = dateText,
                title = "Hôm nay",
                subtitle = if (filteredHabits.isEmpty()) "Một ngày nhẹ nhàng cũng là một ngày có tiến bộ." else "$completedToday/${filteredHabits.size} thói quen đã hoàn thành",
                trailing = {
                    TextButton(onClick = vm::advanceTestDay) {
                        Text("+1 ngày test", color = MaterialTheme.colorScheme.error)
                    }
                }
            )
        }

        userStats?.let { stats ->
            item {
                LevelHeroCard(
                    stats = stats,
                    dailyProgress = dailyProgress,
                    completedToday = completedToday,
                    totalToday = filteredHabits.size,
                    onClick = { showLevelDetail = true }
                )
            }
        }

        if (filteredHabits.isEmpty()) {
            item {
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("🌱", style = MaterialTheme.typography.displaySmall)
                        Spacer(Modifier.height(10.dp))
                        Text("Chưa có việc cần làm hôm nay", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(6.dp))
                        Text(
                            "Tạo thói quen đầu tiên để HabitFlow bắt đầu theo dõi tiến độ cho bạn.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                        Spacer(Modifier.height(16.dp))
                        Button(
                            onClick = onNavigateToHabits,
                            shape = RoundedCornerShape(14.dp)
                        ) { Text("Thêm thói quen") }
                    }
                }
            }
        } else {
            item { SectionTitle("Việc cần làm", "Tập trung vào từng việc nhỏ") }
            items(filteredHabits, key = { it.id }) { habit ->
                HabitTodayCard(
                    habit = habit,
                    occurrence = todayOccurrences[habit.id],
                    streakFreezes = userStats?.streakFreezes ?: 0,
                    skipsAvailable = userStats?.skipsAvailable ?: 0,
                    onComplete = { vm.mark(habit.id, OccurrenceStatus.COMPLETED) },
                    onSkip = { vm.mark(habit.id, OccurrenceStatus.SKIPPED) },
                    onFreeze = { vm.useStreakFreeze(habit.id) },
                    onUseSkip = { vm.useSkip(habit.id) },
                    onReset = { vm.unmark(habit.id, todayEpochDay) }
                )
            }
        }

        if (tomorrowHabits.isNotEmpty()) {
            item { SectionTitle("Ngày mai", "Chuẩn bị trước để giữ nhịp") }
            items(tomorrowHabits, key = { "tomorrow_${it.id}" }) { habit ->
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f)
                ) {
                    Row(
                        Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(shape = CircleShape, color = MaterialTheme.colorScheme.primaryContainer) {
                            Text("→", modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp), color = MaterialTheme.colorScheme.primary)
                        }
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(habit.name, fontWeight = FontWeight.SemiBold)
                            if (habit.description.isNotBlank()) {
                                Text(habit.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        habit.scheduledTime?.let { TimePill(it) }
                    }
                }
            }
        }
    }
}

@Composable
private fun LevelHeroCard(
    stats: UserStatsEntity,
    dailyProgress: Float,
    completedToday: Int,
    totalToday: Int,
    onClick: () -> Unit
) {
    val xpForNext = GamificationManager.getXpForNextLevel(stats.level)
    val xpForCurrent = GamificationManager.getXpForNextLevel(stats.level - 1)
    val xpProgress = if (xpForNext == xpForCurrent) 0f else
        ((stats.xp - xpForCurrent).toFloat() / (xpForNext - xpForCurrent).toFloat()).coerceIn(0f, 1f)

    ElevatedCard(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 0.dp)
    ) {
        Column(Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    modifier = Modifier.size(54.dp),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primary
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            stats.level.toString(),
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Spacer(Modifier.width(14.dp))
                Column(Modifier.weight(1f)) {
                    Text("Cấp độ ${stats.level}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Text("${stats.xp} XP • chạm để xem phần thưởng", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.75f))
                }
                if (stats.streakFreezes > 0) {
                    Surface(shape = RoundedCornerShape(12.dp), color = MaterialTheme.colorScheme.surface.copy(alpha = 0.75f)) {
                        Text("❄ ${stats.streakFreezes}", modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp), fontWeight = FontWeight.SemiBold)
                    }
                }
            }
            Spacer(Modifier.height(18.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Tiến độ cấp", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
                Text("$xpForNext XP", style = MaterialTheme.typography.labelMedium)
            }
            Spacer(Modifier.height(7.dp))
            LinearProgressIndicator(
                progress = { xpProgress },
                modifier = Modifier.fillMaxWidth().height(8.dp).clip(CircleShape),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.75f),
                strokeCap = StrokeCap.Round
            )
            Spacer(Modifier.height(16.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Hôm nay", style = MaterialTheme.typography.labelMedium)
                Spacer(Modifier.width(10.dp))
                LinearProgressIndicator(
                    progress = { dailyProgress.coerceIn(0f, 1f) },
                    modifier = Modifier.weight(1f).height(6.dp).clip(CircleShape),
                    color = MaterialTheme.colorScheme.secondary,
                    trackColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f)
                )
                Spacer(Modifier.width(10.dp))
                Text("$completedToday/$totalToday", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun HabitTodayCard(
    habit: HabitEntity,
    occurrence: OccurrenceEntity?,
    streakFreezes: Int,
    skipsAvailable: Int,
    onComplete: () -> Unit,
    onSkip: () -> Unit,
    onFreeze: () -> Unit,
    onUseSkip: () -> Unit,
    onReset: () -> Unit
) {
    val container = when (occurrence?.status) {
        OccurrenceStatus.COMPLETED -> MaterialTheme.colorScheme.secondaryContainer
        OccurrenceStatus.SKIPPED -> MaterialTheme.colorScheme.tertiaryContainer
        OccurrenceStatus.FROZEN -> MaterialTheme.colorScheme.primaryContainer
        OccurrenceStatus.MISSED -> MaterialTheme.colorScheme.errorContainer
        else -> MaterialTheme.colorScheme.surface
    }

    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = container),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = if (occurrence == null) 2.dp else 0.dp)
    ) {
        Column(Modifier.padding(17.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    modifier = Modifier.size(38.dp),
                    shape = CircleShape,
                    color = if (occurrence?.status == OccurrenceStatus.COMPLETED) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primaryContainer
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            if (occurrence?.status == OccurrenceStatus.COMPLETED) "✓" else "•",
                            color = if (occurrence?.status == OccurrenceStatus.COMPLETED) MaterialTheme.colorScheme.onSecondary else MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(habit.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    if (habit.description.isNotBlank()) {
                        Text(habit.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                habit.scheduledTime?.let { TimePill(it) }
            }

            Spacer(Modifier.height(14.dp))
            if (occurrence != null) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    StatusPill(occurrence.status)
                    Spacer(Modifier.weight(1f))
                    TextButton(onClick = onReset) { Text("Thay đổi") }
                }
            } else {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = onComplete,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(13.dp)
                    ) { Text("Hoàn thành") }
                    OutlinedButton(
                        onClick = onSkip,
                        shape = RoundedCornerShape(13.dp)
                    ) { Text("Bỏ qua") }
                }
                if (streakFreezes > 0 || skipsAvailable > 0) {
                    Spacer(Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (streakFreezes > 0) {
                            AssistChip(onClick = onFreeze, label = { Text("❄ Đóng băng") })
                        }
                        if (skipsAvailable > 0) {
                            AssistChip(onClick = onUseSkip, label = { Text("⏭ Vé bỏ qua") })
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StatusPill(status: OccurrenceStatus) {
    val text = when (status) {
        OccurrenceStatus.COMPLETED -> "Đã hoàn thành"
        OccurrenceStatus.SKIPPED -> "Đã bỏ qua"
        OccurrenceStatus.FROZEN -> "Đã đóng băng"
        OccurrenceStatus.MISSED -> "Đã bỏ lỡ"
        OccurrenceStatus.PENDING -> "Đang chờ"
    }
    Surface(
        shape = RoundedCornerShape(999.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.72f)
    ) {
        Text(text, modifier = Modifier.padding(horizontal = 11.dp, vertical = 6.dp), style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun TimePill(time: String) {
    Surface(shape = RoundedCornerShape(999.dp), color = MaterialTheme.colorScheme.surfaceVariant) {
        Text("◷ $time", modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp), style = MaterialTheme.typography.labelMedium)
    }
}

@Composable
private fun SectionTitle(title: String, subtitle: String? = null) {
    Column {
        Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        if (!subtitle.isNullOrBlank()) {
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun GoalsScreen(vm: MainViewModel) {
    val goals by vm.goals.collectAsStateWithLifecycle()
    var name by remember { mutableStateOf("") }
    var target by remember { mutableStateOf("10") }
    var type by remember { mutableStateOf(GoalMetricType.OCCURRENCE_COUNT) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 18.dp, vertical = 18.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            ScreenHeader(
                eyebrow = "Định hướng",
                title = "Mục tiêu",
                subtitle = "Biến tiến bộ nhỏ thành kết quả nhìn thấy được."
            )
        }
        item {
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Tạo mục tiêu mới", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Tên mục tiêu") },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp)
                    )
                    OutlinedTextField(
                        value = target,
                        onValueChange = { target = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Giá trị mục tiêu") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp)
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = type == GoalMetricType.OCCURRENCE_COUNT,
                            onClick = { type = GoalMetricType.OCCURRENCE_COUNT },
                            label = { Text("Theo số lần") }
                        )
                        FilterChip(
                            selected = type == GoalMetricType.ACCUMULATED_VALUE,
                            onClick = { type = GoalMetricType.ACCUMULATED_VALUE },
                            label = { Text("Theo giá trị") }
                        )
                    }
                    Button(
                        onClick = {
                            target.toDoubleOrNull()?.let { value ->
                                if (name.isNotBlank() && value > 0) {
                                    vm.addGoal(name.trim(), value, type)
                                    name = ""
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp)
                    ) { Text("Tạo mục tiêu") }
                }
            }
        }

        if (goals.isEmpty()) {
            item {
                EmptyCompactCard("🎯", "Chưa có mục tiêu", "Tạo một mục tiêu để theo dõi tiến độ dài hạn.")
            }
        } else {
            item { SectionTitle("Đang theo đuổi", "${goals.size} mục tiêu đang hoạt động") }
            items(goals, key = { it.id }) { goal ->
                val progress = (goal.currentValue / goal.targetValue).toFloat().coerceIn(0f, 1f)
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(Modifier.padding(18.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(shape = RoundedCornerShape(14.dp), color = MaterialTheme.colorScheme.tertiaryContainer) {
                                Text("◆", modifier = Modifier.padding(10.dp), color = MaterialTheme.colorScheme.tertiary, fontWeight = FontWeight.Bold)
                            }
                            Spacer(Modifier.width(12.dp))
                            Column(Modifier.weight(1f)) {
                                Text(goal.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                Text(
                                    "${formatNumber(goal.currentValue)} / ${formatNumber(goal.targetValue)} ${goal.unit}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Text("${(progress * 100).toInt()}%", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                        }
                        Spacer(Modifier.height(14.dp))
                        LinearProgressIndicator(
                            progress = { progress },
                            modifier = Modifier.fillMaxWidth().height(8.dp).clip(CircleShape),
                            color = MaterialTheme.colorScheme.primary,
                            trackColor = MaterialTheme.colorScheme.primaryContainer
                        )
                        Spacer(Modifier.height(10.dp))
                        OutlinedButton(
                            onClick = { vm.addGoalProgress(goal, 1.0) },
                            modifier = Modifier.align(Alignment.End),
                            shape = RoundedCornerShape(12.dp)
                        ) { Text("+1 tiến độ") }
                    }
                }
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
    val firstDayOfWeek = selectedYearMonth.atDay(1).dayOfWeek.value
    val habitsByDayOfWeek = remember(habits) {
        (1..7).associateWith { dayNum ->
            habits.filter { it.scheduledDays.isEmpty() || it.scheduledDays.split(",").contains(dayNum.toString()) }
        }
    }
    val occurrencesByEpochDay = remember(occurrences) { occurrences.groupBy { it.scheduledEpochDay } }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 18.dp, vertical = 18.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            ScreenHeader(
                eyebrow = "Tổng quan",
                title = "Thống kê",
                subtitle = "Nhìn lại nhịp duy trì và chuỗi tiến bộ của bạn."
            )
        }
        item {
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Row(Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(
                            progress = { (stats.completionRate / 100.0).toFloat().coerceIn(0f, 1f) },
                            modifier = Modifier.size(78.dp),
                            strokeWidth = 8.dp,
                            trackColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f)
                        )
                        Text("${stats.completionRate.toInt()}%", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }
                    Spacer(Modifier.width(18.dp))
                    Column {
                        Text("Tỷ lệ hoàn thành", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                        Text("${stats.completed} lần hoàn thành", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        Text("Tuần ${stats.weeklyCompletionRate.toInt()}% • Tháng ${stats.monthlyCompletionRate.toInt()}%", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.75f))
                    }
                }
            }
        }
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                StatCard("✓", "Hoàn thành", stats.completed.toString(), Modifier.weight(1f))
                StatCard("×", "Bỏ lỡ", stats.missed.toString(), Modifier.weight(1f))
            }
        }
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                StatCard("↷", "Bỏ qua", stats.skipped.toString(), Modifier.weight(1f))
                StatCard("❄", "Đóng băng", stats.frozen.toString(), Modifier.weight(1f))
            }
        }
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                StatCard("🔥", "Chuỗi hiện tại", stats.currentStreak.toString(), Modifier.weight(1f))
                StatCard("★", "Dài nhất", stats.longestStreak.toString(), Modifier.weight(1f))
            }
        }
        item {
            CalendarCard(
                selectedYearMonth = selectedYearMonth,
                today = today,
                daysInMonth = daysInMonth,
                firstDayOfWeek = firstDayOfWeek,
                habitsByDayOfWeek = habitsByDayOfWeek,
                occurrencesByEpochDay = occurrencesByEpochDay,
                onPrevious = { selectedYearMonth = selectedYearMonth.minusMonths(1) },
                onNext = { selectedYearMonth = selectedYearMonth.plusMonths(1) }
            )
        }
    }
}

@Composable
private fun StatCard(icon: String, label: String, value: String, modifier: Modifier = Modifier) {
    ElevatedCard(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(icon, style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))
            Text(value, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun CalendarCard(
    selectedYearMonth: YearMonth,
    today: LocalDate,
    daysInMonth: Int,
    firstDayOfWeek: Int,
    habitsByDayOfWeek: Map<Int, List<HabitEntity>>,
    occurrencesByEpochDay: Map<Long, List<OccurrenceEntity>>,
    onPrevious: () -> Unit,
    onNext: () -> Unit
) {
    val days = remember(selectedYearMonth, firstDayOfWeek, daysInMonth) {
        buildList<LocalDate?> {
            repeat(firstDayOfWeek - 1) { add(null) }
            for (day in 1..daysInMonth) add(selectedYearMonth.atDay(day))
        }
    }

    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("Lịch duy trì", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text(
                        selectedYearMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy", Locale("vi"))).replaceFirstChar { it.uppercase() },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                FilledTonalButton(onClick = onPrevious, contentPadding = PaddingValues(horizontal = 12.dp)) { Text("‹") }
                Spacer(Modifier.width(6.dp))
                FilledTonalButton(onClick = onNext, contentPadding = PaddingValues(horizontal = 12.dp)) { Text("›") }
            }

            Row(Modifier.fillMaxWidth()) {
                listOf("T2", "T3", "T4", "T5", "T6", "T7", "CN").forEach { day ->
                    Text(day, modifier = Modifier.weight(1f), textAlign = TextAlign.Center, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            days.chunked(7).forEach { week ->
                Row(Modifier.fillMaxWidth()) {
                    week.forEach { date ->
                        CalendarDayCell(
                            date = date,
                            today = today,
                            habitsByDayOfWeek = habitsByDayOfWeek,
                            occurrencesByEpochDay = occurrencesByEpochDay,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    repeat(7 - week.size) { Spacer(Modifier.weight(1f).aspectRatio(1f)) }
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                LegendDot(MaterialTheme.colorScheme.secondaryContainer, "Tốt")
                LegendDot(MaterialTheme.colorScheme.tertiaryContainer, "Một phần")
                LegendDot(MaterialTheme.colorScheme.errorContainer, "Chưa đạt")
            }
        }
    }
}

@Composable
private fun CalendarDayCell(
    date: LocalDate?,
    today: LocalDate,
    habitsByDayOfWeek: Map<Int, List<HabitEntity>>,
    occurrencesByEpochDay: Map<Long, List<OccurrenceEntity>>,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.aspectRatio(1f).padding(2.dp), contentAlignment = Alignment.Center) {
        if (date != null) {
            val epochDay = date.toEpochDay()
            val dayOccurrences = occurrencesByEpochDay[epochDay] ?: emptyList()
            val dayHabits = habitsByDayOfWeek[date.dayOfWeek.value]?.filter { it.createdAt / 86400000 <= epochDay } ?: emptyList()
            val completed = dayOccurrences.count { it.status == OccurrenceStatus.COMPLETED }
            val frozen = dayOccurrences.any { it.status == OccurrenceStatus.FROZEN }
            val total = dayHabits.size
            val future = epochDay > today.toEpochDay()
            val color = when {
                total == 0 || future -> MaterialTheme.colorScheme.surfaceVariant
                frozen -> MaterialTheme.colorScheme.primaryContainer
                completed * 2 >= total -> MaterialTheme.colorScheme.secondaryContainer
                completed > 0 -> MaterialTheme.colorScheme.tertiaryContainer
                else -> MaterialTheme.colorScheme.errorContainer
            }
            Surface(
                modifier = Modifier.fillMaxSize(),
                shape = RoundedCornerShape(10.dp),
                color = color,
                border = if (date == today) BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(date.dayOfMonth.toString(), style = MaterialTheme.typography.labelMedium, fontWeight = if (date == today) FontWeight.Bold else FontWeight.Normal)
                }
            }
        }
    }
}

@Composable
private fun LegendDot(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(8.dp).background(color, CircleShape))
        Spacer(Modifier.width(4.dp))
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun EmptyCompactCard(icon: String, title: String, description: String) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
    ) {
        Row(Modifier.padding(18.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(icon, style = MaterialTheme.typography.headlineSmall)
            Spacer(Modifier.width(14.dp))
            Column {
                Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

private fun formatNumber(value: Double): String =
    if (value % 1.0 == 0.0) value.toInt().toString() else "%.1f".format(value)

@Composable
fun LevelDetailDialog(
    stats: UserStatsEntity,
    habits: List<HabitEntity>,
    onSkipLevel: () -> Unit,
    onDismiss: () -> Unit
) {
    val xpForNext = GamificationManager.getXpForNextLevel(stats.level)
    val xpForCurrent = GamificationManager.getXpForNextLevel(stats.level - 1)
    val progress = if (xpForNext == xpForCurrent) 0f else
        ((stats.xp - xpForCurrent).toFloat() / (xpForNext - xpForCurrent).toFloat()).coerceIn(0f, 1f)

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(26.dp),
        title = { Text("Cấp độ ${stats.level}", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Column {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Tiến trình XP", style = MaterialTheme.typography.labelLarge)
                        Text("${stats.xp} / $xpForNext XP", style = MaterialTheme.typography.labelMedium)
                    }
                    Spacer(Modifier.height(7.dp))
                    LinearProgressIndicator(progress = { progress }, modifier = Modifier.fillMaxWidth().height(8.dp).clip(CircleShape))
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    SkillItem("❄ Đóng băng", stats.streakFreezes.toString(), Modifier.weight(1f))
                    SkillItem("⏭ Bỏ qua", stats.skipsAvailable.toString(), Modifier.weight(1f))
                }
                Column {
                    Text("Tổng quan", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                    Text("${habits.size} thói quen đang hoạt động", style = MaterialTheme.typography.bodyMedium)
                }
                Column {
                    Text("Phần thưởng sắp tới", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                    GamificationManager.getUpcomingRewards(stats.level).forEach { reward ->
                        Text("• $reward", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Đóng") } },
        dismissButton = { TextButton(onClick = onSkipLevel) { Text("Skip level (test)", color = MaterialTheme.colorScheme.error) } }
    )
}

@Composable
private fun SkillItem(label: String, count: String, modifier: Modifier = Modifier) {
    Surface(modifier = modifier, shape = RoundedCornerShape(16.dp), color = MaterialTheme.colorScheme.surfaceVariant) {
        Column(Modifier.padding(14.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(label, style = MaterialTheme.typography.labelSmall)
            Text(count, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        }
    }
}
