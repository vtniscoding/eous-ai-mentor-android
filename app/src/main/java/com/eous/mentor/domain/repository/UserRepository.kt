package com.eous.mentor.domain.repository

import com.eous.mentor.domain.model.Bookmark
import com.eous.mentor.domain.model.Profile
import com.eous.mentor.domain.model.Quiz
import com.eous.mentor.domain.model.QuizQuestion

interface UserRepository {
    suspend fun getProfile(userId: String): Result<Profile?>
    suspend fun getBookmarks(userId: String): Result<List<Bookmark>>
    suspend fun getQuizzes(userId: String): Result<List<Quiz>>
    suspend fun createQuiz(
            userId: String,
            topic: String,
            title: String,
            totalQuestions: Int,
            questions: List<QuizQuestion> = emptyList(),
            difficulty: String = "medium"
    ): Result<Quiz>
    suspend fun updateQuiz(
            quizId: String,
            currentQuestionIndex: Int,
            score: Int?,
            status: String
    ): Result<Unit>
    suspend fun updateSubjects(userId: String, subjects: List<String>): Result<Unit>
    suspend fun recordUserActivity(userId: String): Result<Profile?>
    suspend fun uploadAvatar(userId: String, imageBytes: ByteArray): Result<String>
    suspend fun deleteAvatar(userId: String): Result<Unit>
    suspend fun updateExplanationStyle(userId: String, style: String): Result<Unit>
    suspend fun updateEducationLevel(userId: String, level: String): Result<Unit>
    suspend fun saveOnboardingProfile(
        userId: String,
        educationLevel: String,
        explanationStyle: String,
        subjects: List<String>
    ): Result<Unit>
}

