package com.habitflow.core.domain.calculator

import com.habitflow.core.model.Goal
import com.habitflow.core.model.enum.GoalMetricType
import com.habitflow.core.model.enum.GoalPeriodType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class GoalProgressCalculatorTest {

    private lateinit var calculator: GoalProgressCalculator

    @Before
    fun setUp() {
        calculator = GoalProgressCalculator()
    }

    @Test
    fun calculate_metricTypeCount_returnsCorrectProgress() {
        // 1. Arrange: Tạo Goal đếm số lần (COUNT)
        val goal = Goal(
            id = "goal_1",
            habitId = "habit_1",
            name = "Đọc sách",
            metricType = GoalMetricType.COUNT,
            periodType = GoalPeriodType.WEEKLY,
            targetValue = 10.0,
            unit = "lần",
            startEpochDay = 1000L,
            endEpochDay = 1007L
        )

        // 2. Act: Thực hiện 5 lần
        val summary = calculator.calculate(goal = goal, completedCount = 5)

        // 3. Assert: Kiểm tra kết quả
        assertEquals(5.0, summary.currentProgress, 0.01)
        assertEquals(50.0, summary.percentage, 0.01)
        assertFalse(summary.isAchieved)
    }

    @Test
    fun calculate_metricTypeValue_returnsCorrectProgress() {
        // 1. Arrange: Tạo Goal tích lũy giá trị (VALUE)
        val goal = Goal(
            id = "goal_2",
            habitId = "habit_2",
            name = "Chạy bộ",
            metricType = GoalMetricType.VALUE,
            periodType = GoalPeriodType.MONTHLY,
            targetValue = 50.0,
            unit = "km",
            startEpochDay = 1000L,
            endEpochDay = 1030L
        )

        // 2. Act: Đã chạy 25.5 km
        val summary = calculator.calculate(goal = goal, accumulatedValue = 25.5)

        // 3. Assert: Kiểm tra kết quả
        assertEquals(25.5, summary.currentProgress, 0.01)
        assertEquals(51.0, summary.percentage, 0.01)
        assertFalse(summary.isAchieved)
    }

    @Test
    fun calculate_exceedTargetValue_capsPercentageAt100AndSetsAchieved() {
        // 1. Arrange: Goal chỉ tiêu 10 lần
        val goal = Goal(
            id = "goal_3",
            habitId = "habit_3",
            name = "Uống nước",
            metricType = GoalMetricType.COUNT,
            periodType = GoalPeriodType.WEEKLY,
            targetValue = 10.0,
            unit = "lần",
            startEpochDay = 1000L,
            endEpochDay = 1007L
        )

        // 2. Act: Thực hiện vượt chỉ tiêu (12 lần)
        val summary = calculator.calculate(goal = goal, completedCount = 12)

        // 3. Assert: Percentage phải giữ ở mốc max 100.0% và isAchieved = true
        assertEquals(12.0, summary.currentProgress, 0.01)
        assertEquals(100.0, summary.percentage, 0.01)
        assertTrue(summary.isAchieved)
    }

    @Test
    fun calculate_zeroTargetValue_returnsZeroPercentage() {
        // 1. Arrange: Goal với targetValue = 0 (kiểm tra tránh lỗi chia cho 0)
        val goal = Goal(
            id = "goal_4",
            habitId = "habit_4",
            name = "Mục tiêu lỗi",
            metricType = GoalMetricType.COUNT,
            periodType = GoalPeriodType.CUSTOM,
            targetValue = 0.0,
            unit = "lần",
            startEpochDay = 1000L,
            endEpochDay = 1007L
        )

        // 2. Act
        val summary = calculator.calculate(goal = goal, completedCount = 5)

        // 3. Assert
        assertEquals(0.0, summary.percentage, 0.01)
    }
}