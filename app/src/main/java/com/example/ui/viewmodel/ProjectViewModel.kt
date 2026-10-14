package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.db.AppDatabase
import com.example.data.model.SongProject
import com.example.data.repository.SongRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class ProjectFilterTab {
    ALL,
    CREATED,
    SAVED
}

class ProjectViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getDatabase(application)
    private val repository = SongRepository(database.songDao(), database.skillDao())

    private val _selectedTab = MutableStateFlow(ProjectFilterTab.ALL)
    val selectedTab: StateFlow<ProjectFilterTab> = _selectedTab.asStateFlow()

    val projects: StateFlow<List<SongProject>> = _selectedTab.flatMapLatest { tab ->
        when (tab) {
            ProjectFilterTab.ALL -> repository.allProjects
            ProjectFilterTab.CREATED -> repository.createdProjects
            ProjectFilterTab.SAVED -> repository.savedProjects
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun selectTab(tab: ProjectFilterTab) {
        _selectedTab.value = tab
    }

    fun deleteProject(project: SongProject) {
        viewModelScope.launch {
            repository.deleteProject(project)
        }
    }

    fun toggleSaveProject(project: SongProject) {
        viewModelScope.launch {
            repository.toggleSaveProject(project)
        }
    }
}
