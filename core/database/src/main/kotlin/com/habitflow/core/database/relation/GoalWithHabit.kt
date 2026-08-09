package com.habitflow.core.database.relation

import androidx.room.Embedded
import androidx.room.Relation
import com.habitflow.core.database.entity.GoalEntity

data class GoalWithHabit(
    @Embedded
    val goal: GoalEntity,
    val habitIds: List<String> = emptyList()
)