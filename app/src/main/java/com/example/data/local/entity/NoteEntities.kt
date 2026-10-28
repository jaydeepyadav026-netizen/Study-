package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notes")
data class NoteEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val content: String,
    val category: String = "General", // General, Ideas, Plans, Learning, Reference
    val tagsJson: String = "[]",
    val isPinned: Boolean = false,
    val isFavorite: Boolean = false,
    val isArchived: Boolean = false,
    val isPrivate: Boolean = false,
    val excludeFromAi: Boolean = false,
    val subjectId: Long? = null,
    val taskId: Long? = null,
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "diary_entries")
data class DiaryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val date: String, // YYYY-MM-DD
    val title: String,
    val content: String,
    val mood: String = "😊 Great", // 😊 Great, 😌 Good, 😐 Neutral, 😔 Down, 😤 Stressed
    val tagsJson: String = "[]",
    val updatedAt: Long = System.currentTimeMillis()
)
