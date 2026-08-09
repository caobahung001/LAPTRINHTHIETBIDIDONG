package com.example.habitflow.core.domain.model

data class Goal(
    val id: String,
    val title: String,
    val targetValue: Int,
    val currentValue: Int = 0,
    val progress: Float = 0f
)