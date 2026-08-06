package com.eous.mentor.domain.usecase.profile

import com.eous.mentor.domain.model.Profile
import com.eous.mentor.domain.repository.UserRepository

class GetProfileUseCase(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(userId: String): Result<Profile?> =
        userRepository.getProfile(userId)
}
