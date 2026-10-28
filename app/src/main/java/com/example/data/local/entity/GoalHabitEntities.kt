package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "goals")
data class GoalEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val description: String = "",
    val category: String = "Personal", // Personal, Career, Finance, Health, Study
    val targetDate: String = "",
    val progressType: String = "Percent", // Percent, Numeric, Checklist
    val targetValue: Double = 100.0,
    val currentValue: Double = 0.0,
    val status: String = "Active", // Active, Achieved, Paused
    val milestonesJson: String = "[]" // [{"title":"Milestone 1","done":false}]
)

@Entity(tableName = "habits")
data class HabitEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val frequency: String = "Daily", // Daily, Weekly
    val targetDaysPerWeek: Int = 7,
    val colorHex: String = "#06B6D4",
    val currentStreak: Int = 0,
    val bestStreak: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "habit_logs")
data class HabitLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val habitId: Long,
    val date: String, // YYYY-MM-DD
    val isCompleted: Boolean = true
)
