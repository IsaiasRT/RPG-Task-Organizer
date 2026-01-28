package com.example.myapplication.data

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface UserProfileDao {
    @Query("SELECT * FROM user_profile WHERE id = 1")
    fun getUserProfile(): LiveData<UserProfile>

    @Query("SELECT * FROM user_profile WHERE id = 1")
    suspend fun getUserProfileSync(): UserProfile?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProfile(profile: UserProfile)

    @Update
    suspend fun updateProfile(profile: UserProfile)

    // Helper function to add EXP and handle level-ups
    @Transaction
    suspend fun addExp(exp: Int) {
        val profile = getUserProfileSync() ?: UserProfile()
        var newExp = profile.currentExp + exp
        var newLevel = profile.level

        // Keep leveling up if we have enough EXP
        while (newExp >= (50 + (newLevel * 50))) {
            newExp -= (50 + (newLevel * 50))
            newLevel++
        }

        updateProfile(
            profile.copy(
                currentExp = newExp,
                level = newLevel
            )
        )
    }
}