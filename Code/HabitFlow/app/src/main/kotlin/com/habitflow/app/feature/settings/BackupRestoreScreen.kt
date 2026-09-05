package com.habitflow.app.feature.settings

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.habitflow.app.HabitFlowDatabase
import com.habitflow.app.core.backup.BackupManager
import com.habitflow.app.core.domain.scheduler.AndroidReminderScheduler
import com.habitflow.app.core.domain.usecase.reminder.ExportBackupUseCase
import com.habitflow.app.core.domain.usecase.reminder.RestoreBackupUseCase
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BackupRestoreScreen(
    onNavigateBack: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Khởi tạo UseCases từ BackupManager và Database
    val database = remember { HabitFlowDatabase.get(context) }
    val scheduler = remember { AndroidReminderScheduler(context) }
    val backupManager = remember { BackupManager(database, scheduler) }
    val exportUseCase = remember { ExportBackupUseCase(backupManager) }
    val restoreUseCase = remember { RestoreBackupUseCase(backupManager) }

    var statusMessage by remember { mutableStateOf("") }
    var isSuccess by remember { mutableStateOf(true) }
    var showConfirmRestoreDialog by remember { mutableStateOf(false) }
    var selectedRestoreUri by remember { mutableStateOf<Uri?>(null) }

    // Launcher tạo file xuất JSON
    val createDocumentLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        if (uri != null) {
            scope.launch {
                try {
                    val jsonText = exportUseCase()
                    context.contentResolver.openOutputStream(uri)?.use { stream ->
                        stream.write(jsonText.toByteArray())
                    }
                    val timestamp = SimpleDateFormat("HH:mm:ss dd/MM/yyyy", Locale.getDefault()).format(Date())
                    statusMessage = "Đã xuất dữ liệu sao lưu thành công lúc $timestamp!"
                    isSuccess = true
                } catch (e: Exception) {
                    statusMessage = "Xuất dữ liệu thất bại: ${e.message}"
                    isSuccess = false
                }
            }
        }
    }

    // Launcher chọn file khôi phục JSON
    val openDocumentLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            selectedRestoreUri = uri
            showConfirmRestoreDialog = true
        }
    }

    // Dialog cảnh báo an toàn trước khi khôi phục
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
                                restoreUseCase(jsonText)
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

        // 1. Thẻ Xuất dữ liệu
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

        // 2. Thẻ Khôi phục dữ liệu
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

        // 3. Thông báo trạng thái
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
