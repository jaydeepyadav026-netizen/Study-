package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "contacts")
data class ContactEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val phone: String = "",
    val email: String = "",
    val relationship: String = "Friend", // Family, Friend, College, Work, Other
    val birthday: String = "", // YYYY-MM-DD
    val notes: String = ""
)

@Entity(tableName = "focus_logs")
data class FocusLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val taskTitle: String,
    val durationMinutes: Int,
    val completedAt: Long = System.currentTimeMillis(),
    val mode: String = "Pomodoro" // Pomodoro, Deep Work, Revision
)

@Entity(tableName = "user_preferences")
data class UserPreferenceEntity(
    @PrimaryKey val key: String,
    val value: String
)
