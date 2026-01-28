package com.example.myapplication.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "achievements")
data class Achievement(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val description: String,
    val requirement: Int, // Number needed to unlock (e.g., 10 tasks)
    val type: AchievementType,
    val isUnlocked: Boolean = false,
    val unlockedAt: Long? = null,
    val iconResId: Int = 0 // Reference to drawable resource
)

enum class AchievementType {
    TASKS_COMPLETED,    // Complete X tasks
    STREAK_DAYS,        // Maintain X day streak
    HIGH_PRIORITY,      // Complete X high-priority tasks
    LEVEL_REACHED,      // Reach level X
    PERFECT_WEEK        // Complete all tasks for 7 days
}