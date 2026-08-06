package com.eous.mentor.domain.usecase.library

import com.eous.mentor.domain.model.*
import com.eous.mentor.testutil.FakeChatRepository
import com.eous.mentor.testutil.FakeUserRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.text.SimpleDateFormat
import java.util.*

class GetLibraryContentUseCaseTest {

    private val chatRepo = FakeChatRepository()
    private val userRepo = FakeUserRepository()
    private val useCase = GetLibraryContentUseCase(chatRepo, userRepo)

    private val userId = "user-123"

    private fun getUtcString(daysOffset: Int = 0): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, daysOffset)
        return "${sdf.format(cal.time)}T12:00:00Z"
    }

    @Test
    fun `calculates weekly stats and practices today correctly`() = runTest {
        val todayStr = getUtcString(0)
        val twoDaysAgoStr = getUtcString(-2)
        val tenDaysAgoStr = getUtcString(-10) // older than 7 days

        // Quizzes: one today, one ten days ago
        val quizzes = listOf(
            Quiz(id = "q1", created_at = todayStr),
            Quiz(id = "q2", created_at = tenDaysAgoStr)
        )
        userRepo.getQuizzesResult = Result.success(quizzes)

        // Bookmarks
        val bookmarks = listOf(ChatMessage(id = "msg-123", content = "Bookmarked answer", role = "ai"))
        chatRepo.getBookmarkedMessagesResult = Result.success(bookmarks)

        // Sessions:
        // Math (2 sessions within last week, 1 older)
        // Physics (1 session within last week)
        val sessions = listOf(
            ChatSession(id = "s1", subject = "Math", created_at = todayStr),
            ChatSession(id = "s2", subject = "Math", created_at = twoDaysAgoStr),
            ChatSession(id = "s3", subject = "Physics", created_at = twoDaysAgoStr),
            ChatSession(id = "s4", subject = "Math", created_at = tenDaysAgoStr) // ignored in weekly count
        )
        chatRepo.getSessionsResult = Result.success(sessions)

        val result = useCase(userId)

        assertTrue(result.isSuccess)
        val libraryContent = result.getOrNull()!!

        // uniqueSubjects should have Math, Physics
        assertEquals(2, libraryContent.subjects.size)
        assertTrue(libraryContent.subjects.contains("Math"))
        assertTrue(libraryContent.subjects.contains("Physics"))

        // Math should be the practiceSubject because it has count 2 (Physics only has 1 within last 7 days)
        assertEquals("Math", libraryContent.practiceSubject)
        assertEquals(2, libraryContent.practiceQuestionCount)
        assertTrue(libraryContent.hasPracticedToday)
    }

    @Test
    fun `hasPracticedToday is false when no quiz is created today`() = runTest {
        val yesterdayStr = getUtcString(-1)
        val quizzes = listOf(
            Quiz(id = "q1", created_at = yesterdayStr)
        )
        userRepo.getQuizzesResult = Result.success(quizzes)
        chatRepo.getSessionsResult = Result.success(emptyList())

        val result = useCase(userId)

        assertTrue(result.isSuccess)
        val libraryContent = result.getOrNull()!!
        assertFalse(libraryContent.hasPracticedToday)
    }
}
