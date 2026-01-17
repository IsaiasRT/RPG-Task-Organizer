package com.example.myapplication.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "tasks")

data class Task(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val description: String,
    val priority: Long = 1, //1 = low, 2 = medium and 3 = high
    val isCompleted: Boolean = false,
    val createdAt: Long = Date().time
) {
    fun getPriorityColor(): Long {
        return when(priority){
            3L -> 0xFFFF5252 // red for high
            2L -> 0xFFFF874 //orange for medium
            else -> 0xFF4CAF50 //green for low
        }

    }

    fun getPriorityText(): String {
        return when(priority){
            3L -> "High"
            2L -> "Medium"
            else -> "Low"
        }
    }

}

