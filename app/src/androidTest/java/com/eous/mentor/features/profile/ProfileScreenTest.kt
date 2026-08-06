package com.eous.mentor.features.profile

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.*
import com.eous.mentor.domain.usecase.profile.GetProfileUseCase
import com.eous.mentor.domain.usecase.profile.SaveOnboardingProfileUseCase
import com.eous.mentor.testutil.FakeUserRepository
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class ProfileScreenTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private val fakeUserRepo = FakeUserRepository()

    private val getProfileUseCase = GetProfileUseCase(fakeUserRepo)
    private val saveOnboardingProfileUseCase = SaveOnboardingProfileUseCase(fakeUserRepo)

    @Test
    fun testProfileScreen_saveOnboardingComplete() {
        val viewModel = ProfileViewModel(
            getProfileUseCase,
            saveOnboardingProfileUseCase
        )

        var onboardingCompleted = false

        composeTestRule.setContent {
            ProfileScreen(
                userId = "user-123",
                isForceOnboarding = true,
                onBack = {},
                viewModel = viewModel,
                onComplete = {
                    onboardingCompleted = true
                }
            )
        }

        // Wait for profile loading to finish and screen contents to render
        composeTestRule.waitForIdle()

        // 1. Click Education Level option (Middle School)
        composeTestRule.onNodeWithTag("edu_option_middle_school").performScrollTo().performClick()

        // 2. Click Subject Option tag (Math)
        composeTestRule.onNodeWithTag("subject_option_Math").performScrollTo().performClick()

        // 3. Click Explanation Style option (Step-by-step)
        composeTestRule.onNodeWithTag("style_option_step_by_step").performScrollTo().performClick()

        // Configure fake repository to return success for save onboarding profile
        fakeUserRepo.saveOnboardingProfileResult = Result.success(Unit)

        // 4. Click Complete button
        composeTestRule.onNodeWithTag("complete_button").performScrollTo().performClick()

        // Wait for idle to process background coroutines
        composeTestRule.waitForIdle()

        // Verify that the onComplete callback was executed successfully
        assertTrue(onboardingCompleted)
        org.junit.Assert.assertEquals(1, fakeUserRepo.saveOnboardingProfileCallCount)
    }
}
