package com.eous.mentor.domain.usecase.auth

import com.eous.mentor.testutil.FakeAuthRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class LogoutUseCaseTest {

    private val fakeRepo = FakeAuthRepository()
    private val useCase = LogoutUseCase(fakeRepo)

    @Test
    fun `delegates to repository and returns success`() = runTest {
        fakeRepo.logoutResult = Result.success(Unit)

        val result = useCase()

        assertTrue(result.isSuccess)
        assertEquals(1, fakeRepo.logoutCallCount)
    }

    @Test
    fun `delegates to repository and forwards failure`() = runTest {
        val exception = Exception("Network error")
        fakeRepo.logoutResult = Result.failure(exception)

        val result = useCase()

        assertTrue(result.isFailure)
        assertEquals(exception, result.exceptionOrNull())
        assertEquals(1, fakeRepo.logoutCallCount)
    }
}
