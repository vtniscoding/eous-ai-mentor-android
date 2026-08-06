package com.eous.mentor.domain.usecase.friend

import com.eous.mentor.domain.model.FriendshipWithProfile
import com.eous.mentor.domain.model.Profile
import com.eous.mentor.testutil.FakeUserRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GetFriendsOverviewUseCaseTest {

    private val userRepo = FakeUserRepository()
    private val useCase = GetFriendsOverviewUseCase(userRepo)

    @Test
    fun `aggregates friends pending requests and suggested users successfully`() = runTest {
        val userId = "user-123"

        val friends = listOf(Profile(id = "friend-1", display_name = "Alice"))
        val pending = listOf(FriendshipWithProfile(id = "req-1", sender_id = "sender-1", receiver_id = userId, status = "pending"))
        val suggested = listOf(Profile(id = "suggest-1", display_name = "Bob"))

        userRepo.getFriendsListResult = Result.success(friends)
        userRepo.getPendingRequestsResult = Result.success(pending)
        userRepo.getSuggestedUsersResult = Result.success(suggested)

        val result = useCase(userId)

        assertTrue(result.isSuccess)
        val overview = result.getOrNull()!!

        assertEquals(friends, overview.friends)
        assertEquals(pending, overview.pendingRequests)
        assertEquals(suggested, overview.suggestedUsers)
    }

    @Test
    fun `gracefully falls back to empty list when a request fails`() = runTest {
        val userId = "user-123"
        val exception = Exception("DB error")
        
        userRepo.getFriendsListResult = Result.failure(exception)
        userRepo.getPendingRequestsResult = Result.success(emptyList())
        userRepo.getSuggestedUsersResult = Result.success(emptyList())

        val result = useCase(userId)

        assertTrue(result.isSuccess)
        val overview = result.getOrNull()!!
        assertTrue(overview.friends.isEmpty())
    }
}
