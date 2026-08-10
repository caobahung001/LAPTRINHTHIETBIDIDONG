# Implementation Plan - Gamification and Motivation Features

This plan outlines the integration of gamification elements (XP, Levels, Streak Freezes) and enhanced analytics into the HabitFlow app.

## User Review Required

> [!IMPORTANT]
> **Database Migration**: This requires a database version bump (v3 to v4). I will use `fallbackToDestructiveMigration()` as per current configuration, which will clear existing data. If you have important data, please export it first using the Settings tab.

> [!NOTE]
> **Gamification Balance**: Initial XP is set to 10 per completion. Streak Freezes are awarded every 7 days of a continuous streak.

## Proposed Changes

### 1. Data Models & Persistence
Update the schema to support new statuses and user statistics.

#### [MODIFY] [Models.kt](file:///C:/Users/lieuv/OneDrive/Tài liệu/filehoctap/LapTrinhAndroid/LAPTRINHTHIETBIDIDONG/Code/HabitFlow/app/src/main/kotlin/com/habitflow/app/Models.kt)
- Add `FROZEN` to `OccurrenceStatus`.
- Add `UserStatsEntity` to track `xp`, `level`, and `streakFreezes`.
- Update `HabitStats` to include `weeklyCompletionRate` and `monthlyCompletionRate`.

#### [MODIFY] [Database.kt](file:///C:/Users/lieuv/OneDrive/Tài liệu/filehoctap/LapTrinhAndroid/LAPTRINHTHIETBIDIDONG/Code/HabitFlow/app/src/main/kotlin/com/habitflow/app/Database.kt)
- Add `UserStatsDao`.
- Add `UserStatsEntity` to the `HabitFlowDatabase` entities list and bump version to 4.

### 2. Logic & Analytics
Implement the rules for gamification and advanced statistics.

#### [NEW] `GamificationManager.kt`
- Logic to award XP (10 per completion, bonus for streaks).
- Level calculation logic (e.g., `level = sqrt(xp/100) + 1`).
- Logic for awarding and using Streak Freezes.

#### [MODIFY] [Calculators.kt](file:///C:/Users/lieuv/OneDrive/Tài liệu/filehoctap/LapTrinhAndroid/LAPTRINHTHIETBIDIDONG/Code/HabitFlow/app/src/main/kotlin/com/habitflow/app/Calculators.kt)
- Update streak calculation to treat `FROZEN` as a "bridge" that doesn't break the streak.
- Implement weekly and monthly completion rate calculations.

### 3. UI Implementation
Surface the new features to the user.

#### [MODIFY] [MainActivity.kt](file:///C:/Users/lieuv/OneDrive/Tài liệu/filehoctap/LapTrinhAndroid/LAPTRINHTHIETBIDIDONG/Code/HabitFlow/app/src/main/kotlin/com/habitflow/app/MainActivity.kt)
- **Today Screen**: Add a header showing Level, XP progress bar, and Streak Freeze count. Add a button to use a Freeze on a pending habit.
- **Statistics Screen**: Display weekly and monthly completion rates.

## Verification Plan

### Automated Tests
- Unit tests for `GamificationManager` (leveling logic).
- Unit tests for `Calculators` (streak logic with frozen status).

### Manual Verification
1.  Complete a habit and verify XP increases and level-up works.
2.  Maintain a 7-day streak and verify a Streak Freeze card is awarded.
3.  Skip a day using a Streak Freeze and verify the streak count is preserved.
4.  Verify weekly/monthly rates update correctly in the Statistics tab.
