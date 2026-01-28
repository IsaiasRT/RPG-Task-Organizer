package com.example.myapplication.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.Task
import com.example.myapplication.repository.TaskRepository
import com.example.myapplication.repository.ProfileRepository
import com.example.myapplication.repository.HistoryRepository
import com.example.myapplication.repository.AchievementRepository
import com.example.myapplication.data.TaskHistory
import com.example.myapplication.data.HistoryStatus
import com.example.myapplication.utils.ExpCalculator
import kotlinx.coroutines.launch

class TaskViewModel(
    private val taskRepository: TaskRepository,
    private val profileRepository: ProfileRepository,
    private val historyRepository: HistoryRepository,
    private val achievementRepository: AchievementRepository
) : ViewModel() {

    // Task data
    val allTasks = taskRepository.allTasks

    // Profile data
    val userProfile = profileRepository.userProfile

    // History data
    val allHistory = historyRepository.allHistory
    val completedHistory = historyRepository.completedHistory
    val failedHistory = historyRepository.failedHistory
    val deletedHistory = historyRepository.deletedHistory

    // Achievement data
    val allAchievements = achievementRepository.allAchievements

    init {
        // Initialize default achievements on first run
        viewModelScope.launch {
            achievementRepository.initializeDefaults()
        }
    }

    fun insertTask(task: Task) = viewModelScope.launch {
        taskRepository.insertTask(task)
    }

    fun updateTask(task: Task) = viewModelScope.launch {
        taskRepository.updateTask(task)
    }

    fun completeTask(task: Task) = viewModelScope.launch {
        val baseExp = ExpCalculator.calculateCompletionExp(task)
        val profile = profileRepository.getUserProfileSync()
        val streakBonus = ExpCalculator.calculateStreakBonus(
            baseExp,
            profile?.streakDays ?: 0
        )
        val totalExp = baseExp + streakBonus

        profileRepository.addExp(totalExp)

        historyRepository.addToHistory(
            TaskHistory(
                taskTitle = task.title,
                taskDescription = task.description,
                priority = task.priority,
                status = HistoryStatus.COMPLETED,
                expEarned = totalExp
            )
        )

        profileRepository.incrementTasksCompleted()
        taskRepository.deleteTask(task)

        // Check for achievement unlocks
        checkAchievements()
    }

    fun deleteTask(task: Task) = viewModelScope.launch {
        if (!task.isCompleted) {
            val penalty = ExpCalculator.calculatePenalty(task)
            profileRepository.addExp(penalty)

            historyRepository.addToHistory(
                TaskHistory(
                    taskTitle = task.title,
                    taskDescription = task.description,
                    priority = task.priority,
                    status = HistoryStatus.DELETED,
                    expEarned = penalty
                )
            )
        }

        taskRepository.deleteTask(task)
    }

    private suspend fun checkAchievements() {
        val profile = profileRepository.getUserProfileSync() ?: return

        achievementRepository.checkAndUnlockAchievements(
            tasksCompleted = profile.totalTasksCompleted,
            streakDays = profile.streakDays,
            level = profile.level,
            highPriorityCompleted = 0 // You'd track this separately
        )
    }

    suspend fun getTotalExpEarned(): Int {
        return historyRepository.getTotalExpEarned()
    }
}