package com.eous.mentor.features.library

import com.eous.mentor.domain.model.ChatMessage
import com.eous.mentor.domain.model.ChatSession

data class LibraryState(
        val subjects: List<String> = emptyList(),
        val bookmarkedMessages: List<ChatMessage> = emptyList(),
        val sessions: List<ChatSession> = emptyList(),
        val isLoading: Boolean = false,
        val selectedFilter: String = "All",
        val searchQuery: String = "",
        val practiceSubject: String = "Math",
        val practiceQuestionCount: Int = 0,
        val errorMessage: String? = null
)
