package com.eous.mentor.domain.usecase.home

import com.eous.mentor.domain.model.*
import com.eous.mentor.testutil.FakeChatRepository
import com.eous.mentor.testutil.FakeSessionRepository
import com.eous.mentor.testutil.FakeUserRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class GetHomeStatsUseCaseTest {

    private val userRepo = FakeUserRepository()
    private val chatRepo = FakeChatRepository()
    private val sessionRepo = FakeSessionRepository()
    private val useCase = GetHomeStatsUseCase(userRepo, chatRepo, sessionRepo)

    private val userId = "user-123"

    @Test
    fun `calculates XP correctly and formats education level`() = runTest {
        // totalQueries * 10 + libraryItems * 20
        // We mock 3 user queries
        val messages = listOf(
            Message(role = "user", content = "Q1"),
            Message(role = "ai", content = "A1"),
            Message(role = "user", content = "Q2"),
            Message(role = "user", content = "Q3")
        )
        chatRepo.getLegacyMessagesResult = Result.success(messages)

        // 2 library items (bookmarks)
        val bookmarks = listOf(
            Bookmark(id = "b1", message_id = "msg1", user_id = userId),
            Bookmark(id = "b2", message_id = "msg2", user_id = userId)
        )
        userRepo.getBookmarksResult = Result.success(bookmarks)

        // Mock profile and activity
        val profile = Profile(id = userId, display_name = "Alice", education_level = "university")
        userRepo.getProfileResult = Result.success(profile)

        val result = useCase(userId)

        assertTrue(result.isSuccess)
        val homeData = result.getOrNull()!!

        assertEquals("Alice", homeData.displayName)
        assertEquals("University / College", homeData.educationLevel)
        
        // XP should be: 3 * 10 + 2 * 20 = 70
        assertEquals(70, homeData.xp)
        assertEquals(70, userRepo.lastUpdatedXp)
        assertEquals(1, userRepo.updateUserXpCallCount)
    }

    @Test
    fun `falls back to email prefix for displayName when display_name is blank`() = runTest {
        chatRepo.getLegacyMessagesResult = Result.success(emptyList())
        userRepo.getBookmarksResult = Result.success(emptyList())

        // No profile name set
        userRepo.getProfileResult = Result.success(Profile(id = userId, display_name = ""))
        sessionRepo.setUserEmail("john.doe@gmail.com")

        val result = useCase(userId)

        assertTrue(result.isSuccess)
        val homeData = result.getOrNull()!!
        assertEquals("John.doe", homeData.displayName) // formatted with capitalization
    }

    @Test
    fun `detects 3-day streak achievement`() = runTest {
        chatRepo.getLegacyMessagesResult = Result.success(emptyList())
        userRepo.getBookmarksResult = Result.success(emptyList())

        // Old streak was 2
        userRepo.getProfileResult = Result.success(Profile(id = userId, current_streak = 2))
        // New recorded streak is 3
        userRepo.recordUserActivityResult = Result.success(Profile(id = userId, current_streak = 3))

        val result = useCase(userId)

        assertTrue(result.isSuccess)
        val homeData = result.getOrNull()!!
        assertEquals(3, homeData.streak)
        assertTrue(homeData.isStreak3Achieved)
        assertFalse(homeData.isStreakLost)
    }

    @Test
    fun `detects streak loss`() = runTest {
        chatRepo.getLegacyMessagesResult = Result.success(emptyList())
        userRepo.getBookmarksResult = Result.success(emptyList())

        // Old streak was 5
        userRepo.getProfileResult = Result.success(Profile(id = userId, current_streak = 5))
        // New streak resets to 1
        userRepo.recordUserActivityResult = Result.success(Profile(id = userId, current_streak = 1))

        val result = useCase(userId)

        assertTrue(result.isSuccess)
        val homeData = result.getOrNull()!!
        assertEquals(1, homeData.streak)
        assertFalse(homeData.isStreak3Achieved)
        assertTrue(homeData.isStreakLost)
    }

    @Test
    fun `gracefully falls back when profile request fails`() = runTest {
        chatRepo.getLegacyMessagesResult = Result.success(emptyList())
        userRepo.getBookmarksResult = Result.success(emptyList())
        
        val exception = Exception("Remote DB is down")
        userRepo.getProfileResult = Result.failure(exception)
        sessionRepo.setUserEmail("test@test.com")

        val result = useCase(userId)

        assertTrue(result.isSuccess)
        val homeData = result.getOrNull()!!
        assertEquals("Test", homeData.displayName)
        assertEquals("Not Set", homeData.educationLevel)
    }
}
