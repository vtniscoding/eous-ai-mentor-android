package com.eous.mentor.domain.usecase.chat

import com.eous.mentor.domain.model.ChatMessage
import com.eous.mentor.domain.model.ChatSession
import com.eous.mentor.domain.repository.ChatRepository

class UploadChatImageUseCase(
    private val chatRepository: ChatRepository
) {
    suspend operator fun invoke(userId: String, fileName: String, bytes: ByteArray): Result<String> = chatRepository.uploadImage(userId, fileName, bytes)
}
