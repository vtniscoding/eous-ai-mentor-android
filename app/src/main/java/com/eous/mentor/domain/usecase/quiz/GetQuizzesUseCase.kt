package com.eous.mentor.domain.usecase.quiz

import com.eous.mentor.domain.model.Quiz
import com.eous.mentor.domain.repository.UserRepository

class GetQuizzesUseCase(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(userId: String): Result<List<Quiz>> =
        userRepository.getQuizzes(userId)
}
