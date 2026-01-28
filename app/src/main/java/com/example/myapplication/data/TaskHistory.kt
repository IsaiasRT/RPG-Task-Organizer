package com.example.myapplication.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "task_history")
data class TaskHistory(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val taskTitle: String,
    val taskDescription: String,
    val priority: Long,
    val status: HistoryStatus,
    val completedAt: Long = System.currentTimeMillis(),
    val expEarned: Int = 0 // How much EXP was gained/lost
)

enum class HistoryStatus {
    COMPLETED,  // Task was finished successfully
    FAILED,     // Task deadline passed without completion
    DELETED     // User deleted the task
}