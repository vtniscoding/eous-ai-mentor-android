package com.eous.mentor.domain.usecase.session

import com.eous.mentor.testutil.FakeUserRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GetRemoteSessionIdUseCaseTest {

    private val fakeRepo = FakeUserRepository()
    private val useCase = GetRemoteSessionIdUseCase(fakeRepo)

    @Test
    fun `returns success with session id`() = runTest {
        fakeRepo.getRemoteSessionIdResult = Result.success("session-789")

        val result = useCase("user-123")

        assertTrue(result.isSuccess)
        assertEquals("session-789", result.getOrNull())
        assertEquals(1, fakeRepo.getRemoteSessionIdCallCount)
    }

    @Test
    fun `returns failure on repository error`() = runTest {
        val exception = Exception("Network error")
        fakeRepo.getRemoteSessionIdResult = Result.failure(exception)

        val result = useCase("user-123")

        assertTrue(result.isFailure)
        assertEquals(exception, result.exceptionOrNull())
        assertEquals(1, fakeRepo.getRemoteSessionIdCallCount)
    }
}
