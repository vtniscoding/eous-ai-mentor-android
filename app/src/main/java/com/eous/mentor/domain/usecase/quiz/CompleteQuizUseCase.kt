package com.eous.mentor.domain.usecase.quiz

import com.eous.mentor.domain.repository.UserRepository

class CompleteQuizUseCase(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(
        userId: String,
        quizId: String,
        totalQuestions: Int,
        score: Int?
    ): Result<Unit> = runCatching {
        userRepository.updateQuiz(quizId, totalQuestions, score, "completed").getOrThrow()
        userRepository.recordUserActivity(userId).getOrThrow()
    }
}
