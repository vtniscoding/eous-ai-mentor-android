package com.eous.mentor.domain.usecase.auth

import com.eous.mentor.testutil.FakeAuthRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RegisterUseCaseTest {

    private val fakeRepo = FakeAuthRepository()
    private val useCase = RegisterUseCase(fakeRepo)

    @Test
    fun `delegates to repository and returns success`() = runTest {
        fakeRepo.registerResult = Result.success(Unit)

        val result = useCase("student@test.com", "password123")

        assertTrue(result.isSuccess)
        assertEquals(1, fakeRepo.registerCallCount)
        assertEquals("student@test.com", fakeRepo.lastRegisterEmail)
    }

    @Test
    fun `delegates to repository and forwards failure`() = runTest {
        val exception = Exception("Weak password")
        fakeRepo.registerResult = Result.failure(exception)

        val result = useCase("student@test.com", "weak")

        assertTrue(result.isFailure)
        assertEquals(exception, result.exceptionOrNull())
        assertEquals(1, fakeRepo.registerCallCount)
    }
}
