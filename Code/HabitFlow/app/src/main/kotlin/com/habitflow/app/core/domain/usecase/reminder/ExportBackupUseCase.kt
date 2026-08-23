package com.habitflow.app.core.domain.usecase.reminder

import com.habitflow.app.core.backup.BackupManager

class ExportBackupUseCase(
    private val backupManager: BackupManager
) {
    suspend operator fun invoke(): String = backupManager.exportBackup()
}
