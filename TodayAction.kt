package com.habitflow.feature.today

sealed interface TodayAction {
    data class Complete(val habitId: Long) : TodayAction
    data class Skip(val habitId: Long) : TodayAction
    data class Undo(val habitId: Long) : TodayAction
    data class AddValue(val habitId: Long, val value: Double, val targetValue: Double) : TodayAction
    data class SelectDate(val dateISO: String) : TodayAction
}
