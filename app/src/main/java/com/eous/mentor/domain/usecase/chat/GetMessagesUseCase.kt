package com.eous.mentor.domain.usecase.chat

import com.eous.mentor.domain.model.Message
import com.eous.mentor.domain.repository.ChatRepository

class GetMessagesUseCase(private val repository: ChatRepository) {
    suspend operator fun invoke(userId: String = ""): Result<List<Message>> =
        repository.getLegacyMessages(userId)
}
