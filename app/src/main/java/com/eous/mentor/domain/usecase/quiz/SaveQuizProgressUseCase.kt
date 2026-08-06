package com.eous.mentor.domain.usecase.quiz

import com.eous.mentor.domain.repository.UserRepository

class SaveQuizProgressUseCase(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(
        userId: String,
        quizId: String,
        currentQuestionIndex: Int,
        score: Int?
    ): Result<Unit> = runCatching {
        userRepository.updateQuiz(quizId, currentQuestionIndex, score, "in_progress").getOrThrow()
        userRepository.recordUserActivity(userId).getOrThrow()
    }
}
