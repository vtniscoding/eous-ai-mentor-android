package com.eous.mentor.domain.usecase.session

import com.eous.mentor.testutil.FakeSessionRepository
import com.eous.mentor.testutil.FakeUserRepository
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.UUID

class IssueLocalSessionUseCaseTest {

    private val fakeSessionRepo = FakeSessionRepository()
    private val fakeUserRepo = FakeUserRepository()
    private val useCase = IssueLocalSessionUseCase(fakeSessionRepo, fakeUserRepo)
    private val context = mockk<android.content.Context>()

    @Test
    fun `issues local session and syncs to remote`() = runTest {
        fakeUserRepo.updateSessionIdResult = Result.success(Unit)

        val result = useCase(context, "user-123")

        assertTrue(result.isSuccess)
        val sessionId = result.getOrNull()
        assertNotNull(sessionId)
        // Verify valid UUID
        UUID.fromString(sessionId)
        // Verify local was saved
        assertEquals(sessionId, fakeSessionRepo.savedLocalSessionId)
        // Verify remote was synced
        assertEquals(1, fakeUserRepo.updateSessionIdCallCount)
        assertEquals(sessionId, fakeUserRepo.lastUpdatedSessionId)
    }

    @Test
    fun `returns failure when remote sync fails`() = runTest {
        val exception = Exception("Failed to sync")
        fakeUserRepo.updateSessionIdResult = Result.failure(exception)

        val result = useCase(context, "user-123")

        assertTrue(result.isFailure)
        // The exception is wrapped by runCatching, so check message
        assertEquals("Failed to sync", result.exceptionOrNull()?.message)
    }
}
