package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.ui.components.AppBottomNavigation
import com.example.ui.components.MainTab
import com.example.ui.screens.AddSkillScreen
import com.example.ui.screens.ComposeScreen
import com.example.ui.screens.ProjectsScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.SkillsScreen
import com.example.ui.theme.AiMelodyTheme
import com.example.ui.viewmodel.ComposeStep
import com.example.ui.viewmodel.ProjectViewModel
import com.example.ui.viewmodel.SettingsViewModel
import com.example.ui.viewmodel.SkillViewModel
import com.example.ui.viewmodel.SongViewModel

class MainActivity : ComponentActivity() {
    private val songViewModel: SongViewModel by viewModels()
    private val projectViewModel: ProjectViewModel by viewModels()
    private val skillViewModel: SkillViewModel by viewModels()
    private val settingsViewModel: SettingsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AiMelodyTheme {
                MainAppScreen(
                    songViewModel = songViewModel,
                    projectViewModel = projectViewModel,
                    skillViewModel = skillViewModel,
                    settingsViewModel = settingsViewModel
                )
            }
        }
    }
}

@Composable
fun MainAppScreen(
    songViewModel: SongViewModel,
    projectViewModel: ProjectViewModel,
    skillViewModel: SkillViewModel,
    settingsViewModel: SettingsViewModel
) {
    var selectedTab by remember { mutableStateOf(MainTab.COMPOSE) }
    var isAddingSkill by remember { mutableStateOf(false) }

    val currentComposeStep by songViewModel.currentStep.collectAsState()
    val apiKey by settingsViewModel.apiKey.collectAsState()

    // Handle Android system back button
    BackHandler {
        if (isAddingSkill) {
            isAddingSkill = false
        } else if (selectedTab != MainTab.COMPOSE) {
            selectedTab = MainTab.COMPOSE
        } else {
            when (currentComposeStep) {
                ComposeStep.STEP_05_SONG_COMPLETE -> songViewModel.navigateToStep(ComposeStep.STEP_04_STYLE_SELECTION)
                ComposeStep.STEP_04_STYLE_SELECTION -> songViewModel.navigateToStep(ComposeStep.STEP_03_WRITE_NEW)
                ComposeStep.STEP_03_WRITE_NEW -> songViewModel.navigateToStep(ComposeStep.STEP_02_AUDIO_ANALYSIS)
                ComposeStep.STEP_02_AUDIO_ANALYSIS -> songViewModel.navigateToStep(ComposeStep.STEP_01_INPUT)
                ComposeStep.STEP_01_INPUT -> {
                    // System default exit
                }
                else -> songViewModel.navigateToStep(ComposeStep.STEP_01_INPUT)
            }
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            if (!isAddingSkill) {
                AppBottomNavigation(
                    selectedTab = selectedTab,
                    onTabSelected = { tab ->
                        selectedTab = tab
                        isAddingSkill = false
                    }
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (isAddingSkill) {
                AddSkillScreen(
                    viewModel = skillViewModel,
                    onNavigateBack = { isAddingSkill = false }
                )
            } else {
                when (selectedTab) {
                    MainTab.COMPOSE -> ComposeScreen(
                        viewModel = songViewModel,
                        apiKey = apiKey,
                        onNavigateToSettings = { selectedTab = MainTab.SETTINGS },
                        onNavigateToSkills = { selectedTab = MainTab.SKILLS }
                    )
                    MainTab.PROJECTS -> ProjectsScreen(
                        viewModel = projectViewModel,
                        onOpenProject = { project ->
                            songViewModel.loadExistingProject(project)
                            selectedTab = MainTab.COMPOSE
                        },
                        onNewProject = {
                            songViewModel.navigateToStep(ComposeStep.STEP_01_INPUT)
                            selectedTab = MainTab.COMPOSE
                        }
                    )
                    MainTab.SKILLS -> SkillsScreen(
                        viewModel = skillViewModel,
                        onNavigateToAddSkill = { isAddingSkill = true }
                    )
                    MainTab.SETTINGS -> SettingsScreen(
                        settingsViewModel = settingsViewModel,
                        skillViewModel = skillViewModel,
                        onNavigateToAddSkill = { isAddingSkill = true }
                    )
                }
            }
        }
    }
}
