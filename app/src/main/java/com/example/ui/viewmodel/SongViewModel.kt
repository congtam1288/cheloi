package com.example.ui.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.audio.AudioPlaybackManager
import com.example.data.db.AppDatabase
import com.example.data.model.LyricSegment
import com.example.data.model.SongProject
import com.example.data.repository.AiRepository
import com.example.data.repository.SongRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class ComposeStep {
    STEP_01_INPUT,
    STEP_02_AUDIO_ANALYSIS,
    STEP_03_WRITE_NEW,
    STEP_04_STYLE_SELECTION,
    STEP_05_SONG_COMPLETE,
    STEP_06_PRODUCTION_GUIDE
}

data class SongStyleConfig(
    val genre: String = "Pop Ballad",
    val vocalStyle: String = "Nữ nhẹ nhàng",
    val subGenre: String = "Acoustic Ballad",
    val tempo: String = "Trung bình",
    val emotion: String = "Lãng mạn",
    val theme: String = "Tình yêu",
    val structure: String = "Verse - Chorus",
    val instruments: String = "Guitar, Piano, Violin",
    val arrangement: String = "Hiện đại, acoustic",
    val energy: String = "Dịu dàng, nhẹ nhàng"
)

class SongViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getDatabase(application)
    val songRepository = SongRepository(database.songDao(), database.skillDao())
    val aiRepository = AiRepository()
    val audioManager = AudioPlaybackManager(application)

    private val _currentStep = MutableStateFlow(ComposeStep.STEP_01_INPUT)
    val currentStep: StateFlow<ComposeStep> = _currentStep.asStateFlow()

    private val _originalLyrics = MutableStateFlow(
        "Từng ngày trôi qua, anh vẫn nhớ...\nDù cho mưa gió, anh vẫn chờ...\nVì trong tim anh, em là tất cả...\nDù xa cách, anh vẫn luôn...\nTình yêu này, sẽ không phai..."
    )
    val originalLyrics: StateFlow<String> = _originalLyrics.asStateFlow()

    private val _audioFileName = MutableStateFlow("original_song.mp3")
    val audioFileName: StateFlow<String> = _audioFileName.asStateFlow()

    private val _audioDuration = MutableStateFlow("03:45")
    val audioDuration: StateFlow<String> = _audioDuration.asStateFlow()

    private val _segments = MutableStateFlow<List<LyricSegment>>(emptyList())
    val segments: StateFlow<List<LyricSegment>> = _segments.asStateFlow()

    private val _styleConfig = MutableStateFlow(SongStyleConfig())
    val styleConfig: StateFlow<SongStyleConfig> = _styleConfig.asStateFlow()

    // Active song result
    private val _completedSong = MutableStateFlow<SongProject?>(null)
    val completedSong: StateFlow<SongProject?> = _completedSong.asStateFlow()

    // Suggestion modal state
    private val _suggestionModalSegment = MutableStateFlow<LyricSegment?>(null)
    val suggestionModalSegment: StateFlow<LyricSegment?> = _suggestionModalSegment.asStateFlow()

    private val _currentSuggestions = MutableStateFlow<List<String>>(emptyList())
    val currentSuggestions: StateFlow<List<String>> = _currentSuggestions.asStateFlow()

    private val _selectedSuggestionIndex = MutableStateFlow(0)
    val selectedSuggestionIndex: StateFlow<Int> = _selectedSuggestionIndex.asStateFlow()

    private val _isLoadingSuggestions = MutableStateFlow(false)
    val isLoadingSuggestions: StateFlow<Boolean> = _isLoadingSuggestions.asStateFlow()

    init {
        // Prepare initial segments
        parseLyricsToSegments(_originalLyrics.value)
    }

    fun updateOriginalLyrics(newText: String) {
        _originalLyrics.value = newText
    }

    fun setCustomAudioFile(uri: Uri, name: String) {
        _audioFileName.value = name
        audioManager.setCustomAudio(uri, name)
    }

    fun clearAudioFile() {
        _audioFileName.value = "original_song.mp3"
        audioManager.clearAudio()
    }

    fun analyzeAudioAndLyrics() {
        parseLyricsToSegments(_originalLyrics.value)
        _currentStep.value = ComposeStep.STEP_02_AUDIO_ANALYSIS
    }

    private fun parseLyricsToSegments(lyrics: String) {
        val lines = lyrics.lines().map { it.trim() }.filter { it.isNotBlank() }
        val defaultLines = if (lines.isNotEmpty()) lines else listOf(
            "Từng ngày trôi qua, anh vẫn nhớ...",
            "Dù cho mưa gió, anh vẫn chờ...",
            "Vì trong tim anh, em là tất cả...",
            "Dù xa cách, anh vẫn luôn...",
            "Tình yêu này, sẽ không phai..."
        )

        val newLinesDefault = listOf(
            "Mỗi sớm mai thức dậy, anh vẫn mơ...",
            "Dù phong ba, anh vẫn tin vào...",
            "Vì trong tim anh, em là ánh sáng...",
            "Dù xa cách, em vẫn luôn...",
            "Tình yêu này, trọn kiếp sau..."
        )

        val timeStamps = listOf(
            Pair("00:12", "00:18"),
            Pair("00:18", "00:25"),
            Pair("00:25", "00:32"),
            Pair("00:32", "00:40"),
            Pair("00:40", "00:48"),
            Pair("00:48", "00:56"),
            Pair("00:56", "01:04"),
            Pair("01:04", "01:12")
        )

        val parsed = defaultLines.mapIndexed { idx, line ->
            val times = timeStamps.getOrElse(idx) { Pair("01:${15 + idx * 8}", "01:${23 + idx * 8}") }
            val newText = newLinesDefault.getOrElse(idx) { line }
            LyricSegment(
                index = idx + 1,
                startTime = times.first,
                endTime = times.second,
                originalText = line,
                newText = newText
            )
        }
        _segments.value = parsed
    }

    fun updateSegmentText(index: Int, newText: String) {
        _segments.value = _segments.value.map {
            if (it.index == index) it.copy(newText = newText) else it
        }
    }

    fun openSuggestionsForSegment(segment: LyricSegment, apiKey: String = "") {
        _suggestionModalSegment.value = segment
        _selectedSuggestionIndex.value = 0
        _isLoadingSuggestions.value = true

        viewModelScope.launch {
            val suggestions = aiRepository.getSuggestionsForLine(
                originalLine = segment.originalText,
                theme = _styleConfig.value.theme,
                genre = _styleConfig.value.genre,
                apiKey = apiKey
            )
            _currentSuggestions.value = suggestions
            _isLoadingSuggestions.value = false
        }
    }

    fun selectSuggestionIndex(idx: Int) {
        _selectedSuggestionIndex.value = idx
    }

    fun applySelectedSuggestion() {
        val segment = _suggestionModalSegment.value ?: return
        val suggestions = _currentSuggestions.value
        val chosen = suggestions.getOrNull(_selectedSuggestionIndex.value)
        if (chosen != null) {
            updateSegmentText(segment.index, chosen)
        }
        closeSuggestionModal()
    }

    fun closeSuggestionModal() {
        _suggestionModalSegment.value = null
        _currentSuggestions.value = emptyList()
    }

    fun updateStyleConfig(newConfig: SongStyleConfig) {
        _styleConfig.value = newConfig
    }

    fun generateNewSong() {
        val config = _styleConfig.value
        val fullOriginal = _segments.value.joinToString("\n") { it.originalText }
        val fullNew = _segments.value.joinToString("\n") { it.newText }

        val formattedLyrics = buildString {
            append("VERSE 1\n")
            _segments.value.take(2).forEach { append(it.newText).append("\n") }
            append("Dù cuộc sống có bao đổi thay\nBếp nhà vẫn là nơi bình yên nhất.\n\n")
            append("CHORUS\n")
            append("Bếp ấm yêu thương, là nơi ta trở về\nCùng nhau nấu nướng, cùng nhau sẻ chia\nNhững bữa cơm giản dị, đầy ắp tiếng cười\nLà hạnh phúc, là yêu thương, là nhà.\n\n")
            append("VERSE 2\n")
            _segments.value.drop(2).take(2).forEach { append(it.newText).append("\n") }
        }

        val prodGuide = aiRepository.generateProductionGuide(
            genre = config.genre,
            vocalStyle = config.vocalStyle,
            subGenre = config.subGenre,
            tempo = config.tempo,
            emotion = config.emotion,
            theme = config.theme,
            structure = config.structure,
            instruments = config.instruments,
            arrangement = config.arrangement,
            energy = config.energy
        )

        val project = SongProject(
            title = "Bếp Ấm Yêu Thương",
            genre = config.genre,
            subGenre = config.subGenre,
            vocalStyle = config.vocalStyle,
            tempo = config.tempo,
            emotion = config.emotion,
            theme = config.theme,
            structure = config.structure,
            instruments = config.instruments,
            arrangement = config.arrangement,
            energy = config.energy,
            duration = _audioDuration.value,
            originalLyrics = fullOriginal,
            newLyrics = formattedLyrics,
            productionGuide = prodGuide,
            imageUrl = "https://lh3.googleusercontent.com/aida-public/AB6AXuCYDkIjTpqH2PJcCKAFznhl2i-IojN-6TYjrrhCMB13ySDY-dE3rTotRHV515H4kQr1rEjK9yJvtNhp0TlxtJlsdlYeKaes-_91GLYPO88S1fc0VI-tYREAnahYjOj0L4S-5iDjzK6JawPhO2BoVEDb3mTChZdeQ49sMUQAchmbfLkJ4OhQFfZ4aOKfx7eMOYI09wsXhMQJG-UtnHimYssjfsxF1v-H7AWH5CQ4kqlfp0oo9rvbcPBvEw",
            isSaved = true
        )

        _completedSong.value = project

        viewModelScope.launch {
            songRepository.saveProject(project)
        }

        _currentStep.value = ComposeStep.STEP_05_SONG_COMPLETE
    }

    fun toggleFavoriteCurrentSong() {
        _completedSong.value?.let { current ->
            val updated = current.copy(isSaved = !current.isSaved)
            _completedSong.value = updated
            viewModelScope.launch {
                songRepository.updateProject(updated)
            }
        }
    }

    fun navigateToStep(step: ComposeStep) {
        _currentStep.value = step
    }

    fun loadExistingProject(project: SongProject) {
        _completedSong.value = project
        _originalLyrics.value = project.originalLyrics
        _styleConfig.value = SongStyleConfig(
            genre = project.genre,
            vocalStyle = project.vocalStyle,
            subGenre = project.subGenre,
            tempo = project.tempo,
            emotion = project.emotion,
            theme = project.theme,
            structure = project.structure,
            instruments = project.instruments,
            arrangement = project.arrangement,
            energy = project.energy
        )
        parseLyricsToSegments(project.originalLyrics)
        _currentStep.value = ComposeStep.STEP_05_SONG_COMPLETE
    }
}
