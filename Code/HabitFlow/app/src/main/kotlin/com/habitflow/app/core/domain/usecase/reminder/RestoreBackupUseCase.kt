package com.habitflow.app.core.domain.usecase.reminder

import com.habitflow.app.core.backup.BackupManager

class RestoreBackupUseCase(
    private val backupManager: BackupManager
) {
    suspend operator fun invoke(jsonText: String) {
        backupManager.restoreBackup(jsonText)
    }
}
