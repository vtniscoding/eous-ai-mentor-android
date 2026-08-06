package com.eous.mentor.domain.usecase.chat

import com.eous.mentor.domain.repository.ChatRepository

class DeleteAllSessionsUseCase(
    private val chatRepository: ChatRepository
) {
    suspend operator fun invoke(userId: String): Result<Unit> = chatRepository.deleteAllSessions(userId)
}
