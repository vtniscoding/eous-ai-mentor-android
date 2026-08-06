package com.eous.mentor.domain.usecase.bookmark

import com.eous.mentor.domain.model.ChatMessage
import com.eous.mentor.domain.repository.ChatRepository

class GetBookmarkedMessagesUseCase(
    private val chatRepository: ChatRepository
) {
    suspend operator fun invoke(userId: String): Result<List<ChatMessage>> =
        chatRepository.getBookmarkedMessages(userId)
}
