package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val description: String = "",
    val date: String, // YYYY-MM-DD
    val startTime: String = "", // HH:mm
    val endTime: String = "", // HH:mm
    val category: String = "Personal", // Personal, College, Study, Work, Money, Health, Family, Shopping, Travel, Other
    val priority: String = "Medium", // Low, Medium, High, Urgent
    val status: String = "Not Started", // Not Started, In Progress, Completed, Cancelled
    val location: String = "",
    val checklistJson: String = "[]", // [{"title":"Subtask","done":false}]
    val notes: String = "",
    val estimatedMinutes: Int = 0,
    val actualMinutes: Int = 0,
    val amount: Double = 0.0,
    val recurrence: String = "None", // None, Daily, Weekly, Monthly, Yearly
    val isCompleted: Boolean = false,
    val reminderTime: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
