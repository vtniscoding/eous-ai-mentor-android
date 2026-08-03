package com.eous.mentor.features.chat

import com.eous.mentor.domain.model.ChatMessage
import com.eous.mentor.domain.model.ChatSession

data class ChatState(
        // Session management
        val sessions: List<ChatSession> = emptyList(),
        val activeSession: ChatSession? = null,
        val isLoadingSessions: Boolean = true,
        val explanationStyle: String = "detailed", // "short", "detailed", "step_by_step"
        val isStyleMenuExpanded: Boolean = false,

        // Messages in the active session
        val messages: List<ChatMessage> = emptyList(),
        val isLoadingMessages: Boolean = false,

        // Input
        val inputText: String = "",
        val pendingImageUri: String? = null, // local URI of image picked by user
        val pendingImageUrl: String? = null, // uploaded Storage URL

        // AI response state
        val isSending: Boolean = false,
        val isAiResponding: Boolean = false,

        // Session drawer
        val isSessionDrawerOpen: Boolean = false,

        // Errors
        val errorMessage: String? = null
)
