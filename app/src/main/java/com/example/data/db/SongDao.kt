package com.example.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.SongProject
import kotlinx.coroutines.flow.Flow

@Dao
interface SongDao {
    @Query("SELECT * FROM song_projects ORDER BY createdAt DESC")
    fun getAllProjects(): Flow<List<SongProject>>

    @Query("SELECT * FROM song_projects WHERE isCreated = 1 ORDER BY createdAt DESC")
    fun getCreatedProjects(): Flow<List<SongProject>>

    @Query("SELECT * FROM song_projects WHERE isSaved = 1 ORDER BY createdAt DESC")
    fun getSavedProjects(): Flow<List<SongProject>>

    @Query("SELECT * FROM song_projects WHERE id = :id LIMIT 1")
    suspend fun getProjectById(id: Long): SongProject?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProject(project: SongProject): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(projects: List<SongProject>)

    @Update
    suspend fun updateProject(project: SongProject)

    @Delete
    suspend fun deleteProject(project: SongProject)

    @Query("DELETE FROM song_projects WHERE id = :id")
    suspend fun deleteProjectById(id: Long)

    @Query("SELECT COUNT(*) FROM song_projects")
    suspend fun getProjectCount(): Int
}
