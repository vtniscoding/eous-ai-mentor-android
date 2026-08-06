package com.eous.mentor.domain.usecase.chat

import com.eous.mentor.domain.model.ChatMessage
import com.eous.mentor.domain.model.ChatSession
import com.eous.mentor.domain.repository.ChatRepository

class DeleteSessionUseCase(
    private val chatRepository: ChatRepository
) {
    suspend operator fun invoke(sessionId: String): Result<Unit> = chatRepository.deleteSession(sessionId)
}
