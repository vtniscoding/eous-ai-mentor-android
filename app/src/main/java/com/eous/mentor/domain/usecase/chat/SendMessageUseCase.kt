package com.eous.mentor.domain.usecase.chat

import com.eous.mentor.domain.repository.ChatRepository

class SendMessageUseCase(private val repository: ChatRepository) {
    suspend operator fun invoke(message: String): Result<Unit> =
        repository.sendMessage(message)
}
