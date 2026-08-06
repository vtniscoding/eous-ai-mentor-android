package com.eous.mentor.domain.usecase.auth

import com.eous.mentor.testutil.FakeAuthRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class LoginUseCaseTest {

    private val fakeRepo = FakeAuthRepository()
    private val useCase = LoginUseCase(fakeRepo)

    @Test
    fun `delegates to repository and returns success`() = runTest {
        fakeRepo.loginResult = Result.success(Unit)

        val result = useCase("student@test.com", "password123")

        assertTrue(result.isSuccess)
        assertEquals(1, fakeRepo.loginCallCount)
        assertEquals("student@test.com", fakeRepo.lastLoginEmail)
        assertEquals("password123", fakeRepo.lastLoginPassword)
    }

    @Test
    fun `delegates to repository and forwards failure`() = runTest {
        val exception = Exception("Invalid credentials")
        fakeRepo.loginResult = Result.failure(exception)

        val result = useCase("student@test.com", "wrong")

        assertTrue(result.isFailure)
        assertEquals(exception, result.exceptionOrNull())
        assertEquals(1, fakeRepo.loginCallCount)
    }
}
