package com.habitflow.app.feature.settings

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.habitflow.app.MainViewModel
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    mainViewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var message by remember { mutableStateOf("") }

    val createDocument = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        if (uri != null) {
            scope.launch {
                try {
                    val json = mainViewModel.exportJson()
                    context.contentResolver.openOutputStream(uri)?.use { stream ->
                        stream.write(json.toByteArray())
                    }
                    message = "Đã xuất dữ liệu"
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
                    mainViewModel.restoreJson(text)
                    message = "Đã khôi phục dữ liệu"
                } catch (e: Exception) {
                    message = e.message ?: "Khôi phục thất bại"
                }
            }
        }
    }

    val notificationPermission = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        message = if (granted) "Đã cấp quyền thông báo" else "Chưa cấp quyền thông báo"
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Cài đặt", style = MaterialTheme.typography.headlineMedium)

        // 1. Nút Xuất JSON, Khôi phục JSON và Cấp quyền thông báo (Giao diện cũ)
        Button(onClick = { createDocument.launch("habitflow-backup.json") }) {
            Text("Xuất JSON")
        }

        OutlinedButton(onClick = { openDocument.launch(arrayOf("application/json", "text/plain")) }) {
            Text("Khôi phục JSON")
        }

        OutlinedButton(onClick = {
            if (Build.VERSION.SDK_INT >= 33) {
                val hasPermission = androidx.core.content.ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == android.content.pm.PackageManager.PERMISSION_GRANTED

                if (hasPermission) {
                    message = "Ứng dụng ĐÃ ĐƯỢC CẤP QUYỀN thông báo từ trước rồi!"
                } else {
                    notificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            } else {
                message = "Thiết bị Android phiên bản cũ không cần xin quyền runtime"
            }
        }) {
            Text("Cấp quyền thông báo")
        }

        OutlinedButton(onClick = {
            com.habitflow.app.core.reminder.NotificationFactory.showNotification(
                context = context,
                notificationId = 1001,
                habitName = "Uống 2L nước mỗi ngày",
                note = "Hãy uống 1 ly nước đầy để giữ năng lượng cho ngày mới!"
            )
            message = "Đã phát thông báo thử nghiệm!"
        }) {
            Text("Test thông báo tức thì 🔔")
        }

        OutlinedButton(onClick = {
            val alarmManager = context.getSystemService(android.content.Context.ALARM_SERVICE) as android.app.AlarmManager
            val intent = android.content.Intent(context, com.habitflow.app.core.reminder.AlarmReceiver::class.java).apply {
                putExtra(com.habitflow.app.core.reminder.AlarmReceiver.EXTRA_NOTIFICATION_ID, 9999)
                putExtra(com.habitflow.app.core.reminder.AlarmReceiver.EXTRA_HABIT_NAME, "Tập thể dục buổi sáng 🏃")
                putExtra(com.habitflow.app.core.reminder.AlarmReceiver.EXTRA_NOTE, "AlarmManager đã đánh thức hệ thống đúng giờ!")
            }
            val pendingIntent = android.app.PendingIntent.getBroadcast(
                context,
                9999,
                intent,
                android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE
            )
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    if (alarmManager.canScheduleExactAlarms()) {
                        alarmManager.setExactAndAllowWhileIdle(
                            android.app.AlarmManager.RTC_WAKEUP,
                            System.currentTimeMillis() + 10_000,
                            pendingIntent
                        )
                    } else {
                        alarmManager.setAndAllowWhileIdle(
                            android.app.AlarmManager.RTC_WAKEUP,
                            System.currentTimeMillis() + 10_000,
                            pendingIntent
                        )
                    }
                } else {
                    alarmManager.setExactAndAllowWhileIdle(
                        android.app.AlarmManager.RTC_WAKEUP,
                        System.currentTimeMillis() + 10_000,
                        pendingIntent
                    )
                }
                message = "Đã hẹn giờ! Hãy thoát app, chuông sẽ nổ sau 10 giây ⏰"
            } catch (e: SecurityException) {
                alarmManager.setAndAllowWhileIdle(
                    android.app.AlarmManager.RTC_WAKEUP,
                    System.currentTimeMillis() + 10_000,
                    pendingIntent
                )
                message = "Đã hẹn giờ (chế độ tiêu chuẩn)! Chuông sẽ nổ sau 10 giây ⏰"
            }
        }) {
            Text("Hẹn giờ Alarm nổ sau 10 giây ⏰")
        }

        if (message.isNotBlank()) {
            Text(message, color = MaterialTheme.colorScheme.primary)
        }

        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

        // 2. Chọn Giao diện Theme & Cài đặt (Thành viên 6)
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
                    Text("Bật thông báo nhắc nhở")
                    Switch(
                        checked = prefs.isNotificationEnabled,
                        onCheckedChange = { enabled ->
                            viewModel.onNotificationToggled(enabled)
                        }
                    )
                }
            }
        }
    }
}