package com.habitflow.app.feature.settings

import android.Manifest
import android.content.pm.PackageManager
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.habitflow.app.HabitFlowDatabase
import com.habitflow.app.MainViewModel
import com.habitflow.app.ReminderEntity
import com.habitflow.app.core.backup.BackupManager
import com.habitflow.app.core.data.ReminderRepositoryImpl
import com.habitflow.app.core.domain.scheduler.AndroidReminderScheduler
import com.habitflow.app.core.domain.usecase.reminder.ExportBackupUseCase
import com.habitflow.app.core.domain.usecase.reminder.RestoreBackupUseCase
import kotlinx.coroutines.launch

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

    // Khởi tạo các thành phần Core
    val database = remember { HabitFlowDatabase.get(context) }
    val scheduler = remember { AndroidReminderScheduler(context) }
    val reminderRepository = remember { ReminderRepositoryImpl(database.reminderDao()) }
    val backupManager = remember { BackupManager(database, scheduler) }
    val exportUseCase = remember { ExportBackupUseCase(backupManager) }
    val restoreUseCase = remember { RestoreBackupUseCase(backupManager) }

    // Quản lý dialog chỉnh sửa giờ nhắc nhở
    var selectedReminderForEdit by remember { mutableStateOf<ReminderEntity?>(null) }
    var showReminderDialog by remember { mutableStateOf(false) }
    val activeReminders by reminderRepository.observeAllEnabled().collectAsStateWithLifecycle(initialValue = emptyList())
    val habits by mainViewModel.habits.collectAsStateWithLifecycle()

    // Nếu người dùng chọn mở màn hình Sao lưu Chuyên sâu
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

    // Launcher Xuất file JSON
    val createDocument = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        if (uri != null) {
            scope.launch {
                try {
                    val json = exportUseCase()
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

    // Launcher Khôi phục file JSON
    val openDocument = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            scope.launch {
                try {
                    val text = context.contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() }
                        ?: error("Không đọc được tệp")
                    restoreUseCase(text)
                    message = "Đã khôi phục dữ liệu thành công"
                } catch (e: Exception) {
                    message = e.message ?: "Khôi phục thất bại"
                }
            }
        }
    }

    // Launcher xin quyền thông báo
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

    // Dialog tạo/chỉnh sửa nhắc nhở
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
                        reminderRepository.saveReminder(reminder)
                        if (reminder.enabled) {
                            scheduler.schedule(reminder, habitToEdit.name)
                        } else {
                            scheduler.cancel(reminder)
                        }
                        message = "Đã lưu giờ nhắc nhở cho: ${habitToEdit.name}"
                    }
                },
                onDelete = { reminderId ->
                    scope.launch {
                        selectedReminderForEdit?.let { scheduler.cancel(it) }
                        reminderRepository.deleteReminder(reminderId)
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

        // 1. Quản lý Sao lưu & Khôi phục
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

        // 2. Khu vực Quản lý Báo thức
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

        // 3. Tùy chọn Giao diện & Hệ thống
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
            }
        }
    }
}