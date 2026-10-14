package com.example.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.media.MediaPlayer
import android.net.Uri
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.sin

data class PlaybackState(
    val isPlaying: Boolean = false,
    val currentPositionMs: Int = 75000, // 01:15 default matching mockup
    val totalDurationMs: Int = 225000,  // 03:45 default matching mockup
    val playingSegmentIndex: Int? = null,
    val customAudioUri: Uri? = null,
    val audioTitle: String = "original_song.mp3"
)

class AudioPlaybackManager(private val context: Context) {
    private val _playbackState = MutableStateFlow(PlaybackState())
    val playbackState: StateFlow<PlaybackState> = _playbackState.asStateFlow()

    private var mediaPlayer: MediaPlayer? = null
    private var progressJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.Default)

    fun playSegment(segmentIndex: Int, durationSeconds: Int = 6) {
        if (_playbackState.value.isPlaying && _playbackState.value.playingSegmentIndex == segmentIndex) {
            pause()
            return
        }

        stop()
        _playbackState.value = _playbackState.value.copy(
            isPlaying = true,
            playingSegmentIndex = segmentIndex
        )

        // Play musical melody tones corresponding to segment index
        playMelodyForSegment(segmentIndex, durationSeconds)
    }

    fun togglePlaySong() {
        if (_playbackState.value.isPlaying) {
            pause()
        } else {
            playFullSong()
        }
    }

    fun seekTo(positionMs: Int) {
        val clamped = positionMs.coerceIn(0, _playbackState.value.totalDurationMs)
        _playbackState.value = _playbackState.value.copy(currentPositionMs = clamped)
        mediaPlayer?.let {
            if (it.isPlaying) {
                it.seekTo(clamped)
            }
        }
    }

    fun setCustomAudio(uri: Uri, fileName: String, durationMs: Int = 225000) {
        stop()
        _playbackState.value = _playbackState.value.copy(
            customAudioUri = uri,
            audioTitle = fileName,
            totalDurationMs = durationMs,
            currentPositionMs = 0
        )
    }

    fun clearAudio() {
        stop()
        _playbackState.value = PlaybackState(
            audioTitle = "original_song.mp3",
            currentPositionMs = 0
        )
    }

    private fun playFullSong() {
        val customUri = _playbackState.value.customAudioUri
        if (customUri != null) {
            try {
                mediaPlayer?.release()
                mediaPlayer = MediaPlayer().apply {
                    setDataSource(context, customUri)
                    prepare()
                    seekTo(_playbackState.value.currentPositionMs)
                    start()
                    setOnCompletionListener {
                        stop()
                    }
                }
            } catch (_: Exception) {
                // Fallback to synthesized melody
            }
        }

        _playbackState.value = _playbackState.value.copy(
            isPlaying = true,
            playingSegmentIndex = null
        )

        progressJob?.cancel()
        progressJob = scope.launch {
            while (isActive && _playbackState.value.isPlaying) {
                delay(500)
                val newPos = _playbackState.value.currentPositionMs + 500
                if (newPos >= _playbackState.value.totalDurationMs) {
                    _playbackState.value = _playbackState.value.copy(
                        isPlaying = false,
                        currentPositionMs = _playbackState.value.totalDurationMs
                    )
                    break
                } else {
                    _playbackState.value = _playbackState.value.copy(currentPositionMs = newPos)
                }
            }
        }
    }

    fun pause() {
        mediaPlayer?.pause()
        progressJob?.cancel()
        _playbackState.value = _playbackState.value.copy(isPlaying = false)
    }

    fun stop() {
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
        progressJob?.cancel()
        _playbackState.value = _playbackState.value.copy(
            isPlaying = false,
            playingSegmentIndex = null
        )
    }

    private fun playMelodyForSegment(segmentIndex: Int, durationSec: Int) {
        scope.launch(Dispatchers.IO) {
            // Note frequencies for soothing pentatonic Vietnamese melody: C4, D4, E4, G4, A4, C5
            val notes = when (segmentIndex % 5) {
                1 -> doubleArrayOf(261.63, 293.66, 329.63, 392.00, 440.00) // C - D - E - G - A
                2 -> doubleArrayOf(329.63, 392.00, 440.00, 523.25, 440.00) // E - G - A - C5 - A
                3 -> doubleArrayOf(392.00, 440.00, 523.25, 587.33, 523.25) // G - A - C5 - D5 - C5
                4 -> doubleArrayOf(440.00, 392.00, 329.63, 293.66, 261.63) // A - G - E - D - C
                else -> doubleArrayOf(261.63, 329.63, 392.00, 440.00, 523.25)
            }

            try {
                val sampleRate = 22050
                val noteDurationMs = (durationSec * 1000) / notes.size
                val numSamplesPerNote = (sampleRate * noteDurationMs / 1000)
                val trackBufferSize = numSamplesPerNote * 2

                val audioTrack = AudioTrack.Builder()
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_MEDIA)
                            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                            .build()
                    )
                    .setAudioFormat(
                        AudioFormat.Builder()
                            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                            .setSampleRate(sampleRate)
                            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                            .build()
                    )
                    .setBufferSizeInBytes(trackBufferSize)
                    .setTransferMode(AudioTrack.MODE_STREAM)
                    .build()

                audioTrack.play()

                for (freq in notes) {
                    if (!_playbackState.value.isPlaying || _playbackState.value.playingSegmentIndex != segmentIndex) {
                        break
                    }
                    val buffer = ShortArray(numSamplesPerNote)
                    for (i in 0 until numSamplesPerNote) {
                        // Envelope to avoid clicking
                        val envelope = when {
                            i < 500 -> i / 500.0
                            i > numSamplesPerNote - 500 -> (numSamplesPerNote - i) / 500.0
                            else -> 1.0
                        }
                        val sample = (sin(2.0 * Math.PI * i * freq / sampleRate) * 0.6 * envelope * Short.MAX_VALUE).toInt()
                        buffer[i] = sample.toShort()
                    }
                    audioTrack.write(buffer, 0, buffer.size)
                }

                audioTrack.stop()
                audioTrack.release()
            } catch (_: Exception) {
            } finally {
                if (_playbackState.value.playingSegmentIndex == segmentIndex) {
                    _playbackState.value = _playbackState.value.copy(
                        isPlaying = false,
                        playingSegmentIndex = null
                    )
                }
            }
        }
    }
}
