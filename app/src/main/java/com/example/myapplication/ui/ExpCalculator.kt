package com.example.myapplication.utils

import com.example.myapplication.data.Task

object ExpCalculator {
    // Base EXP values for each priority level
    private const val LOW_PRIORITY_BASE = 10
    private const val MEDIUM_PRIORITY_BASE = 20
    private const val HIGH_PRIORITY_BASE = 35

    fun calculateCompletionExp(task: Task): Int {
        return when (task.priority) {
            1L -> LOW_PRIORITY_BASE
            2L -> MEDIUM_PRIORITY_BASE
            3L -> HIGH_PRIORITY_BASE
            else -> LOW_PRIORITY_BASE
        }
    }

    fun calculatePenalty(task: Task): Int {
        return -(calculateCompletionExp(task) / 2)
    }

    fun calculateStreakBonus(baseExp: Int, streakDays: Int): Int {
        val bonusMultiplier = (streakDays / 7) * 0.1
        return (baseExp * bonusMultiplier).toInt()
    }
}