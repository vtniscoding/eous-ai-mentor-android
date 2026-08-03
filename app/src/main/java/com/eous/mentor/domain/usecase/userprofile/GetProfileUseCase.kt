package com.eous.mentor.domain.usecase.user

import com.eous.mentor.domain.model.Profile
import com.eous.mentor.domain.repository.UserRepository

class GetProfileUseCase(private val repository: UserRepository) {
    suspend operator fun invoke(userId: String): Result<Profile?> =
        repository.getProfile(userId)
}
