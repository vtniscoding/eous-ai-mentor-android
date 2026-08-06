package com.eous.mentor.features.quizzes

import com.eous.mentor.domain.model.Quiz
import com.eous.mentor.domain.model.QuizQuestion

data class QuizzesState(
    val isLoading: Boolean = false,
    val isGeneratingQuiz: Boolean = false,
    val quizzes: List<Quiz> = emptyList(),
    val selectedFilter: QuizFilter = QuizFilter.ALL,
    val searchQuery: String = "",
    val errorMessage: String? = null,
    val userSubjects: List<String> = emptyList(),
    val userEducationLevel: String = "high_school",
    val activeQuiz: Quiz? = null,
    val activeQuestions: List<QuizQuestion> = emptyList(),
    val activeQuestionIndex: Int = 0,
    val showResultModal: Boolean = false
)

enum class QuizFilter {
    ALL,
    IN_PROGRESS,
    COMPLETED
}
