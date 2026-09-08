package com.habitflow.app

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

// 1. DATASTORE LƯU TRỮ CÀI ĐẶT
enum class AppTheme { SYSTEM, LIGHT, DARK }

data class UserPreferences(
    val appTheme: AppTheme = AppTheme.SYSTEM,
    val isNotificationEnabled: Boolean = true,
    val greetingMessage: String = ""
)

private val Context.dataStore by preferencesDataStore(name = "user_preferences")

class UserPreferencesDataSource(private val context: Context) {
    private object Keys {
        val APP_THEME = stringPreferencesKey("app_theme")
        val NOTIFICATION_ENABLED = booleanPreferencesKey("notification_enabled")
        val GREETING_MESSAGE = stringPreferencesKey("greeting_message")
    }

    val userPreferencesStream: Flow<UserPreferences> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences()) else throw exception
        }
        .map { preferences ->
            val themeName = preferences[Keys.APP_THEME] ?: AppTheme.SYSTEM.name
            val theme = try { AppTheme.valueOf(themeName) } catch (e: Exception) { AppTheme.SYSTEM }
            val isNotificationEnabled = preferences[Keys.NOTIFICATION_ENABLED] ?: true
            val greetingMessage = preferences[Keys.GREETING_MESSAGE] ?: "Ngày mới lại bắt đầu rồi"

            UserPreferences(
                appTheme = theme,
                isNotificationEnabled = isNotificationEnabled,
                greetingMessage = greetingMessage
            )
        }

    suspend fun updateAppTheme(theme: AppTheme) {
        context.dataStore.edit { it[Keys.APP_THEME] = theme.name }
    }

    suspend fun setNotificationEnabled(enabled: Boolean) {
        context.dataStore.edit { it[Keys.NOTIFICATION_ENABLED] = enabled }
    }

    suspend fun updateGreetingMessage(greeting: String) {
        context.dataStore.edit { it[Keys.GREETING_MESSAGE] = greeting }
    }
}

// 2. VIEWMODEL QUẢN LÝ TRẠNG THÁI CÀI ĐẶT
sealed interface SettingsUiState {
    data object Loading : SettingsUiState
    data class Success(val userPreferences: UserPreferences) : SettingsUiState
}

class SettingsViewModel(
    private val preferencesDataSource: UserPreferencesDataSource
) : ViewModel() {

    val uiState: StateFlow<SettingsUiState> = preferencesDataSource.userPreferencesStream
        .map { SettingsUiState.Success(it) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = SettingsUiState.Loading
        )

    fun onThemeSelected(theme: AppTheme) {
        viewModelScope.launch { preferencesDataSource.updateAppTheme(theme) }
    }

    fun onNotificationToggled(enabled: Boolean) {
        viewModelScope.launch { preferencesDataSource.setNotificationEnabled(enabled) }
    }

    fun onGreetingChanged(greeting: String) {
        viewModelScope.launch { preferencesDataSource.updateGreetingMessage(greeting) }
    }
}

// 3. COMPOSABLE: CHUYỂN ĐỔI THEME
@Composable
fun ThemeToggleRow(
    currentTheme: AppTheme,
    onThemeSelected: (AppTheme) -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = when (currentTheme) {
        AppTheme.DARK -> true
        AppTheme.LIGHT -> false
        AppTheme.SYSTEM -> androidx.compose.foundation.isSystemInDarkTheme()
    }

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("Chế độ sáng/tối")
        Switch(
            checked = isDark,
            onCheckedChange = { checked ->
                onThemeSelected(if (checked) AppTheme.DARK else AppTheme.LIGHT)
            }
        )
    }
}

// 4. DIALOG: CHỈNH SỬA BÁO THỨC (MATERIAL 3)
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
    val hour by remember { mutableIntStateOf(existingReminder?.hour ?: 8) }
    val minute by remember { mutableIntStateOf(existingReminder?.minute ?: 0) }
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
                TimeInput(state = timePickerState)

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

// 5. MÀN HÌNH SAO LƯU CHUYÊN SÂU
@Composable
fun BackupRestoreScreen(
    onNavigateBack: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val database = remember { HabitFlowDatabase.get(context) }
    val backupManager = remember { BackupManager(context, database) }

    var statusMessage by remember { mutableStateOf("") }
    var isSuccess by remember { mutableStateOf(true) }
    var showConfirmRestoreDialog by remember { mutableStateOf(false) }
    var selectedRestoreUri by remember { mutableStateOf<Uri?>(null) }

    val createDocumentLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        if (uri != null) {
            scope.launch {
                try {
                    val jsonText = backupManager.exportBackup()
                    context.contentResolver.openOutputStream(uri)?.use { stream ->
                        stream.write(jsonText.toByteArray())
                    }
                    val timestamp = SimpleDateFormat("HH:mm:ss dd/MM/yyyy", Locale.getDefault()).format(Date())
                    statusMessage = "Đã xuất dữ liệu sao lưu thành công lúc $timestamp"
                    isSuccess = true
                } catch (e: Exception) {
                    statusMessage = "Xuất dữ liệu thất bại: ${e.message}"
                    isSuccess = false
                }
            }
        }
    }

    val openDocumentLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            selectedRestoreUri = uri
            showConfirmRestoreDialog = true
        }
    }

    if (showConfirmRestoreDialog && selectedRestoreUri != null) {
        AlertDialog(
            onDismissRequest = { showConfirmRestoreDialog = false },
            title = { Text("Xác nhận khôi phục dữ liệu") },
            text = {
                Text("Quá trình khôi phục sẽ ghi đè toàn bộ dữ liệu hiện tại bằng dữ liệu từ file sao lưu. Bạn có chắc chắn muốn tiếp tục không?")
            },
            confirmButton = {
                Button(
                    onClick = {
                        val uri = selectedRestoreUri!!
                        showConfirmRestoreDialog = false
                        scope.launch {
                            try {
                                val jsonText = context.contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() }
                                    ?: error("Không đọc được tệp sao lưu")
                                backupManager.restoreBackup(jsonText)
                                statusMessage = "Khôi phục dữ liệu thành công! Toàn bộ thói quen và báo thức đã được cập nhật."
                                isSuccess = true
                            } catch (e: Exception) {
                                statusMessage = "Khôi phục thất bại: ${e.message}"
                                isSuccess = false
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Đồng ý ghi đè")
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmRestoreDialog = false }) {
                    Text("Hủy")
                }
            }
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Sao lưu & Khôi phục", style = MaterialTheme.typography.headlineMedium)

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Xuất bản sao lưu (Export JSON)", style = MaterialTheme.typography.titleMedium)
                Text(
                    "Trích xuất toàn bộ danh sách thói quen, lịch sử thực hiện, mục tiêu và giờ báo thức thành file JSON an toàn.",
                    style = MaterialTheme.typography.bodyMedium
                )
                Button(
                    onClick = { createDocumentLauncher.launch("habitflow_backup_${System.currentTimeMillis() / 1000}.json") },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Xuất file sao lưu")
                }
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Khôi phục từ bản sao lưu (Restore JSON)", style = MaterialTheme.typography.titleMedium)
                Text(
                    "Nạp lại toàn bộ dữ liệu từ file sao lưu JSON trước đó. Hệ thống sẽ tự động kiểm tra tính toàn vẹn và lập lịch lại các báo thức.",
                    style = MaterialTheme.typography.bodyMedium
                )
                OutlinedButton(
                    onClick = { openDocumentLauncher.launch(arrayOf("application/json", "text/plain")) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Chọn file sao lưu để khôi phục")
                }
            }
        }

        if (statusMessage.isNotBlank()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = if (isSuccess) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Text(
                    text = statusMessage,
                    modifier = Modifier.padding(16.dp),
                    color = if (isSuccess) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onErrorContainer,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

// 6. MÀN HÌNH CÀI ĐẶT CHÍNH (SETTINGS SCREEN)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    mainViewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var message by remember { mutableStateOf("") }
    var showBackupRestoreScreen by remember { mutableStateOf(false) }

    val database = remember { HabitFlowDatabase.get(context) }
    val backupManager = remember { BackupManager(context, database) }

    var selectedReminderForEdit by remember { mutableStateOf<ReminderEntity?>(null) }
    var showReminderDialog by remember { mutableStateOf(false) }

    val remindersFlow = remember { database.reminderDao().observeAllEnabled() }
    val activeReminders by remindersFlow.collectAsStateWithLifecycle(initialValue = emptyList())
    val habits by mainViewModel.habits.collectAsStateWithLifecycle()

    if (showBackupRestoreScreen) {
        Column(modifier = modifier.fillMaxSize()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = { showBackupRestoreScreen = false }) {
                    Text("← Quay lại Cài đặt")
                }
            }
            BackupRestoreScreen(
                onNavigateBack = { showBackupRestoreScreen = false }
            )
        }
        return
    }

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val createDocument = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        if (uri != null) {
            scope.launch {
                try {
                    val json = backupManager.exportBackup()
                    context.contentResolver.openOutputStream(uri)?.use { stream ->
                        stream.write(json.toByteArray())
                    }
                    message = "Đã xuất dữ liệu sao lưu thành công"
                } catch (e: Exception) {
                    message = e.message ?: "Xuất thất bại"
                }
            }
        }
    }

    val openDocument = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            scope.launch {
                try {
                    val text = context.contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() }
                        ?: error("Không đọc được tệp")
                    backupManager.restoreBackup(text)
                    message = "Đã khôi phục dữ liệu thành công"
                } catch (e: Exception) {
                    message = e.message ?: "Khôi phục thất bại"
                }
            }
        }
    }

    val notificationPermission = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            viewModel.onNotificationToggled(true)
            message = "Đã bật thông báo nhắc nhở"
        } else {
            message = "Ứng dụng chưa được cấp quyền gửi thông báo"
        }
    }

    if (showReminderDialog) {
        val habitToEdit = habits.firstOrNull { it.id == selectedReminderForEdit?.habitId } ?: habits.firstOrNull()
        if (habitToEdit != null) {
            ReminderEditorDialog(
                habitId = habitToEdit.id,
                habitName = habitToEdit.name,
                existingReminder = selectedReminderForEdit,
                onDismiss = {
                    showReminderDialog = false
                    selectedReminderForEdit = null
                },
                onSave = { reminder ->
                    scope.launch {
                        database.reminderDao().upsert(reminder)
                        if (reminder.enabled) {
                            ReminderScheduler.schedule(context, reminder, habitToEdit.name)
                        } else {
                            ReminderScheduler.cancel(context, reminder)
                        }
                        message = "Đã lưu giờ nhắc nhở cho: ${habitToEdit.name}"
                    }
                },
                onDelete = { reminderId ->
                    scope.launch {
                        selectedReminderForEdit?.let { ReminderScheduler.cancel(context, it) }
                        database.reminderDao().delete(reminderId)
                        message = "Đã xóa nhắc nhở"
                    }
                }
            )
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Cài đặt", style = MaterialTheme.typography.headlineMedium)

        // 1. Sao lưu & Dữ liệu
        Text("Sao lưu & Dữ liệu", style = MaterialTheme.typography.titleMedium)

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { showBackupRestoreScreen = true },
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        ) {
            Row(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Quản lý sao lưu & khôi phục", style = MaterialTheme.typography.titleMedium)
                    Text("Xuất hoặc nạp file sao lưu JSON an toàn cho dữ liệu của bạn", style = MaterialTheme.typography.bodySmall)
                }
                Text("→", style = MaterialTheme.typography.titleMedium)
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = { createDocument.launch("habitflow_backup.json") },
                modifier = Modifier.weight(1f)
            ) {
                Text("Xuất dữ liệu")
            }

            OutlinedButton(
                onClick = { openDocument.launch(arrayOf("application/json", "text/plain")) },
                modifier = Modifier.weight(1f)
            ) {
                Text("Khôi phục")
            }
        }

        if (message.isNotBlank()) {
            Text(message, color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.bodyMedium)
        }

        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

        // 2. Nhắc nhở thói quen
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Nhắc nhở thói quen", style = MaterialTheme.typography.titleMedium)
            if (habits.isNotEmpty()) {
                Button(onClick = {
                    selectedReminderForEdit = null
                    showReminderDialog = true
                }) {
                    Text("+ Thêm mới")
                }
            }
        }

        if (habits.isEmpty()) {
            Text("Chưa có thói quen nào để tạo lịch nhắc nhở.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        } else if (activeReminders.isEmpty()) {
            Text("Hiện chưa có nhắc nhở nào đang hoạt động.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        } else {
            activeReminders.forEach { reminder ->
                val habit = habits.firstOrNull { it.id == reminder.habitId }
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            selectedReminderForEdit = reminder
                            showReminderDialog = true
                        },
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Row(
                        modifier = Modifier
                            .padding(14.dp)
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = habit?.name ?: "Thói quen",
                                style = MaterialTheme.typography.titleSmall
                            )
                            Text(
                                text = "Thời gian: %02d:%02d".format(reminder.hour, reminder.minute),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Text("Chỉnh sửa", style = MaterialTheme.typography.labelMedium)
                    }
                }
            }
        }

        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

        // 3. Hệ thống & Lời chào
        Text("Hệ thống", style = MaterialTheme.typography.titleMedium)

        when (val state = uiState) {
            is SettingsUiState.Loading -> {
                CircularProgressIndicator()
            }
            is SettingsUiState.Success -> {
                val prefs = state.userPreferences

                ThemeToggleRow(
                    currentTheme = prefs.appTheme,
                    onThemeSelected = { newTheme ->
                        viewModel.onThemeSelected(newTheme)
                    }
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Thông báo nhắc nhở")
                    Switch(
                        checked = prefs.isNotificationEnabled,
                        onCheckedChange = { enabled ->
                            if (enabled && Build.VERSION.SDK_INT >= 33) {
                                val hasPermission = ContextCompat.checkSelfPermission(
                                    context,
                                    Manifest.permission.POST_NOTIFICATIONS
                                ) == PackageManager.PERMISSION_GRANTED
                                if (!hasPermission) {
                                    notificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
                                    return@Switch
                                }
                            }
                            viewModel.onNotificationToggled(enabled)
                        }
                    )
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                Text("Lời chào khi mở ứng dụng", style = MaterialTheme.typography.titleMedium)

                var tempGreeting by remember(prefs.greetingMessage) { mutableStateOf(prefs.greetingMessage) }

                OutlinedTextField(
                    value = tempGreeting,
                    onValueChange = { tempGreeting = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Lời chào") },
                    trailingIcon = {
                        if (tempGreeting != prefs.greetingMessage) {
                            TextButton(onClick = { viewModel.onGreetingChanged(tempGreeting) }) {
                                Text("Lưu")
                            }
                        }
                    }
                )
            }
        }
    }
}
