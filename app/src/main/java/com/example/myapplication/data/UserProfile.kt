package com.example.myapplication.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_profile")
data class UserProfile(
    @PrimaryKey val id: Long = 1, // Only one profile per user
    val username: String = "Player",
    val level: Int = 1,
    val currentExp: Int = 0,
    val totalTasksCompleted: Int = 0,
    val totalTasksFailed: Int = 0,
    val streakDays: Int = 0,
    val lastActivityDate: Long = System.currentTimeMillis()
) {
    // Calculate how much EXP is needed to reach the next level
    // The formula creates an increasing curve: level 2 needs 100 EXP,
    // level 3 needs 150, level 4 needs 200, etc.
    fun expToNextLevel(): Int = 50 + (level * 50)

    // Calculate percentage progress toward next level
    fun levelProgress(): Float = (currentExp.toFloat() / expToNextLevel()) * 100f
}