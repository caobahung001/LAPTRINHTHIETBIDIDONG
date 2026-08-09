package com.habitflow.feature.goals

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.habitflow.core.domain.model.Goal
import com.habitflow.feature.goals.component.GoalCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoalListScreen() {
    val mockGoals = listOf(
        Goal(id = "1", title = "Đọc 50 trang sách/tuần", targetValue = 50, currentValue = 25, progress = 0.5f),
        Goal(id = "2", title = "Uống 2L nước mỗi ngày", targetValue = 30, currentValue = 21, progress = 0.7f),
        Goal(id = "3", title = "Chạy bộ 10km/tháng", targetValue = 10, currentValue = 10, progress = 1.0f)
    )

    Scaffold(
        topBar = { TopAppBar(title = { Text("Danh sách Mục tiêu (Mock UI)") }) }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(mockGoals) { goal ->
                GoalCard(goal = goal)
            }
        }
    }
}