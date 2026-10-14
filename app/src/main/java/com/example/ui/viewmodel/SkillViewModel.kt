package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.db.AppDatabase
import com.example.data.model.AiSkillEntity
import com.example.data.repository.SongRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SkillViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getDatabase(application)
    private val repository = SongRepository(database.songDao(), database.skillDao())

    val skills: StateFlow<List<AiSkillEntity>> = repository.allSkills
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _addSkillMode = MutableStateFlow(0) // 0: By Description, 1: By Code
    val addSkillMode: StateFlow<Int> = _addSkillMode.asStateFlow()

    private val _newSkillDescription = MutableStateFlow("")
    val newSkillDescription: StateFlow<String> = _newSkillDescription.asStateFlow()

    fun setAddSkillMode(mode: Int) {
        _addSkillMode.value = mode
    }

    fun updateSkillDescription(desc: String) {
        _newSkillDescription.value = desc
    }

    fun addSkill(name: String, desc: String, iconType: String = "tune", onComplete: () -> Unit) {
        if (desc.isBlank()) return
        viewModelScope.launch {
            val skillName = name.ifBlank {
                if (desc.length > 20) desc.take(20) + "..." else desc
            }
            repository.addSkill(
                AiSkillEntity(
                    name = skillName,
                    description = desc,
                    iconType = iconType,
                    isActive = true
                )
            )
            _newSkillDescription.value = ""
            onComplete()
        }
    }

    fun toggleSkillActive(skill: AiSkillEntity) {
        viewModelScope.launch {
            repository.updateSkill(skill.copy(isActive = !skill.isActive))
        }
    }
}
