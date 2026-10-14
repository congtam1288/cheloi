package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "song_projects")
data class SongProject(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val genre: String = "Pop Ballad",
    val subGenre: String = "Acoustic Ballad",
    val vocalStyle: String = "Nữ nhẹ nhàng",
    val tempo: String = "Trung bình",
    val emotion: String = "Lãng mạn",
    val theme: String = "Tình yêu",
    val structure: String = "Verse - Chorus",
    val instruments: String = "Guitar, Piano, Violin",
    val arrangement: String = "Hiện đại, acoustic",
    val energy: String = "Dịu dàng, nhẹ nhàng",
    val duration: String = "03:45",
    val originalLyrics: String = "",
    val newLyrics: String = "",
    val productionGuide: String = "",
    val imageUrl: String = "",
    val isSaved: Boolean = false,
    val isCreated: Boolean = true,
    val audioFileName: String = "original_song.mp3",
    val audioDurationSeconds: Int = 225,
    val createdAt: Long = System.currentTimeMillis()
)
