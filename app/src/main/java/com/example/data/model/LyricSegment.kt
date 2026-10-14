package com.example.data.model

data class LyricSegment(
    val index: Int,
    val startTime: String,
    val endTime: String,
    val originalText: String,
    var newText: String,
    val suggestions: List<String> = emptyList()
)
