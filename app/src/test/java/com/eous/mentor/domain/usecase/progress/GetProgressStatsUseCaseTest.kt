package com.eous.mentor.domain.usecase.progress

import com.eous.mentor.domain.model.*
import com.eous.mentor.testutil.FakeChatRepository
import com.eous.mentor.testutil.FakeUserRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class GetProgressStatsUseCaseTest {

    private val userRepo = FakeUserRepository()
    private val chatRepo = FakeChatRepository()
    private val useCase = GetProgressStatsUseCase(userRepo, chatRepo)

    private val userId = "user-123"

    @Test
    fun `throws exception when userId is empty`() = runTest {
        val result = useCase("")
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is IllegalArgumentException)
    }

    @Test
    fun `calculates progress stats and subject statistics correctly`() = runTest {
        val profile = Profile(
            id = userId,
            display_name = "Alice",
            email = "alice@test.com",
            current_streak = 4,
            education_level = "middle_school"
        )
        userRepo.getProfileResult = Result.success(profile)

        // Mock sessions (subject mapping)
        val sessions = listOf(
            ChatSession(id = "s1", subject = "Math"),
            ChatSession(id = "s2", subject = "Programming")
        )
        chatRepo.getSessionsResult = Result.success(sessions)

        // Mock messages to compute query counts and subjects
        val todayStr = LocalDate.now().toString()
        val messages = listOf(
            Message(role = "user", content = "Math Q", session_id = "s1", created_at = "${todayStr}T10:00:00Z"),
            Message(role = "ai", content = "Math Ans", session_id = "s1", subject = "Math"),
            Message(role = "user", content = "Programming Q", session_id = "s2", created_at = "${todayStr}T11:00:00Z"),
            Message(role = "ai", content = "Programming Ans", session_id = "s2", subject = "Programming"),
            Message(role = "user", content = "General Q", session_id = "s3", created_at = "${todayStr}T12:00:00Z"), // raw general
            Message(role = "ai", content = "General Ans", session_id = "s3", subject = "General") // ignored
        )
        chatRepo.getLegacyMessagesResult = Result.success(messages)

        // Mock bookmarks
        val bookmarks = listOf(
            Bookmark(id = "b1", message_id = "msg1", user_id = userId)
        )
        userRepo.getBookmarksResult = Result.success(bookmarks)

        val result = useCase(userId, recordActivity = false)

        assertTrue(result.isSuccess)
        val stats = result.getOrNull()!!

        assertEquals("Alice", stats.displayName)
        assertEquals("Middle School", stats.educationLevel)
        assertEquals(3, stats.totalQueries) // 3 user role messages
        assertEquals(1, stats.libraryItems) // 1 bookmark
        assertEquals(4, stats.streak)
        
        // Subject Stats: total AI subjects recorded = 2 (Math, Programming)
        // Math = 1/2 = 50%, Programming = 1/2 = 50%
        assertEquals(2, stats.subjectStats.size)
        
        val mathPct = stats.subjectStats.find { it.name == "Math" }?.percentage ?: 0
        val progPct = stats.subjectStats.find { it.name == "Programming" }?.percentage ?: 0
        assertEquals(50, mathPct)
        assertEquals(50, progPct)

        // XP calculation: 3 queries * 10 + 1 bookmark * 20 = 50
        // level = (50 / 100) + 1 = 1
        // xp = 50 % 100 = 50
        assertEquals(1, stats.level)
        assertEquals(50, stats.xp)
    }

    @Test
    fun `calculates weekly activity days correctly`() = runTest {
        val profile = Profile(id = userId, display_name = "Alice", email = "alice@test.com")
        userRepo.getProfileResult = Result.success(profile)
        chatRepo.getSessionsResult = Result.success(emptyList())

        // Create user queries mapped to specific dates
        val today = LocalDate.now()
        val dayOfWeekVal = today.dayOfWeek.value // 1 (Mon) to 7 (Sun)
        val startOfThisWeek = today.minusDays((dayOfWeekVal - 1).toLong())

        val monDateStr = startOfThisWeek.toString()
        val tueDateStr = startOfThisWeek.plusDays(1).toString()

        val messages = listOf(
            Message(role = "user", content = "Mon Q1", created_at = "${monDateStr}T10:00:00Z"),
            Message(role = "user", content = "Mon Q2", created_at = "${monDateStr}T11:00:00Z"),
            Message(role = "user", content = "Tue Q1", created_at = "${tueDateStr}T12:00:00Z")
        )
        chatRepo.getLegacyMessagesResult = Result.success(messages)

        val result = useCase(userId, recordActivity = false)

        assertTrue(result.isSuccess)
        val stats = result.getOrNull()!!

        // Mon index = 0, Tue index = 1
        assertEquals(2, stats.thisWeekActivity[0]) // Monday has 2 queries
        assertEquals(1, stats.thisWeekActivity[1]) // Tuesday has 1 query
        assertEquals(0, stats.thisWeekActivity[2]) // Wednesday has 0 queries
    }
}
