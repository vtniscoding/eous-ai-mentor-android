package com.eous.mentor.domain.model

data class LibraryContent(
    val bookmarkedMessages: List<ChatMessage>,
    val sessions: List<ChatSession>,
    val subjects: List<String>,
    val practiceSubject: String,
    val practiceQuestionCount: Int,
    val hasPracticedToday: Boolean
)
