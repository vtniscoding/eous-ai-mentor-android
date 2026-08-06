package com.eous.mentor.domain.usecase.chat

import com.eous.mentor.domain.model.ChatSession
import com.eous.mentor.domain.repository.ChatRepository

class GetSessionsUseCase(
    private val chatRepository: ChatRepository
) {
    suspend operator fun invoke(userId: String): Result<List<ChatSession>> = chatRepository.getSessions(userId)
}
