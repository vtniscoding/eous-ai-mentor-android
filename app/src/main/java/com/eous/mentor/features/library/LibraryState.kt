package com.eous.mentor.features.library

import com.eous.mentor.domain.model.ChatMessage

data class LibraryState(
        val subjects: List<String> = listOf("Math", "Science", "Programming", "Foreign Languages"),
        val bookmarkedMessages: List<ChatMessage> = emptyList(),
        val isLoading: Boolean = false,
        val selectedSubject: String? = null,
        val errorMessage: String? = null
)
