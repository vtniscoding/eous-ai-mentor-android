package com.eous.mentor.domain.usecase.profile

import com.eous.mentor.domain.repository.UserRepository

class UpdateExplanationStyleUseCase(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(userId: String, style: String): Result<Unit> =
        userRepository.updateExplanationStyle(userId, style)
}
