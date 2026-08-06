package com.eous.mentor.features.quizzes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eous.mentor.domain.model.Quiz
import com.eous.mentor.domain.model.QuizQuestion
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

import com.eous.mentor.di.UseCaseProvider
import com.eous.mentor.domain.usecase.profile.GetProfileUseCase
import com.eous.mentor.domain.usecase.quiz.*

class QuizzesViewModel(
    private val getProfileUseCase: GetProfileUseCase = UseCaseProvider.getProfile,
    private val getQuizzesUseCase: GetQuizzesUseCase = UseCaseProvider.getQuizzes,
    private val generateQuizUseCase: GenerateQuizUseCase = UseCaseProvider.generateQuiz,
    private val saveQuizProgressUseCase: SaveQuizProgressUseCase = UseCaseProvider.saveQuizProgress,
    private val completeQuizUseCase: CompleteQuizUseCase = UseCaseProvider.completeQuiz,
    private val resetQuizUseCase: ResetQuizUseCase = UseCaseProvider.resetQuiz
) : ViewModel() {

    private val _uiState = MutableStateFlow(QuizzesState())
    val uiState: StateFlow<QuizzesState> = _uiState.asStateFlow()

    fun loadQuizzes(userId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            val profileRes = getProfileUseCase(userId)
            val profile = profileRes.getOrNull()
            val subjects = profile?.subjects?.filter { it.isNotBlank() } ?: emptyList()
            val educationLevel = profile?.education_level ?: "high_school"

            val result = getQuizzesUseCase(userId)
            val loadedQuizzes = result.getOrDefault(emptyList())

            _uiState.update { 
                it.copy(
                    isLoading = false,
                    quizzes = loadedQuizzes,
                    userSubjects = subjects,
                    userEducationLevel = educationLevel
                ) 
            }
        }
    }

    fun setFilter(filter: QuizFilter) {
        _uiState.update { it.copy(selectedFilter = filter) }
    }

    fun setSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun createQuizWithAi(
        userId: String,
        topic: String,
        prompt: String,
        totalQuestions: Int = 5,
        difficulty: String? = null
    ) {
        viewModelScope.launch {
            val startTime = System.currentTimeMillis()
            _uiState.update { it.copy(isGeneratingQuiz = true) }

            val res = generateQuizUseCase(userId, topic, prompt, totalQuestions, difficulty)

            val elapsed = System.currentTimeMillis() - startTime
            val minDurationMs = 2600L
            if (elapsed < minDurationMs) kotlinx.coroutines.delay(minDurationMs - elapsed)
            
            _uiState.update { it.copy(isGeneratingQuiz = false) }

            res.onSuccess { createdQuiz ->
                loadQuizzes(userId)
                openQuiz(createdQuiz)
            }.onFailure {
                _uiState.update { it.copy(errorMessage = "Failed to create quiz in database") }
            }
        }
    }

    fun openQuiz(quiz: Quiz) {
        val questions = quiz.questions
        val initialIdx = if (quiz.status == "completed") 0 else quiz.current_question_index.coerceIn(0, (questions.size - 1).coerceAtLeast(0))
        val isCompleted = quiz.status == "completed"

        _uiState.update {
            it.copy(
                activeQuiz = quiz,
                activeQuestions = questions,
                activeQuestionIndex = initialIdx,
                showResultModal = isCompleted
            )
        }
    }

    fun selectAnswer(questionIndex: Int, answerIndex: Int) {
        _uiState.update { state ->
            val updatedQuestions = state.activeQuestions.mapIndexed { idx, q ->
                if (idx == questionIndex) q.copy(selectedAnswerIndex = answerIndex) else q
            }
            state.copy(activeQuestions = updatedQuestions)
        }
    }

    fun nextQuestion(userId: String) {
        val state = _uiState.value
        val quiz = state.activeQuiz ?: return
        val nextIdx = (state.activeQuestionIndex + 1).coerceAtMost(state.activeQuestions.size - 1)

        _uiState.update { it.copy(activeQuestionIndex = nextIdx) }

        viewModelScope.launch {
            saveQuizProgressUseCase(userId, quiz.id, nextIdx + 1, quiz.score)
        }
    }

    fun prevQuestion() {
        _uiState.update { state ->
            val prevIdx = (state.activeQuestionIndex - 1).coerceAtLeast(0)
            state.copy(activeQuestionIndex = prevIdx)
        }
    }

    fun submitQuiz(userId: String) {
        val state = _uiState.value
        val quiz = state.activeQuiz ?: return

        var correctCount = 0
        state.activeQuestions.forEach { q ->
            if (q.selectedAnswerIndex == q.correctAnswerIndex) {
                correctCount++
            }
        }

        viewModelScope.launch {
            completeQuizUseCase(userId, quiz.id, state.activeQuestions.size, correctCount)
            _uiState.update {
                it.copy(
                    activeQuiz = quiz.copy(status = "completed", score = correctCount),
                    showResultModal = true
                )
            }
            loadQuizzes(userId)
        }
    }

    fun retakeActiveQuiz(userId: String) {
        val quiz = _uiState.value.activeQuiz ?: return
        val freshQuestions = (if (quiz.questions.isNotEmpty()) quiz.questions else _uiState.value.activeQuestions)
            .map { it.copy(selectedAnswerIndex = null) }
        _uiState.update {
            it.copy(
                activeQuestions = freshQuestions,
                activeQuestionIndex = 0,
                showResultModal = false
            )
        }
        viewModelScope.launch {
            resetQuizUseCase(quiz.id)
        }
    }

    fun closeActiveQuiz(userId: String) {
        _uiState.update {
            it.copy(
                activeQuiz = null,
                activeQuestions = emptyList(),
                activeQuestionIndex = 0,
                showResultModal = false
            )
        }
        loadQuizzes(userId)
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}
