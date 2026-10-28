package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "subjects")
data class SubjectEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val code: String = "",
    val teacher: String = "",
    val classroom: String = "",
    val colorHex: String = "#8B5CF6",
    val targetWeeklyHours: Double = 5.0
)

@Entity(tableName = "timetable_slots")
data class TimetableSlotEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val subjectId: Long,
    val subjectName: String,
    val dayOfWeek: Int, // 1 = Monday, ..., 7 = Sunday
    val startTime: String, // HH:mm
    val endTime: String, // HH:mm
    val room: String = "",
    val teacher: String = ""
)

@Entity(tableName = "assignments")
data class AssignmentEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val subjectId: Long,
    val subjectName: String,
    val title: String,
    val description: String = "",
    val dueDate: String, // YYYY-MM-DD
    val isSubmitted: Boolean = false,
    val marksOrGrade: String = ""
)

@Entity(tableName = "exams")
data class ExamEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val subjectId: Long,
    val subjectName: String,
    val title: String,
    val examDate: String, // YYYY-MM-DD
    val startTime: String = "10:00",
    val room: String = "",
    val syllabusNotes: String = ""
)

@Entity(tableName = "study_sessions")
data class StudySessionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val subjectId: Long,
    val subjectName: String,
    val date: String, // YYYY-MM-DD
    val plannedMinutes: Int,
    val actualMinutes: Int,
    val notes: String = "",
    val isCompleted: Boolean = true
)
