package com.habitflow.app

import java.time.LocalDate

object HabitStatisticsCalculator {
    fun calculate(items: List<OccurrenceEntity>, todayEpochDay: Long): HabitStats {
        val ordered = items.sortedBy { it.scheduledEpochDay }
        val completed = ordered.count { it.status == OccurrenceStatus.COMPLETED }
        val missed = ordered.count { it.status == OccurrenceStatus.MISSED }
        val skipped = ordered.count { it.status == OccurrenceStatus.SKIPPED }
        val frozen = ordered.count { it.status == OccurrenceStatus.FROZEN }
        
        var running = 0
        var longest = 0
        ordered.filter { it.status != OccurrenceStatus.PENDING }.forEach {
            when (it.status) {
                OccurrenceStatus.COMPLETED -> { 
                    running++
                    longest = maxOf(longest, running) 
                }
                OccurrenceStatus.MISSED -> running = 0
                OccurrenceStatus.SKIPPED, OccurrenceStatus.FROZEN, OccurrenceStatus.PENDING -> Unit
            }
        }
        
        var current = 0
        for (item in ordered.asReversed()) {
            when (item.status) {
                OccurrenceStatus.COMPLETED -> current++
                OccurrenceStatus.SKIPPED, OccurrenceStatus.FROZEN -> Unit
                OccurrenceStatus.MISSED -> break
                OccurrenceStatus.PENDING -> Unit
            }
        }
        
        val totalConsidered = completed + missed
        val completionRate = if (totalConsidered == 0) 0.0 else completed * 100.0 / totalConsidered

        // Weekly and Monthly rates
        val last7Days = ordered.filter { it.scheduledEpochDay > todayEpochDay - 7 }
        val last30Days = ordered.filter { it.scheduledEpochDay > todayEpochDay - 30 }
        
        fun calculateRate(list: List<OccurrenceEntity>): Double {
            val c = list.count { it.status == OccurrenceStatus.COMPLETED }
            val m = list.count { it.status == OccurrenceStatus.MISSED }
            return if (c + m == 0) 0.0 else c * 100.0 / (c + m)
        }

        return HabitStats(
            completed = completed,
            missed = missed,
            skipped = skipped,
            frozen = frozen,
            currentStreak = current,
            longestStreak = longest,
            completionRate = completionRate,
            weeklyCompletionRate = calculateRate(last7Days),
            monthlyCompletionRate = calculateRate(last30Days)
        )
    }
}
