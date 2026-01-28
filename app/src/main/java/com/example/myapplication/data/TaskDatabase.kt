package com.example.myapplication.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        Task::class,           // Your original tasks table
        UserProfile::class,    // New table for user progress and stats
        Achievement::class,    // New table for achievements
        TaskHistory::class     // New table for completed/failed/deleted tasks
    ],
    version = 2,  // Changed from 1 to 2 because we added new tables
    exportSchema = false
)
abstract class TaskDatabase : RoomDatabase() {

    abstract fun taskDao(): TaskDao
    abstract fun userProfileDao(): UserProfileDao
    abstract fun achievementDao(): AchievementDao
    abstract fun taskHistoryDao(): TaskHistoryDao

    companion object {
        @Volatile
        private var INSTANCE: TaskDatabase? = null

        fun getDatabase(context: Context): TaskDatabase {
            // If INSTANCE already exists, return it immediately
            return INSTANCE ?: synchronized(this) {

                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    TaskDatabase::class.java,
                    "task_database"
                )
                    .fallbackToDestructiveMigration()

                    // Add a callback to initialize data when the database is first created
                    .addCallback(DatabaseCallback())

                    .build()

                INSTANCE = instance
                instance
            }
        }
    }

    private class DatabaseCallback : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)

            INSTANCE?.let { database ->
                CoroutineScope(Dispatchers.IO).launch {
                    populateDatabase(database)
                }
            }
        }

        suspend fun populateDatabase(database: TaskDatabase) {
            // Create the initial user profile
            // Every user starts at level 1 with 0 experience
            val userProfileDao = database.userProfileDao()
            val initialProfile = UserProfile(
                id = 1,  // Always ID 1 since there's only one profile per app
                username = "Player",
                level = 1,
                currentExp = 0,
                totalTasksCompleted = 0,
                totalTasksFailed = 0,
                streakDays = 0,
                lastActivityDate = System.currentTimeMillis()
            )
            userProfileDao.insertProfile(initialProfile)

            // These are the goals that users can work toward
            val achievementDao = database.achievementDao()

            val defaultAchievements = listOf(
                Achievement(
                    title = "First Steps",
                    description = "Complete your first task",
                    requirement = 1,
                    type = AchievementType.TASKS_COMPLETED
                ),
                Achievement(
                    title = "Getting Started",
                    description = "Complete 10 tasks",
                    requirement = 10,
                    type = AchievementType.TASKS_COMPLETED
                ),
                Achievement(
                    title = "Task Master",
                    description = "Complete 50 tasks",
                    requirement = 50,
                    type = AchievementType.TASKS_COMPLETED
                ),
                Achievement(
                    title = "Productivity Legend",
                    description = "Complete 100 tasks",
                    requirement = 100,
                    type = AchievementType.TASKS_COMPLETED
                ),
                Achievement(
                    title = "Consistency King",
                    description = "Maintain a 7-day streak",
                    requirement = 7,
                    type = AchievementType.STREAK_DAYS
                ),
                Achievement(
                    title = "Unstoppable Force",
                    description = "Maintain a 30-day streak",
                    requirement = 30,
                    type = AchievementType.STREAK_DAYS
                ),
                Achievement(
                    title = "Priority Master",
                    description = "Complete 20 high-priority tasks",
                    requirement = 20,
                    type = AchievementType.HIGH_PRIORITY
                ),
                Achievement(
                    title = "Rising Star",
                    description = "Reach level 5",
                    requirement = 5,
                    type = AchievementType.LEVEL_REACHED
                ),
                Achievement(
                    title = "Elite Performer",
                    description = "Reach level 10",
                    requirement = 10,
                    type = AchievementType.LEVEL_REACHED
                ),
                Achievement(
                    title = "Legendary Status",
                    description = "Reach level 20",
                    requirement = 20,
                    type = AchievementType.LEVEL_REACHED
                )
            )

            // Insert each achievement into the database
            defaultAchievements.forEach { achievement ->
                achievementDao.insertAchievement(achievement)
            }
        }
    }
}