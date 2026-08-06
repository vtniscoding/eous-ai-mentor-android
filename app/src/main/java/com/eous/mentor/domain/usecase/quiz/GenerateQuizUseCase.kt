package com.eous.mentor.domain.usecase.quiz

import com.eous.mentor.core.util.QuizParser
import com.eous.mentor.domain.model.Quiz
import com.eous.mentor.domain.model.QuizQuestion
import com.eous.mentor.domain.model.UserContext
import com.eous.mentor.domain.repository.ChatRepository
import com.eous.mentor.domain.repository.UserRepository

class GenerateQuizUseCase(
    private val userRepository: UserRepository,
    private val chatRepository: ChatRepository
) {
    suspend operator fun invoke(
        userId: String,
        topic: String,
        prompt: String,
        totalQuestions: Int = 5,
        difficulty: String? = null
    ): Result<Quiz> = runCatching {
        val userEducationLevel =
            userRepository.getProfile(userId).getOrNull()?.education_level ?: "high_school"
        val quizDifficulty =
            if (difficulty.isNullOrBlank() || difficulty == "medium") userEducationLevel
            else difficulty

        val userTopic = topic.ifBlank { "General" }
        val userPrompt = prompt.ifBlank { userTopic }
        val aiMessagePrompt =
            "Tạo bài tập trắc nghiệm $totalQuestions câu hỏi môn $userTopic " +
                "độ khó $quizDifficulty với chủ đề: $userPrompt"

        val userContext = UserContext(
            education_level = userEducationLevel,
            explanation_style = "detailed",
            subjects = emptyList()
        )

        val aiResponse =
            chatRepository.getAiResponse(aiMessagePrompt, emptyList(), null, userContext)
                .getOrNull()

        // Prefer the quiz parsed by the Edge Function; otherwise re-parse the reply
        // client-side before falling back to locally generated questions.
        val aiGeneratedQuiz = aiResponse?.quiz?.takeIf { it.questions.isNotEmpty() }
            ?: aiResponse?.reply?.let { QuizParser.extractFromReply(it)?.first }

        val questions = aiGeneratedQuiz?.questions?.ifEmpty { null }
            ?: fallbackQuestions(userTopic, userPrompt, totalQuestions)
        val quizTitle = aiGeneratedQuiz?.title?.ifBlank { null } ?: userPrompt

        userRepository.createQuiz(
            userId = userId,
            topic = userTopic,
            title = quizTitle,
            totalQuestions = questions.size,
            questions = questions,
            difficulty = quizDifficulty
        ).getOrThrow()
    }

    private fun fallbackQuestions(
        topic: String,
        prompt: String,
        count: Int
    ): List<QuizQuestion> = List(count) { i ->
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
