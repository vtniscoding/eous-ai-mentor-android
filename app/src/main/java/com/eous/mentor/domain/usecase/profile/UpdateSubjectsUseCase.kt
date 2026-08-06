package com.eous.mentor.domain.usecase.profile

import com.eous.mentor.domain.repository.UserRepository

class UpdateSubjectsUseCase(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(userId: String, subjects: List<String>): Result<Unit> =
        userRepository.updateSubjects(userId, subjects)
}
