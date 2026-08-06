package com.eous.mentor.domain.usecase.chat

import com.eous.mentor.domain.model.ChatMessage
import com.eous.mentor.domain.model.ChatSession
import com.eous.mentor.domain.repository.ChatRepository

class UpdateSessionSubjectUseCase(
    private val chatRepository: ChatRepository
) {
    suspend operator fun invoke(sessionId: String, subject: String): Result<Unit> = chatRepository.updateSessionSubject(sessionId, subject)
}
