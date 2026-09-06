package com.habitflow.core.database.model

import androidx.room.Embedded
import com.habitflow.core.database.entity.GoalEntity

data class GoalWithHabit(
    @Embedded val goal: GoalEntity,
    val habitName: String = "Thói quen liên kết"
)