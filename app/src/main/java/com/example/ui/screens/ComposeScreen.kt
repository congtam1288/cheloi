package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.LibraryMusic
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.audio.PlaybackState
import com.example.data.model.LyricSegment
import com.example.ui.components.AudioWaveformVisualizer
import com.example.ui.components.DropdownOptionSelector
import com.example.ui.components.PrimaryGradientButton
import com.example.ui.theme.AppBackground
import com.example.ui.theme.BadgeFuchsia
import com.example.ui.theme.BadgeGreen
import com.example.ui.theme.BadgeOrange
import com.example.ui.theme.BadgeRed
import com.example.ui.theme.BadgeTeal
import com.example.ui.theme.BrandBlue
import com.example.ui.theme.BrandBlueDark
import com.example.ui.theme.BrandBlueDeep
import com.example.ui.theme.BrandBlueSoft
import com.example.ui.theme.BrandIndigo
import com.example.ui.theme.BrandPurple
import com.example.ui.theme.CardBorder
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.ComposeStep
import com.example.ui.viewmodel.SongStyleConfig
import com.example.ui.viewmodel.SongViewModel

@Composable
fun ComposeScreen(
    viewModel: SongViewModel,
    apiKey: String,
    onNavigateToSettings: () -> Unit,
    onNavigateToSkills: () -> Unit,
    modifier: Modifier = Modifier
) {
    val currentStep by viewModel.currentStep.collectAsState()
    val playbackState by viewModel.audioManager.playbackState.collectAsState()
    val segments by viewModel.segments.collectAsState()
    val suggestionModalSegment by viewModel.suggestionModalSegment.collectAsState()
    val suggestions by viewModel.currentSuggestions.collectAsState()
    val selectedSuggestionIndex by viewModel.selectedSuggestionIndex.collectAsState()
    val isLoadingSuggestions by viewModel.isLoadingSuggestions.collectAsState()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(AppBackground)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Spacer(modifier = Modifier.height(6.dp))

            when (currentStep) {
                ComposeStep.STEP_01_INPUT -> Step01Input(
                    viewModel = viewModel,
                    onNavigateToSettings = onNavigateToSettings,
                    onNavigateToSkills = onNavigateToSkills
                )
                ComposeStep.STEP_02_AUDIO_ANALYSIS -> Step02AudioAnalysis(
                    viewModel = viewModel,
                    playbackState = playbackState,
                    segments = segments
                )
                ComposeStep.STEP_03_WRITE_NEW -> Step03WriteNew(
                    viewModel = viewModel,
                    playbackState = playbackState,
                    segments = segments,
                    apiKey = apiKey
                )
                ComposeStep.STEP_04_STYLE_SELECTION -> Step04StyleSelection(
                    viewModel = viewModel
                )
                ComposeStep.STEP_05_SONG_COMPLETE, ComposeStep.STEP_06_PRODUCTION_GUIDE -> Step05SongComplete(
                    viewModel = viewModel,
                    playbackState = playbackState
                )
            }

            Spacer(modifier = Modifier.height(80.dp))
        }

        // Suggestion Modal Sheet
        suggestionModalSegment?.let { segment ->
            SuggestionModal(
                segment = segment,
                suggestions = suggestions,
                selectedIndex = selectedSuggestionIndex,
                isLoading = isLoadingSuggestions,
                onSelectIndex = { viewModel.selectSuggestionIndex(it) },
                onApply = { viewModel.applySelectedSuggestion() },
                onDismiss = { viewModel.closeSuggestionModal() }
            )
        }
    }
}

// -------------------------------------------------------------------------------------------------
// STEP 01: NHẬP BÀI HÁT GỐC
// -------------------------------------------------------------------------------------------------
@Composable
private fun Step01Input(
    viewModel: SongViewModel,
    onNavigateToSettings: () -> Unit,
    onNavigateToSkills: () -> Unit
) {
    val context = LocalContext.current
    val originalLyrics by viewModel.originalLyrics.collectAsState()
    val audioFileName by viewModel.audioFileName.collectAsState()
    val audioDuration by viewModel.audioDuration.collectAsState()

    // File audio picker launcher
    val audioPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            val name = uri.lastPathSegment?.substringAfterLast('/') ?: "audio_selected.mp3"
            viewModel.setCustomAudioFile(uri, name)
            Toast.makeText(context, "Đã chọn file: $name", Toast.LENGTH_SHORT).show()
        }
    }

    // App Main Header
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(Color(0xFF2563EB), Color(0xFF38BDF8))
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.MusicNote,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(26.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = "AI VIẾT LỜI",
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Black,
                    color = BrandBlueDeep,
                    lineHeight = 22.sp
                )
                Text(
                    text = "THEO GIAI ĐIỆU",
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Black,
                    color = BrandBlueDeep,
                    lineHeight = 22.sp
                )
            }
        }

        IconButton(onClick = onNavigateToSettings) {
            Icon(
                imageVector = Icons.Outlined.Settings,
                contentDescription = "Cài đặt",
                tint = Color(0xFF94A3B8)
            )
        }
    }

    Text(
        text = "Sáng tác lời mới — Khớp nhịp, giai điệu và âm điệu",
        fontSize = 12.sp,
        color = TextSecondary,
        fontWeight = FontWeight.Normal
    )

    // Main Input Card
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(24.dp))
            .background(Color.White, RoundedCornerShape(24.dp))
            .border(1.dp, CardBorder, RoundedCornerShape(24.dp))
            .padding(16.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            // Card Title
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Filled.Description,
                    contentDescription = null,
                    tint = BrandBlue,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "01. NHẬP BÀI HÁT GỐC",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = BrandBlueDeep
                )
            }

            // Lyrics textarea with paste button
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFFF8FAFC))
                    .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(16.dp))
                    .padding(12.dp)
            ) {
                OutlinedTextField(
                    value = originalLyrics,
                    onValueChange = { viewModel.updateOriginalLyrics(it) },
                    placeholder = {
                        Text(
                            text = "Dán lời bài hát gốc vào đây...",
                            fontSize = 13.sp,
                            color = Color(0xFF94A3B8)
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(end = 28.dp),
                    colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent
                    ),
                    maxLines = 5
                )

                IconButton(
                    onClick = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val clip = clipboard.primaryClip?.getItemAt(0)?.text?.toString()
                        if (!clip.isNullOrBlank()) {
                            viewModel.updateOriginalLyrics(clip)
                            Toast.makeText(context, "Đã dán lời bài hát!", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ContentPaste,
                        contentDescription = "Dán",
                        tint = Color(0xFF94A3B8),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            // Waveform visual row & Select Audio Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Mini Waveform
                Row(
                    horizontalArrangement = Arrangement.spacedBy(3.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val heights = listOf(14, 20, 8, 24, 16, 28, 12, 20)
                    heights.forEach { h ->
                        Box(
                            modifier = Modifier
                                .width(3.dp)
                                .height(h.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(BrandBlue)
                        )
                    }
                }

                // Upload Button
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .border(1.dp, Color(0xFF60A5FA), RoundedCornerShape(20.dp))
                        .clickable { audioPickerLauncher.launch("audio/*") }
                        .padding(horizontal = 14.dp, vertical = 7.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        tint = BrandBlue,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Chọn file audio",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = BrandBlue
                    )
                }
            }

            // Selected Audio File Pill Box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(BrandBlueSoft)
                    .border(1.dp, Color(0xFFDBEAFE), RoundedCornerShape(16.dp))
                    .padding(10.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(BrandBlue),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.MusicNote,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = audioFileName,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = audioDuration,
                                    fontSize = 11.sp,
                                    color = TextSecondary,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = "● Ước 10 câu",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color(0xFF059669)
                                )
                                Text(
                                    text = "Thay file",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = BrandBlue,
                                    modifier = Modifier.clickable { audioPickerLauncher.launch("audio/*") }
                                )
                                Text(
                                    text = "✕ Xóa",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color(0xFFEF4444),
                                    modifier = Modifier.clickable { viewModel.clearAudioFile() }
                                )
                            }
                        }
                    }
                }
            }

            // Analyze CTA Button
            PrimaryGradientButton(
                text = "PHÂN TÍCH AUDIO & LỜI HÁT",
                icon = Icons.Filled.GraphicEq,
                tag = "analyze_button",
                onClick = { viewModel.analyzeAudioAndLyrics() }
            )
        }
    }

    // Quick Shortcuts
    ShortcutItem(
        icon = Icons.Outlined.Settings,
        title = "Cài đặt",
        subtitle = "API Key, cấu hình hệ thống",
        onClick = onNavigateToSettings
    )

    ShortcutItem(
        icon = Icons.Outlined.LibraryMusic,
        title = "Quản lý Skills",
        subtitle = "Thêm và quản lý AI Skills",
        onClick = onNavigateToSkills
    )
}

@Composable
private fun ShortcutItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(2.dp, RoundedCornerShape(16.dp))
            .background(Color.White, RoundedCornerShape(16.dp))
            .border(1.dp, CardBorder, RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFFEFF6FF)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = BrandBlue,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = title,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = subtitle,
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                }
            }

            Icon(
                imageVector = Icons.Default.KeyboardArrowRight,
                contentDescription = null,
                tint = Color(0xFF94A3B8)
            )
        }
    }
}

// -------------------------------------------------------------------------------------------------
// STEP 02: PHÂN TÍCH THEO AUDIO
// -------------------------------------------------------------------------------------------------
@Composable
private fun Step02AudioAnalysis(
    viewModel: SongViewModel,
    playbackState: PlaybackState,
    segments: List<LyricSegment>
) {
    var editingSegment by remember { mutableStateOf<LyricSegment?>(null) }

    // Section Header
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(BrandBlueSoft),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.MusicNote,
                contentDescription = null,
                tint = BrandBlue,
                modifier = Modifier.size(18.dp)
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "02. PHÂN TÍCH THEO AUDIO",
            fontSize = 17.sp,
            fontWeight = FontWeight.Black,
            color = BrandBlueDeep
        )
    }

    Text(
        text = "AI xác định câu hát theo cách ca sĩ thực sự hát, không phụ thuộc vào các xuống dòng của văn bản.",
        fontSize = 12.sp,
        color = TextSecondary,
        lineHeight = 17.sp
    )

    // Waveform Player Card
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(3.dp, RoundedCornerShape(20.dp))
            .background(Color.White, RoundedCornerShape(20.dp))
            .border(1.dp, CardBorder, RoundedCornerShape(20.dp))
            .padding(14.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { viewModel.audioManager.togglePlaySong() },
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(BrandBlue)
                ) {
                    Icon(
                        imageVector = if (playbackState.isPlaying && playbackState.playingSegmentIndex == null) {
                            Icons.Default.Pause
                        } else {
                            Icons.Default.PlayArrow
                        },
                        contentDescription = "Play/Pause",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                AudioWaveformVisualizer(
                    isPlaying = playbackState.isPlaying,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Mic,
                        contentDescription = null,
                        tint = BrandBlue,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${segments.size} câu hát",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }

                Text(
                    text = "00:00 / 03:45",
                    fontSize = 11.sp,
                    color = TextSecondary,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }

    // Segments List
    val badgeColors = listOf(BadgeRed, BadgeOrange, BadgeGreen, BadgeFuchsia, BadgeTeal)

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        segments.forEachIndexed { idx, segment ->
            val isPlayingThis = playbackState.isPlaying && playbackState.playingSegmentIndex == segment.index
            val badgeColor = badgeColors[idx % badgeColors.size]

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(2.dp, RoundedCornerShape(16.dp))
                    .background(Color.White, RoundedCornerShape(16.dp))
                    .border(
                        1.dp,
                        if (isPlayingThis) BrandBlue else CardBorder,
                        RoundedCornerShape(16.dp)
                    )
                    .padding(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(22.dp)
                                    .clip(CircleShape)
                                    .background(badgeColor),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "${segment.index}",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Câu hát ${String.format("%02d", segment.index)}",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "${segment.startTime} - ${segment.endTime}",
                                fontSize = 11.sp,
                                color = Color(0xFF94A3B8)
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = segment.originalText,
                            fontSize = 13.sp,
                            color = TextPrimary,
                            fontWeight = FontWeight.Medium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(start = 30.dp)
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = { viewModel.audioManager.playSegment(segment.index) },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = if (isPlayingThis) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = "Play Segment",
                                tint = BrandBlue
                            )
                        }

                        IconButton(
                            onClick = { editingSegment = segment },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Edit Segment",
                                tint = BrandBlue,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }
    }

    // CTA Button
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(16.dp))
            .background(BrandBlue, RoundedCornerShape(16.dp))
            .clickable { viewModel.navigateToStep(ComposeStep.STEP_03_WRITE_NEW) }
            .padding(vertical = 14.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "Tiếp tục",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(text = "➔", color = Color.White, fontSize = 16.sp)
        }
    }

    // Dialog for editing segment text
    editingSegment?.let { seg ->
        var editValue by remember { mutableStateOf(seg.originalText) }
        AlertDialog(
            onDismissRequest = { editingSegment = null },
            title = { Text("Sửa câu hát ${seg.index}") },
            text = {
                OutlinedTextField(
                    value = editValue,
                    onValueChange = { editValue = it },
                    label = { Text("Lời câu hát") }
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.updateSegmentText(seg.index, editValue)
                        editingSegment = null
                    }
                ) {
                    Text("Lưu")
                }
            },
            dismissButton = {
                TextButton(onClick = { editingSegment = null }) {
                    Text("Hủy")
                }
            }
        )
    }
}

// -------------------------------------------------------------------------------------------------
// STEP 03: SÁNG TÁC LỜI MỚI
// -------------------------------------------------------------------------------------------------
@Composable
private fun Step03WriteNew(
    viewModel: SongViewModel,
    playbackState: PlaybackState,
    segments: List<LyricSegment>,
    apiKey: String
) {
    var manualEditSegment by remember { mutableStateOf<LyricSegment?>(null) }

    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = Icons.Filled.Edit,
            contentDescription = null,
            tint = BrandBlue,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "03. SÁNG TÁC LỜI MỚI",
            fontSize = 17.sp,
            fontWeight = FontWeight.Black,
            color = BrandBlueDeep
        )
    }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        segments.forEach { segment ->
            val isPlaying = playbackState.isPlaying && playbackState.playingSegmentIndex == segment.index

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(2.dp, RoundedCornerShape(18.dp))
                    .background(Color.White, RoundedCornerShape(18.dp))
                    .border(
                        1.dp,
                        if (isPlaying) BrandBlue else Color(0xFFDBEAFE),
                        RoundedCornerShape(18.dp)
                    )
                    .padding(14.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    // Card Top
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF6366F1)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(13.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Câu ${segment.index}",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "${segment.startTime} – ${segment.endTime}",
                            fontSize = 12.sp,
                            color = Color(0xFF94A3B8)
                        )
                    }

                    // Comparison
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Column {
                            Text(
                                text = "Lời gốc:",
                                fontSize = 12.sp,
                                color = Color(0xFF94A3B8)
                            )
                            Text(
                                text = segment.originalText,
                                fontSize = 13.sp,
                                color = Color(0xFF475569),
                                fontWeight = FontWeight.Normal
                            )
                        }

                        Column {
                            Text(
                                text = "Lời mới:",
                                fontSize = 12.sp,
                                color = BrandBlue,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = segment.newText,
                                fontSize = 14.sp,
                                color = TextPrimary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Action Buttons Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Play button
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(BrandBlue)
                                .clickable { viewModel.audioManager.playSegment(segment.index) },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = "Play",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        // Sửa Button
                        Row(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(20.dp))
                                .border(1.dp, Color(0xFF93C5FD), RoundedCornerShape(20.dp))
                                .clickable { manualEditSegment = segment }
                                .padding(vertical = 7.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = null,
                                tint = BrandBlue,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Sửa",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = BrandBlue
                            )
                        }

                        // Gợi ý 5 câu Button
                        Row(
                            modifier = Modifier
                                .weight(1.3f)
                                .clip(RoundedCornerShape(20.dp))
                                .border(1.dp, Color(0xFF93C5FD), RoundedCornerShape(20.dp))
                                .clickable { viewModel.openSuggestionsForSegment(segment, apiKey) }
                                .padding(vertical = 7.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.AutoAwesome,
                                contentDescription = null,
                                tint = BrandBlue,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Gợi ý 5 câu",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = BrandBlue
                            )
                        }
                    }
                }
            }
        }
    }

    // Continue CTA
    PrimaryGradientButton(
        text = "TIẾP TỤC CHỌN PHONG CÁCH ➔",
        tag = "step_03_continue",
        onClick = { viewModel.navigateToStep(ComposeStep.STEP_04_STYLE_SELECTION) }
    )

    manualEditSegment?.let { seg ->
        var manualValue by remember { mutableStateOf(seg.newText) }
        AlertDialog(
            onDismissRequest = { manualEditSegment = null },
            title = { Text("Chỉnh sửa lời mới - Câu ${seg.index}") },
            text = {
                OutlinedTextField(
                    value = manualValue,
                    onValueChange = { manualValue = it },
                    label = { Text("Lời mới") }
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.updateSegmentText(seg.index, manualValue)
                        manualEditSegment = null
                    }
                ) {
                    Text("Lưu")
                }
            },
            dismissButton = {
                TextButton(onClick = { manualEditSegment = null }) {
                    Text("Hủy")
                }
            }
        )
    }
}

// -------------------------------------------------------------------------------------------------
// STEP 04: PHONG CÁCH BÀI HÁT MỚI
// -------------------------------------------------------------------------------------------------
@Composable
private fun Step04StyleSelection(
    viewModel: SongViewModel
) {
    val config by viewModel.styleConfig.collectAsState()

    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(CircleShape)
                .background(Color(0xFFDBEAFE)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.Star,
                contentDescription = null,
                tint = BrandBlue,
                modifier = Modifier.size(16.dp)
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "04. PHONG CÁCH BÀI HÁT MỚI",
            fontSize = 17.sp,
            fontWeight = FontWeight.Black,
            color = BrandBlueDeep
        )
    }

    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        StyleOptionItem(
            icon = Icons.Filled.MusicNote,
            iconTint = Color(0xFF10B981),
            title = "1. Thể loại nhạc",
            value = config.genre,
            options = listOf("Pop Ballad", "Rock", "Acoustic", "R&B", "EDM", "Indie Pop"),
            onSelect = { viewModel.updateStyleConfig(config.copy(genre = it)) }
        )

        StyleOptionItem(
            icon = Icons.Filled.Mic,
            iconTint = Color(0xFFD946EF),
            title = "2. Phong cách giọng hát",
            value = config.vocalStyle,
            options = listOf("Nữ nhẹ nhàng", "Nam mạnh mẽ", "Nữ nội lực", "Nam trầm ấm"),
            onSelect = { viewModel.updateStyleConfig(config.copy(vocalStyle = it)) }
        )

        StyleOptionItem(
            icon = Icons.Filled.MusicNote,
            iconTint = Color(0xFF3B82F6),
            title = "3. Thể loại phụ",
            value = config.subGenre,
            options = listOf("Acoustic Ballad", "Indie Folk", "Upbeat Pop", "Lo-fi"),
            onSelect = { viewModel.updateStyleConfig(config.copy(subGenre = it)) }
        )

        StyleOptionItem(
            icon = Icons.Default.GraphicEq,
            iconTint = Color(0xFFF59E0B),
            title = "4. Tốc độ nhạc",
            value = config.tempo,
            options = listOf("Chậm", "Trung bình", "Nhanh", "Rất nhanh"),
            onSelect = { viewModel.updateStyleConfig(config.copy(tempo = it)) }
        )

        StyleOptionItem(
            icon = Icons.Filled.Favorite,
            iconTint = Color(0xFFF43F5E),
            title = "5. Cảm xúc",
            value = config.emotion,
            options = listOf("Lãng mạn", "U sầu", "Vui tươi", "Hào hùng", "Hoài niệm"),
            onSelect = { viewModel.updateStyleConfig(config.copy(emotion = it)) }
        )

        StyleOptionItem(
            icon = Icons.Default.Description,
            iconTint = Color(0xFF0D9488),
            title = "6. Chủ đề lời bài hát",
            value = config.theme,
            options = listOf("Tình yêu", "Gia đình", "Du lịch", "Tình bạn", "Cuộc sống"),
            onSelect = { viewModel.updateStyleConfig(config.copy(theme = it)) }
        )

        StyleOptionItem(
            icon = Icons.Filled.MusicNote,
            iconTint = Color(0xFF7C3AED),
            title = "7. Bố cục bài hát",
            value = config.structure,
            options = listOf("Verse - Chorus", "Verse - Chorus - Bridge", "AABA"),
            onSelect = { viewModel.updateStyleConfig(config.copy(structure = it)) }
        )

        StyleOptionItem(
            icon = Icons.Filled.MusicNote,
            iconTint = Color(0xFFEC4899),
            title = "8. Nhạc cụ",
            value = config.instruments,
            options = listOf("Guitar, Piano, Violin", "Guitar Acoustic", "Synth & Drum Machine", "Piano Solo"),
            onSelect = { viewModel.updateStyleConfig(config.copy(instruments = it)) }
        )

        StyleOptionItem(
            icon = Icons.Default.GraphicEq,
            iconTint = Color(0xFF6366F1),
            title = "9. Phối khí & sản xuất",
            value = config.arrangement,
            options = listOf("Hiện đại, acoustic", "Mộc mạc, sâu lắng", "Điện tử sôi động", "Cổ điển dàn dây"),
            onSelect = { viewModel.updateStyleConfig(config.copy(arrangement = it)) }
        )

        StyleOptionItem(
            icon = Icons.Filled.Star,
            iconTint = Color(0xFFF59E0B),
            title = "10. Năng lượng & thể hiện",
            value = config.energy,
            options = listOf("Dịu dàng, nhẹ nhàng", "Mạnh mẽ, bùng nổ", "Sôi động, tươi trẻ"),
            onSelect = { viewModel.updateStyleConfig(config.copy(energy = it)) }
        )
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        PrimaryGradientButton(
            text = "★ TẠO BÀI HÁT MỚI",
            tag = "create_song_button",
            onClick = { viewModel.generateNewSong() }
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "Giữ nhịp điệu — Đổi lời ca — Sáng tạo phong cách",
            fontSize = 10.sp,
            color = Color(0xFF94A3B8),
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun StyleOptionItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconTint: Color,
    title: String,
    value: String,
    options: List<String>,
    onSelect: (String) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(1.dp, RoundedCornerShape(12.dp))
            .background(Color.White, RoundedCornerShape(12.dp))
            .border(1.dp, Color(0xFFF1F5F9), RoundedCornerShape(12.dp))
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = title,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF334155)
                )
            }

            DropdownOptionSelector(
                label = title,
                selectedValue = value,
                options = options,
                onSelect = onSelect
            )
        }
    }
}

// -------------------------------------------------------------------------------------------------
// STEP 05: BÀI HÁT CỦA BẠN & PRODUCTION GUIDE
// -------------------------------------------------------------------------------------------------
@Composable
private fun Step05SongComplete(
    viewModel: SongViewModel,
    playbackState: PlaybackState
) {
    val context = LocalContext.current
    val song = viewModel.completedSong.collectAsState().value
    var isProdGuideExpanded by remember { mutableStateOf(true) }
    var isAdvancedSettingsExpanded by remember { mutableStateOf(false) }

    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(BrandBlueSoft),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.MusicNote,
                contentDescription = null,
                tint = BrandBlue,
                modifier = Modifier.size(18.dp)
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "05. BÀI HÁT CỦA BẠN",
            fontSize = 17.sp,
            fontWeight = FontWeight.Black,
            color = BrandBlueDeep
        )
    }

    // Song Banner Card
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(3.dp, RoundedCornerShape(20.dp))
            .background(Color.White, RoundedCornerShape(20.dp))
            .border(1.dp, CardBorder, RoundedCornerShape(20.dp))
            .padding(14.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column {
                    Text(
                        text = song?.title ?: "Bếp Ấm Yêu Thương",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color(0xFFEFF6FF))
                            .border(1.dp, Color(0xFFBFDBFE), RoundedCornerShape(20.dp))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = song?.genre ?: "Pop Ballad",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = BrandBlue
                        )
                    }
                }

                IconButton(
                    onClick = { viewModel.toggleFavoriteCurrentSong() },
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFFEF2F2))
                ) {
                    Icon(
                        imageVector = if (song?.isSaved == true) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Yêu thích",
                        tint = Color(0xFFEF4444),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Banner Image
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xFFFEF3C7))
            ) {
                AsyncImage(
                    model = song?.imageUrl ?: "https://lh3.googleusercontent.com/aida-public/AB6AXuCYDkIjTpqH2PJcCKAFznhl2i-IojN-6TYjrrhCMB13ySDY-dE3rTotRHV515H4kQr1rEjK9yJvtNhp0TlxtJlsdlYeKaes-_91GLYPO88S1fc0VI-tYREAnahYjOj0L4S-5iDjzK6JawPhO2BoVEDb3mTChZdeQ49sMUQAchmbfLkJ4OhQFfZ4aOKfx7eMOYI09wsXhMQJG-UtnHimYssjfsxF1v-H7AWH5CQ4kqlfp0oo9rvbcPBvEw",
                    contentDescription = "Song cover",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }

    // Lyrics Display Card
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(3.dp, RoundedCornerShape(20.dp))
            .background(Color.White, RoundedCornerShape(20.dp))
            .border(1.dp, CardBorder, RoundedCornerShape(20.dp))
            .padding(16.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            val lyrics = song?.newLyrics ?: ""
            Text(
                text = lyrics,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF334155),
                lineHeight = 22.sp
            )
        }
    }

    // Audio Playback Controls
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(3.dp, RoundedCornerShape(20.dp))
            .background(Color.White, RoundedCornerShape(20.dp))
            .border(1.dp, CardBorder, RoundedCornerShape(20.dp))
            .padding(14.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            // Slider
            val currentPos = playbackState.currentPositionMs.toFloat()
            val total = playbackState.totalDurationMs.toFloat()

            Slider(
                value = currentPos,
                onValueChange = { viewModel.audioManager.seekTo(it.toInt()) },
                valueRange = 0f..total,
                colors = SliderDefaults.colors(
                    thumbColor = BrandBlue,
                    activeTrackColor = BrandBlue,
                    inactiveTrackColor = Color(0xFFDBEAFE)
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                val currentMin = (playbackState.currentPositionMs / 1000) / 60
                val currentSec = (playbackState.currentPositionMs / 1000) % 60
                val totalMin = (playbackState.totalDurationMs / 1000) / 60
                val totalSec = (playbackState.totalDurationMs / 1000) % 60

                Text(
                    text = String.format("%02d:%02d", currentMin, currentSec),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF94A3B8)
                )
                Text(
                    text = String.format("%02d:%02d", totalMin, totalSec),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF94A3B8)
                )
            }

            // Buttons: Nghe thử & Copy toàn bộ lời
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Nghe thử
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(14.dp))
                        .background(
                            Brush.horizontalGradient(listOf(BrandBlue, Color(0xFF3B82F6)))
                        )
                        .clickable { viewModel.audioManager.togglePlaySong() }
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (playbackState.isPlaying && playbackState.playingSegmentIndex == null) {
                                Icons.Default.Pause
                            } else {
                                Icons.Default.PlayArrow
                            },
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (playbackState.isPlaying && playbackState.playingSegmentIndex == null) "Tạm dừng" else "Nghe thử",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }

                // Copy toàn bộ lời
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xFFEFF6FF))
                        .border(1.dp, Color(0xFFBFDBFE), RoundedCornerShape(14.dp))
                        .clickable {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clip = ClipData.newPlainText("Lyrics", song?.newLyrics ?: "")
                            clipboard.setPrimaryClip(clip)
                            Toast.makeText(context, "Đã copy toàn bộ lời bài hát!", Toast.LENGTH_SHORT).show()
                        }
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = null,
                            tint = BrandBlue,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "COPY TOÀN BỘ LỜI",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = BrandBlueDark
                        )
                    }
                }
            }
        }
    }

    // PRODUCTION GUIDE CARD
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(3.dp, RoundedCornerShape(20.dp))
            .background(Color.White, RoundedCornerShape(20.dp))
            .border(1.dp, CardBorder, RoundedCornerShape(20.dp))
            .padding(14.dp)
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isProdGuideExpanded = !isProdGuideExpanded },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Description,
                        contentDescription = null,
                        tint = BrandBlue,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "PRODUCTION GUIDE",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }

                Icon(
                    imageVector = if (isProdGuideExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    tint = Color(0xFF94A3B8)
                )
            }

            AnimatedVisibility(visible = isProdGuideExpanded) {
                Column(
                    modifier = Modifier.padding(top = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    val guideText = song?.productionGuide ?: ""

                    Text(
                        text = guideText,
                        fontSize = 12.sp,
                        color = Color(0xFF334155),
                        lineHeight = 18.sp
                    )

                    Text(
                        text = "ENGLISH • ${guideText.length}/990 CHARACTERS",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF94A3B8)
                    )

                    PrimaryGradientButton(
                        text = "COPY PRODUCTION GUIDE",
                        icon = Icons.Default.ContentCopy,
                        tag = "copy_guide_btn",
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clip = ClipData.newPlainText("Production Guide", guideText)
                            clipboard.setPrimaryClip(clip)
                            Toast.makeText(context, "Đã copy Production Guide!", Toast.LENGTH_SHORT).show()
                        }
                    )
                }
            }
        }
    }

    // Advanced Settings Toggle
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(2.dp, RoundedCornerShape(20.dp))
            .background(Color.White, RoundedCornerShape(20.dp))
            .border(1.dp, CardBorder, RoundedCornerShape(20.dp))
            .padding(14.dp)
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isAdvancedSettingsExpanded = !isAdvancedSettingsExpanded },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.Star,
                        contentDescription = null,
                        tint = BrandBlue,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Tùy chỉnh nâng cao",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }

                Icon(
                    imageVector = if (isAdvancedSettingsExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    tint = Color(0xFF94A3B8)
                )
            }

            AnimatedVisibility(visible = isAdvancedSettingsExpanded) {
                Column(
                    modifier = Modifier.padding(top = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AdvancedSettingRow("Thể loại nhạc", song?.genre ?: "Pop Ballad")
                    AdvancedSettingRow("Phong cách giọng hát", song?.vocalStyle ?: "Nữ nhẹ nhàng")
                    AdvancedSettingRow("Thể loại phụ", song?.subGenre ?: "Acoustic Ballad")
                    AdvancedSettingRow("Tốc độ nhạc", song?.tempo ?: "Trung bình")
                    AdvancedSettingRow("Cảm xúc", song?.emotion ?: "Lãng mạn")
                    AdvancedSettingRow("Chủ đề lời bài hát", song?.theme ?: "Tình yêu")
                }
            }
        }
    }
}

@Composable
private fun AdvancedSettingRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, fontSize = 12.sp, color = TextSecondary)
        Text(text = value, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
    }
}
