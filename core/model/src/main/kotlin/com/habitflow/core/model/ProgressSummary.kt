package com.habitflow.core.model

data class ProgressSummary(
    val goalId: String,
    val goalName: String,
    val currentProgress: Double,
    val targetValue: Double,
    val unit: String,
    val percentage: Double,
    val isAchieved: Boolean
)