package com.eous.mentor.domain.usecase.session

import com.eous.mentor.domain.repository.UserRepository

class GetRemoteSessionIdUseCase(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(userId: String): Result<String?> =
        userRepository.getRemoteSessionId(userId)
}
