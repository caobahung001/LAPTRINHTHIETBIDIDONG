package com.habitflow.app

import java.time.LocalDate
import kotlin.math.sqrt

object GamificationManager {
    const val XP_PER_COMPLETION = 10L
    const val XP_STREAK_BONUS = 5L
    const val STREAK_FREEZE_AWARD_DAYS = 7L

    fun calculateLevel(xp: Long): Int {
        return (sqrt(xp.toDouble() / 100.0).toInt() + 1).coerceAtLeast(1)
    }

    fun getXpForNextLevel(currentLevel: Int): Long {
        return (currentLevel * currentLevel * 100).toLong()
    }

    /**
     * Logic to process XP and Streak Freezes when a habit is completed.
     */
    suspend fun processCompletion(
        repository: HabitRepository,
        currentStreak: Int
    ) {
        val stats = repository.getUserStats()
        
        // Award XP
        val bonus = if (currentStreak > 0 && currentStreak % 5 == 0) XP_STREAK_BONUS else 0L
        val newXp = stats.xp + XP_PER_COMPLETION + bonus
        val newLevel = calculateLevel(newXp)
        
        // Award Streak Freeze card every 7 days of streak (if not already awarded today)
        var newStreakFreezes = stats.streakFreezes
        val today = LocalDate.now().toEpochDay()
        if (currentStreak > 0 && currentStreak % STREAK_FREEZE_AWARD_DAYS.toInt() == 0 && stats.lastAwardedStreakFreezeEpochDay != today) {
            newStreakFreezes += 1
        }
        
        repository.updateUserStats(stats.copy(
            xp = newXp,
            level = newLevel,
            streakFreezes = newStreakFreezes,
            lastAwardedStreakFreezeEpochDay = if (newStreakFreezes > stats.streakFreezes) today else stats.lastAwardedStreakFreezeEpochDay
        ))
    }
    
    suspend fun useStreakFreeze(repository: HabitRepository): Boolean {
        val stats = repository.getUserStats()
        if (stats.streakFreezes > 0) {
            repository.updateUserStats(stats.copy(streakFreezes = stats.streakFreezes - 1))
            return true
        }
        return false
    }
}
