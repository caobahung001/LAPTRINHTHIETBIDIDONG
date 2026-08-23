package com.habitflow.feature.goals

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.habitflow.core.model.enum.GoalPeriodType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoalEditorScreen(
    uiState: GoalEditorUiState,
    onNameChanged: (String) -> Unit,
    onTargetValueChanged: (String) -> Unit,
    onUnitChanged: (String) -> Unit,
    onPeriodSelected: (GoalPeriodType) -> Unit,
    onSaveClick: () -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Tự động quay lại màn hình danh sách khi lưu thành công
    LaunchedEffect(uiState.isSaved) {
        if (uiState.isSaved) {
            onBackClick()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(if (uiState.id == null) "Tạo mục tiêu" else "Sửa mục tiêu")
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Quay lại"
                        )
                    }
                }
            )
        },
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Tên mục tiêu
            OutlinedTextField(
                value = uiState.name,
                onValueChange = onNameChanged,
                label = { Text("Tên mục tiêu") },
                placeholder = { Text("Ví dụ: Đọc sách, Chạy bộ...") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // Chỉ tiêu & Đơn vị
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = uiState.targetValue,
                    onValueChange = onTargetValueChanged,
                    label = { Text("Chỉ tiêu") },
                    placeholder = { Text("30") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )

                OutlinedTextField(
                    value = uiState.unit,
                    onValueChange = onUnitChanged,
                    label = { Text("Đơn vị") },
                    placeholder = { Text("lần, km, trang...") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
            }

            // Chọn chu kỳ
            var expanded by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded }
            ) {
                OutlinedTextField(
                    value = when (uiState.selectedPeriod) {
                        GoalPeriodType.WEEKLY -> "Hằng tuần"
                        GoalPeriodType.MONTHLY -> "Hằng tháng"
                        GoalPeriodType.CUSTOM -> "Tùy chỉnh"
                    },
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Chu kỳ") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                )

                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    GoalPeriodType.entries.forEach { period ->
                        DropdownMenuItem(
                            text = {
                                Text(
                                    when (period) {
                                        GoalPeriodType.WEEKLY -> "Hằng tuần"
                                        GoalPeriodType.MONTHLY -> "Hằng tháng"
                                        GoalPeriodType.CUSTOM -> "Tùy chỉnh"
                                    }
                                )
                            },
                            onClick = {
                                onPeriodSelected(period)
                                expanded = false
                            }
                        )
                    }
                }
            }

            // Hiển thị thông báo lỗi nếu có
            if (uiState.errorMessage != null) {
                Text(
                    text = uiState.errorMessage,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            // Nút Lưu
            Button(
                onClick = onSaveClick,
                enabled = !uiState.isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                } else {
                    Text("Lưu mục tiêu")
                }
            }
        }
    }
}