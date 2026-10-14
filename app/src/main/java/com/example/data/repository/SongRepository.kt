package com.example.data.repository

import com.example.data.db.SkillDao
import com.example.data.db.SongDao
import com.example.data.model.AiSkillEntity
import com.example.data.model.SongProject
import kotlinx.coroutines.flow.Flow

class SongRepository(
    private val songDao: SongDao,
    private val skillDao: SkillDao
) {
    val allProjects: Flow<List<SongProject>> = songDao.getAllProjects()
    val createdProjects: Flow<List<SongProject>> = songDao.getCreatedProjects()
    val savedProjects: Flow<List<SongProject>> = songDao.getSavedProjects()
    val allSkills: Flow<List<AiSkillEntity>> = skillDao.getAllSkills()

    suspend fun getProjectById(id: Long): SongProject? = songDao.getProjectById(id)

    suspend fun saveProject(project: SongProject): Long = songDao.insertProject(project)

    suspend fun updateProject(project: SongProject) = songDao.updateProject(project)

    suspend fun deleteProject(project: SongProject) = songDao.deleteProject(project)

    suspend fun toggleSaveProject(project: SongProject) {
        val updated = project.copy(isSaved = !project.isSaved)
        songDao.updateProject(updated)
    }

    suspend fun addSkill(skill: AiSkillEntity): Long = skillDao.insertSkill(skill)

    suspend fun updateSkill(skill: AiSkillEntity) = skillDao.updateSkill(skill)

    suspend fun deleteSkill(skill: AiSkillEntity) = skillDao.deleteSkill(skill)
}
