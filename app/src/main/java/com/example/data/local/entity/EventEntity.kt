package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "events")
data class EventEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val description: String = "",
    val date: String, // YYYY-MM-DD
    val startTime: String = "09:00",
    val endTime: String = "10:00",
    val eventType: String = "Personal", // Lecture, Exam, Appointment, Birthday, Bill, Repayment, Personal, Milestone
    val location: String = "",
    val isAllDay: Boolean = false,
    val recurrence: String = "None", // None, Daily, Weekly, Monthly, Yearly
    val reminderMinutes: Int = 15,
    val colorHex: String = "#8B5CF6",
    val createdAt: Long = System.currentTimeMillis()
)
