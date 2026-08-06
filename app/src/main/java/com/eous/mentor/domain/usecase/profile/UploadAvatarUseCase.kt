package com.eous.mentor.domain.usecase.profile

import com.eous.mentor.domain.repository.UserRepository

class UploadAvatarUseCase(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(userId: String, imageBytes: ByteArray): Result<String> =
        userRepository.uploadAvatar(userId, imageBytes)
}
