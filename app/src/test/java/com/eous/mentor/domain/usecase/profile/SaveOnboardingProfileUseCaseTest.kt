package com.eous.mentor.domain.usecase.profile

import com.eous.mentor.testutil.FakeUserRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SaveOnboardingProfileUseCaseTest {

    private val userRepo = FakeUserRepository()
    private val useCase = SaveOnboardingProfileUseCase(userRepo)

    @Test
    fun `delegates to repository and returns success`() = runTest {
        userRepo.saveOnboardingProfileResult = Result.success(Unit)

        val result = useCase(
            userId = "user-123",
            educationLevel = "high_school",
            explanationStyle = "detailed",
            subjects = listOf("Math", "Physics")
        )

        assertTrue(result.isSuccess)
        assertEquals(1, userRepo.saveOnboardingProfileCallCount)
        assertEquals("user-123", userRepo.lastOnboardingUserId)
        assertEquals("high_school", userRepo.lastOnboardingEducationLevel)
        assertEquals("detailed", userRepo.lastOnboardingExplanationStyle)
        assertEquals(listOf("Math", "Physics"), userRepo.lastOnboardingSubjects)
    }

    @Test
    fun `delegates to repository and forwards failure`() = runTest {
        val exception = Exception("Failed to save profile")
        userRepo.saveOnboardingProfileResult = Result.failure(exception)

        val result = useCase(
            userId = "user-123",
            educationLevel = "high_school",
            explanationStyle = "detailed",
            subjects = listOf("Math", "Physics")
        )

        assertTrue(result.isFailure)
        assertEquals(exception, result.exceptionOrNull())
        assertEquals(1, userRepo.saveOnboardingProfileCallCount)
    }
}
