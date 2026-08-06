package com.eous.mentor.domain.usecase.bookmark

import com.eous.mentor.domain.repository.ChatRepository

class ToggleBookmarkUseCase(
    private val chatRepository: ChatRepository
) {
    suspend operator fun invoke(
        messageId: String,
        userId: String,
        isBookmarked: Boolean,
        folder: String = "General"
    ): Result<Unit> = chatRepository.toggleBookmark(messageId, userId, isBookmarked, folder)
}
