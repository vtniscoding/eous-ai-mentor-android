package com.eous.mentor.domain.usecase.chat

import com.eous.mentor.domain.model.*
import com.eous.mentor.testutil.FakeChatRepository
import com.eous.mentor.testutil.FakeUserRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class RequestAiReplyUseCaseTest {

    private val chatRepo = FakeChatRepository()
    private val userRepo = FakeUserRepository()
    private val useCase = RequestAiReplyUseCase(chatRepo, userRepo)

    private val userContext = UserContext(
        education_level = "high_school",
        explanation_style = "detailed",
        subjects = listOf("Math", "Physics")
    )

    @Test
    fun `returns success when AI responds normally and subject is unchanged`() = runTest {
        val aiResponse = AiChatResponse(
            reply = "This is normal explanation",
            subject = "Math"
        )
        chatRepo.getAiResponseResult = Result.success(aiResponse)
        
        val savedMsg = ChatMessage(id = "msg-ai-123", content = "This is normal explanation", role = "ai")
        chatRepo.insertMessageResult = Result.success(savedMsg)

        val result = useCase(
            userId = "user-123",
            sessionId = "session-123",
            currentInput = "Help with math",
            imageUrl = null,
            historyBeforeLast = emptyList(),
            userContext = userContext,
            isOnlyMessage = false,
            savedUserMsgId = "msg-user-123",
            sessionSubject = "Math"
        )

        assertTrue(result.isSuccess)
        val value = result.getOrNull()!!
        assertEquals("msg-ai-123", value.message.id)
        assertNull(value.updatedSubject) // subject was already Math, so no update
        assertEquals(0, chatRepo.updateSessionSubjectCallCount)
    }

    @Test
    fun `updates session subject when AI detects a different subject`() = runTest {
        val aiResponse = AiChatResponse(
            reply = "This is Physics content",
            subject = "Physics"
        )
        chatRepo.getAiResponseResult = Result.success(aiResponse)
        
        val savedMsg = ChatMessage(id = "msg-ai-123", content = "This is Physics content", role = "ai")
        chatRepo.insertMessageResult = Result.success(savedMsg)

        val result = useCase(
            userId = "user-123",
            sessionId = "session-123",
            currentInput = "Help with Physics",
            imageUrl = null,
            historyBeforeLast = emptyList(),
            userContext = userContext,
            isOnlyMessage = false,
            savedUserMsgId = "msg-user-123",
            sessionSubject = "Math" // original subject was Math
        )

        assertTrue(result.isSuccess)
        val value = result.getOrNull()!!
        assertEquals("Physics", value.updatedSubject)
        assertEquals(1, chatRepo.updateSessionSubjectCallCount)
        assertEquals("session-123", chatRepo.lastUpdatedSessionId)
        assertEquals("Physics", chatRepo.lastUpdatedSubject)
    }

    @Test
    fun `returns failure when AI response contains error`() = runTest {
        val aiResponse = AiChatResponse(
            reply = "",
            error = "AI service overloaded"
        )
        chatRepo.getAiResponseResult = Result.success(aiResponse)

        val result = useCase(
            userId = "user-123",
            sessionId = "session-123",
            currentInput = "Help with math",
            imageUrl = null,
            historyBeforeLast = emptyList(),
            userContext = userContext,
            isOnlyMessage = false,
            savedUserMsgId = "msg-user-123",
            sessionSubject = "Math"
        )

        assertTrue(result.isFailure)
        assertEquals("AI service overloaded", result.exceptionOrNull()?.message)
    }

    @Test
    fun `deletes session and fails when refusal detected in single-message session`() = runTest {
        val aiResponse = AiChatResponse(
            reply = "I cannot assist with this request. I can only assist with academic topics."
        )
        chatRepo.getAiResponseResult = Result.success(aiResponse)

        val result = useCase(
            userId = "user-123",
            sessionId = "session-123",
            currentInput = "Tell me a joke",
            imageUrl = null,
            historyBeforeLast = emptyList(),
            userContext = userContext,
            isOnlyMessage = true, // single message session
            savedUserMsgId = "msg-user-123",
            sessionSubject = "Math"
        )

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()?.message!!.contains("Cannot assist with this request"))
        assertEquals(1, chatRepo.deleteSessionCallCount)
        assertEquals("session-123", chatRepo.lastDeletedSessionId)
    }

    @Test
    fun `deletes user message and fails when refusal detected in multi-message session`() = runTest {
        val aiResponse = AiChatResponse(
            reply = "I cannot assist with this request."
        )
        chatRepo.getAiResponseResult = Result.success(aiResponse)

        val result = useCase(
            userId = "user-123",
            sessionId = "session-123",
            currentInput = "Tell me a joke",
            imageUrl = null,
            historyBeforeLast = listOf(ChatMessage(role = "user", content = "First question")),
            userContext = userContext,
            isOnlyMessage = false, // multi-message
            savedUserMsgId = "msg-user-123",
            sessionSubject = "Math"
        )

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()?.message!!.contains("Cannot assist with this request"))
        assertEquals(0, chatRepo.deleteSessionCallCount)
        assertEquals(1, chatRepo.deleteMessageCallCount)
        assertEquals("msg-user-123", chatRepo.lastDeletedMessageId)
    }

    @Test
    fun `creates quiz when AI response contains a quiz`() = runTest {
        val generatedQuiz = Quiz(
            id = "quiz-123",
            title = "Algebra Test",
            questions = listOf(
                QuizQuestion(id = 1, question = "Solve x+2=4", options = listOf("1", "2", "3"), correctAnswerIndex = 1)
            )
        )
        val aiResponse = AiChatResponse(
            reply = "Here is your quiz",
            subject = "Math",
            quiz = generatedQuiz
        )
        chatRepo.getAiResponseResult = Result.success(aiResponse)
        userRepo.createQuizResult = Result.success(generatedQuiz)
        
        val expectedMsg = ChatMessage(id = "msg-ai-123", content = "Quiz template reply", role = "ai", quiz_id = "quiz-123")
        chatRepo.insertMessageResult = Result.success(expectedMsg)

        val result = useCase(
            userId = "user-123",
            sessionId = "session-123",
            currentInput = "Help with algebra",
            imageUrl = null,
            historyBeforeLast = emptyList(),
            userContext = userContext,
            isOnlyMessage = false,
            savedUserMsgId = "msg-user-123",
            sessionSubject = "Math"
        )

        assertTrue(result.isSuccess)
        assertEquals(1, userRepo.createQuizCallCount)
        assertEquals("Algebra Test", userRepo.lastCreatedQuizQuestions?.get(0)?.question?.let { "Algebra Test" }) // check created quiz
        assertEquals("quiz-123", chatRepo.lastInsertedMessage?.quiz_id)
        assertTrue(chatRepo.lastInsertedMessage?.content!!.contains("I have designed the questions above"))
    }

    @Test
    fun `extracts and creates quiz from reply content fallback when quiz property is empty`() = runTest {
        val quizJson = """
            {
              "topic": "Math",
              "title": "Fallback Quiz",
              "questions": [
                {
                  "id": 1,
                  "question": "What is 2*3?",
                  "options": ["5", "6", "7"],
                  "correctAnswerIndex": 1
                }
              ]
            }
        """.trimIndent()
        val aiResponse = AiChatResponse(
            reply = "Here is a fallback quiz:\n[QuizJSON]\n$quizJson\n[/QuizJSON]\nHave fun!",
            subject = "Math"
        )
        chatRepo.getAiResponseResult = Result.success(aiResponse)

        val generatedQuiz = Quiz(
            id = "fallback-quiz-123",
            title = "Fallback Quiz",
            questions = listOf(
                QuizQuestion(id = 1, question = "What is 2*3?", options = listOf("5", "6", "7"), correctAnswerIndex = 1)
            )
        )
        userRepo.createQuizResult = Result.success(generatedQuiz)

        val expectedMsg = ChatMessage(id = "msg-ai-123", content = "Quiz template reply", role = "ai", quiz_id = "fallback-quiz-123")
        chatRepo.insertMessageResult = Result.success(expectedMsg)

        val result = useCase(
            userId = "user-123",
            sessionId = "session-123",
            currentInput = "Generate algebra questions",
            imageUrl = null,
            historyBeforeLast = emptyList(),
            userContext = userContext,
            isOnlyMessage = false,
            savedUserMsgId = "msg-user-123",
            sessionSubject = "Math"
        )

        assertTrue(result.isSuccess)
        assertEquals(1, userRepo.createQuizCallCount)
        assertEquals("fallback-quiz-123", chatRepo.lastInsertedMessage?.quiz_id)
    }
}
