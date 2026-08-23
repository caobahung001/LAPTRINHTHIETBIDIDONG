package com.habitflow.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "goals")
data class GoalEntity(
    @PrimaryKey val id: String,
    val habitId: String,
    val name: String,
    val metricType: String,
    val periodType: String,
    val targetValue: Double,
    val unit: String,
    val startDate: String,
    val endDate: String
)