package com.eous.mentor.features.auth.login

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.*
import androidx.navigation.NavController
import androidx.test.core.app.ApplicationProvider
import com.eous.mentor.domain.usecase.auth.LoginUseCase
import com.eous.mentor.domain.usecase.profile.GetProfileUseCase
import com.eous.mentor.domain.usecase.session.IssueLocalSessionUseCase
import com.eous.mentor.testutil.FakeAuthRepository
import com.eous.mentor.testutil.FakeSessionRepository
import com.eous.mentor.testutil.FakeUserRepository
import org.junit.Rule
import org.junit.Test

class LoginScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val fakeAuthRepo = FakeAuthRepository()
    private val fakeSessionRepo = FakeSessionRepository()
    private val fakeUserRepo = FakeUserRepository()

    private val loginUseCase = LoginUseCase(fakeAuthRepo)
    private val issueLocalSessionUseCase = IssueLocalSessionUseCase(fakeSessionRepo, fakeUserRepo)
    private val getProfileUseCase = GetProfileUseCase(fakeUserRepo)

    @Test
    fun testLoginFormScreen_validationError() {
        val viewModel = LoginViewModel(
            loginUseCase,
            issueLocalSessionUseCase,
            getProfileUseCase,
            fakeSessionRepo
        )

        composeTestRule.setContent {
            LoginFormScreen(
                navController = NavController(ApplicationProvider.getApplicationContext()),
                isTablet = false,
                viewModel = viewModel
            )
        }

        // Click login immediately to trigger validation error (empty fields)
        composeTestRule.onNodeWithTag("login_button").performClick()

        // Verify error banner is shown with correct text
        composeTestRule.onNodeWithTag("error_banner").assertExists()
        composeTestRule.onNodeWithText("Email and password are required.").assertExists()
    }

    @Test
    fun testLoginFormScreen_invalidEmail() {
        val viewModel = LoginViewModel(
            loginUseCase,
            issueLocalSessionUseCase,
            getProfileUseCase,
            fakeSessionRepo
        )

        composeTestRule.setContent {
            LoginFormScreen(
                navController = NavController(ApplicationProvider.getApplicationContext()),
                isTablet = false,
                viewModel = viewModel
            )
        }

        // Input invalid email and password
        composeTestRule.onNodeWithTag("email_input").performTextInput("invalid-email")
        composeTestRule.onNodeWithTag("password_input").performTextInput("password123")

        // Click login
        composeTestRule.onNodeWithTag("login_button").performClick()

        // Verify error banner for invalid email is shown
        composeTestRule.onNodeWithTag("error_banner").assertExists()
        composeTestRule.onNodeWithText("Please enter a valid email address.").assertExists()
    }
}
