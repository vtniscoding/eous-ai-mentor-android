package com.eous.mentor.features.quizzes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eous.mentor.data.repository.UserRepositoryImpl
import com.eous.mentor.domain.model.Quiz
import com.eous.mentor.domain.model.QuizQuestion
import com.eous.mentor.domain.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

import com.eous.mentor.data.repository.ChatRepositoryImpl
import com.eous.mentor.domain.repository.ChatRepository

class QuizzesViewModel(
    private val userRepository: UserRepository = UserRepositoryImpl(),
    private val chatRepository: ChatRepository = ChatRepositoryImpl()
) : ViewModel() {

    private val _uiState = MutableStateFlow(QuizzesState())
    val uiState: StateFlow<QuizzesState> = _uiState.asStateFlow()

    fun loadQuizzes(userId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            val profileRes = userRepository.getProfile(userId)
            val subjects = profileRes.getOrNull()?.subjects?.filter { it.isNotBlank() } ?: emptyList()

            val result = userRepository.getQuizzes(userId)
            val loadedQuizzes = result.getOrDefault(emptyList())

            _uiState.update { 
                it.copy(
                    isLoading = false,
                    quizzes = loadedQuizzes,
                    userSubjects = subjects
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
        difficulty: String = "medium"
    ) {
        viewModelScope.launch {
            val startTime = System.currentTimeMillis()
            _uiState.update { it.copy(isGeneratingQuiz = true) }

            val userTopic = topic.ifBlank { "General" }
            val userPrompt = prompt.ifBlank { userTopic }
            val aiMessagePrompt = "Tạo bài tập trắc nghiệm $totalQuestions câu hỏi môn $userTopic độ khó $difficulty với chủ đề: $userPrompt"

            val aiResult = chatRepository.getAiResponse(aiMessagePrompt, emptyList(), null)
            val aiGeneratedQuiz = aiResult.getOrNull()?.quiz
            val questions = aiGeneratedQuiz?.questions?.ifEmpty { null } ?: createFallbackQuestions(userTopic, userPrompt, totalQuestions)
            val quizTitle = aiGeneratedQuiz?.title?.ifBlank { null } ?: userPrompt

            val res = userRepository.createQuiz(
                userId = userId,
                topic = userTopic,
                title = quizTitle,
                totalQuestions = questions.size,
                questions = questions,
                difficulty = difficulty
            )

            val elapsedTime = System.currentTimeMillis() - startTime
            val minDurationMs = 2600L
            if (elapsedTime < minDurationMs) {
                kotlinx.coroutines.delay(minDurationMs - elapsedTime)
            }

            _uiState.update { it.copy(isGeneratingQuiz = false) }

            if (res.isSuccess) {
                val createdQuiz = res.getOrNull()
                loadQuizzes(userId)
                if (createdQuiz != null) {
                    openQuiz(createdQuiz)
                }
            } else {
                _uiState.update { it.copy(errorMessage = "Failed to create quiz in database") }
            }
        }
    }

    private fun createFallbackQuestions(topic: String, prompt: String, count: Int): List<QuizQuestion> {
        return List(count) { i ->
            QuizQuestion(
                id = i + 1,
                question = "Question ${i + 1}: What is a core concept regarding $prompt ($topic)?",
                options = listOf(
                    "Primary fundamental concept of $prompt",
                    "Secondary element of $topic",
                    "Alternative theoretical approach",
                    "None of the above"
                ),
                correctAnswerIndex = 0,
                explanation = "Option 1 is the fundamental definition for $prompt."
            )
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
            userRepository.updateQuiz(quiz.id, nextIdx + 1, quiz.score, "in_progress")
            // Record activity for study streak
            userRepository.recordUserActivity(userId)
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
            userRepository.updateQuiz(quiz.id, state.activeQuestions.size, correctCount, "completed")
            userRepository.recordUserActivity(userId)
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
            userRepository.updateQuiz(quiz.id, 0, null, "in_progress")
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
