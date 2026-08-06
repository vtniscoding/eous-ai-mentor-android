package com.eous.mentor.domain.usecase.quiz

import com.eous.mentor.domain.repository.UserRepository

class ResetQuizUseCase(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(quizId: String): Result<Unit> =
        userRepository.updateQuiz(quizId, 0, null, "in_progress")
}
