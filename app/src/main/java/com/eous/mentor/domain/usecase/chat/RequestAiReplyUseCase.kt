package com.eous.mentor.domain.usecase.chat

import com.eous.mentor.core.util.QuizParser
import com.eous.mentor.domain.model.AnswerParser
import com.eous.mentor.domain.model.AnswerType
import com.eous.mentor.domain.model.ChatMessage
import com.eous.mentor.domain.model.UserContext
import com.eous.mentor.domain.repository.ChatRepository
import com.eous.mentor.domain.repository.UserRepository

data class AiReplyResult(
    val message: ChatMessage,
    val updatedSubject: String?
)

class RequestAiReplyUseCase(
    private val chatRepository: ChatRepository,
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(
        userId: String,
        sessionId: String,
        currentInput: String,
        imageUrl: String?,
        historyBeforeLast: List<ChatMessage>,
        userContext: UserContext,
        isOnlyMessage: Boolean,
        savedUserMsgId: String?,
        sessionSubject: String
    ): Result<AiReplyResult> = runCatching {
        
        val aiResponse = chatRepository.getAiResponse(
            message = currentInput,
            history = historyBeforeLast,
            imageUrl = imageUrl,
            userContext = userContext
        ).getOrThrow()

        if (!aiResponse.error.isNullOrBlank()) {
            throw Exception(aiResponse.error)
        }

        // Detect Refusal
        val parsed = AnswerParser.parse(aiResponse.reply, aiResponse.subject)
        if (parsed.type == AnswerType.REFUSAL) {
            if (isOnlyMessage) {
                chatRepository.deleteSession(sessionId)
            } else {
                savedUserMsgId?.let { msgId ->
                    chatRepository.deleteMessage(msgId)
                }
            }
            throw Exception("Cannot assist with this request. I can only assist with academic and study-related queries.")
        }

        // Check if AI generated a Quiz
        var createdQuizId: String? = null
        var effectiveQuiz = aiResponse.quiz
        var effectiveReply = aiResponse.reply

        if (effectiveQuiz == null || effectiveQuiz.questions.isEmpty()) {
            val fallbackResult = QuizParser.extractFromReply(effectiveReply)
            if (fallbackResult != null) {
                effectiveQuiz = fallbackResult.first
                effectiveReply = fallbackResult.second
            }
        }

        if (effectiveQuiz != null && effectiveQuiz.questions.isNotEmpty()) {
            val topicName = aiResponse.subject ?: effectiveQuiz.topic.ifBlank { "General" }
            val quizTitle = effectiveQuiz.title.ifBlank { "Bài tập $topicName" }
            val quizDifficulty = userContext.education_level
            val createRes = userRepository.createQuiz(
                userId = userId,
                topic = topicName,
                title = quizTitle,
                totalQuestions = effectiveQuiz.questions.size,
                questions = effectiveQuiz.questions,
                difficulty = quizDifficulty
            )
            if (createRes.isSuccess) {
                createdQuizId = createRes.getOrNull()?.id
            }
        }

        // Insert AI response into DB
        val replyContent = if (createdQuizId != null) {
            "I have designed the questions above to help you review your knowledge. Don't hesitate to try your best, as every mistake is an opportunity to learn even more deeply. Happy learning, and keep up your eager spirit!"
        } else {
            effectiveReply
        }
        val aiMsg = ChatMessage(
            user_id = userId,
            session_id = sessionId,
            role = "ai",
            content = replyContent,
            subject = aiResponse.subject,
            quiz_id = createdQuizId
        )
        val savedAiMsg = chatRepository.insertMessage(aiMsg).getOrThrow()
        
        val recognizedSubject = aiResponse.subject ?: "General"
        val updatedSubject = if (sessionSubject != recognizedSubject) {
            chatRepository.updateSessionSubject(sessionId, recognizedSubject)
            recognizedSubject
        } else null
        
        AiReplyResult(savedAiMsg, updatedSubject)
    }
}
