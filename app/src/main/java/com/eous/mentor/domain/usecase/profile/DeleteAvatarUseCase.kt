package com.eous.mentor.domain.usecase.profile

import com.eous.mentor.domain.repository.UserRepository

class DeleteAvatarUseCase(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(userId: String): Result<Unit> =
        userRepository.deleteAvatar(userId)
}
