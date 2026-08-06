package com.eous.mentor.domain.usecase.profile

import com.eous.mentor.domain.repository.UserRepository

class SaveOnboardingProfileUseCase(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(
        userId: String,
        educationLevel: String,
        explanationStyle: String,
        subjects: List<String>
    ): Result<Unit> =
        userRepository.saveOnboardingProfile(userId, educationLevel, explanationStyle, subjects)
}
