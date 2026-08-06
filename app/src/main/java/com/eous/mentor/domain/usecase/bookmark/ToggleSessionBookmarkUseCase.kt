package com.eous.mentor.domain.usecase.bookmark

import com.eous.mentor.domain.model.ChatMessage
import com.eous.mentor.domain.model.ChatSession
import com.eous.mentor.domain.repository.ChatRepository

class ToggleSessionBookmarkUseCase(
    private val chatRepository: ChatRepository
) {
    suspend operator fun invoke(
        session: ChatSession,
        isCurrentlyBookmarked: Boolean,
        bookmarkedMsg: ChatMessage?,
        userId: String
    ): Result<Unit> = runCatching {
        val sessionId = session.id ?: throw IllegalArgumentException("Session ID is null")
        if (isCurrentlyBookmarked) {
            val msgId = bookmarkedMsg?.id ?: throw IllegalArgumentException("Bookmarked message ID is null")
            chatRepository.toggleBookmark(msgId, userId, false, "General").getOrThrow()
        } else {
            val messages = chatRepository.getMessages(sessionId).getOrThrow()
            val latestAiMsg = messages.lastOrNull { it.role == "ai" } 
                ?: throw Exception("No AI response found in this session to bookmark.")
            val msgId = latestAiMsg.id ?: throw Exception("AI message ID is null")
            chatRepository.toggleBookmark(
                messageId = msgId,
                userId = userId,
                isBookmarked = true,
                folder = session.subject
            ).getOrThrow()
        }
    }
}
