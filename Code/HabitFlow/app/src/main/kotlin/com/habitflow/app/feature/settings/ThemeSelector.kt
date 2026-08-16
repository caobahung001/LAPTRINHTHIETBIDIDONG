package com.habitflow.app.feature.settings

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.habitflow.app.core.datastore.AppTheme

@Composable
fun ThemeToggleRow(
    currentTheme: AppTheme,
    onThemeSelected: (AppTheme) -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = currentTheme == AppTheme.DARK

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Chế độ sáng/tối",
            style = MaterialTheme.typography.bodyLarge
        )
        Switch(
            checked = isDark,
            onCheckedChange = { checked ->
                onThemeSelected(if (checked) AppTheme.DARK else AppTheme.LIGHT)
            }
        )
    }
}