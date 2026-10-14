package com.example.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.AiSkillEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SkillDao {
    @Query("SELECT * FROM ai_skills ORDER BY id ASC")
    fun getAllSkills(): Flow<List<AiSkillEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSkill(skill: AiSkillEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(skills: List<AiSkillEntity>)

    @Update
    suspend fun updateSkill(skill: AiSkillEntity)

    @Delete
    suspend fun deleteSkill(skill: AiSkillEntity)

    @Query("SELECT COUNT(*) FROM ai_skills")
    suspend fun getSkillCount(): Int
}
