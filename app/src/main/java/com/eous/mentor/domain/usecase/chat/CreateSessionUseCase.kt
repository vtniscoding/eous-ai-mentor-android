package com.eous.mentor.domain.usecase.chat

import com.eous.mentor.domain.model.ChatMessage
import com.eous.mentor.domain.model.ChatSession
import com.eous.mentor.domain.repository.ChatRepository

class CreateSessionUseCase(
    private val chatRepository: ChatRepository
) {
    suspend operator fun invoke(userId: String): Result<ChatSession> = chatRepository.createSession(userId)
}
