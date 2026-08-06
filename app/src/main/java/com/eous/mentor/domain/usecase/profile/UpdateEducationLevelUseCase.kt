package com.eous.mentor.domain.usecase.profile

import com.eous.mentor.domain.repository.UserRepository

class UpdateEducationLevelUseCase(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(userId: String, level: String): Result<Unit> =
        userRepository.updateEducationLevel(userId, level)
}
