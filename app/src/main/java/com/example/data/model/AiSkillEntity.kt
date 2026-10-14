package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "ai_skills")
data class AiSkillEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val description: String,
    val iconType: String = "music", // "music", "pen", "tune", "text", "other"
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)
