package com.eous.mentor.domain.usecase.chat

import com.eous.mentor.domain.model.ChatMessage
import com.eous.mentor.domain.repository.ChatRepository

class GetSessionMessagesUseCase(
    private val chatRepository: ChatRepository
) {
    suspend operator fun invoke(sessionId: String): Result<List<ChatMessage>> = chatRepository.getMessages(sessionId)
}
