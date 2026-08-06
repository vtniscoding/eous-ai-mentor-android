package com.eous.mentor.features.auth.login

import android.content.Context
import com.eous.mentor.domain.model.Profile
import com.eous.mentor.domain.usecase.auth.LoginUseCase
import com.eous.mentor.domain.usecase.profile.GetProfileUseCase
import com.eous.mentor.domain.usecase.session.IssueLocalSessionUseCase
import com.eous.mentor.testutil.FakeAuthRepository
import com.eous.mentor.testutil.FakeSessionRepository
import com.eous.mentor.testutil.FakeUserRepository
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class LoginViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    private val fakeAuthRepo = FakeAuthRepository()
    private val fakeSessionRepo = FakeSessionRepository()
    private val fakeUserRepo = FakeUserRepository()

    // Instantiate real use cases with fake repositories
    private val loginUseCase = LoginUseCase(fakeAuthRepo)
    private val issueLocalSessionUseCase = IssueLocalSessionUseCase(fakeSessionRepo, fakeUserRepo)
    private val getProfileUseCase = GetProfileUseCase(fakeUserRepo)

    private val context = mockk<Context>()

    private lateinit var viewModel: LoginViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        viewModel = LoginViewModel(
            loginUseCase,
            issueLocalSessionUseCase,
            getProfileUseCase,
            fakeSessionRepo
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `onLogin success path triggers onSuccess callback`() = runTest {
        viewModel.onEmailChanged("test@test.com")
        viewModel.onPasswordChanged("password")

        fakeAuthRepo.loginResult = Result.success(Unit)
        fakeSessionRepo.setUserId("user-123")
        fakeUserRepo.updateSessionIdResult = Result.success(Unit)
        
        val profile = Profile(id = "user-123", avatar_url = "avatar.png")
        fakeUserRepo.getProfileResult = Result.success(profile)

        var successCalled = false
        viewModel.onLogin {
            successCalled = true
        }

        testDispatcher.scheduler.advanceUntilIdle()

        assertFalse(viewModel.state.value.isLoading)
        assertTrue(successCalled)
        assertNull(viewModel.state.value.error)
    }

    @Test
    fun `onLogin failure updates state error`() = runTest {
        viewModel.onEmailChanged("test@test.com")
        viewModel.onPasswordChanged("wrong")

        val exception = Exception("Invalid email or password")
        fakeAuthRepo.loginResult = Result.failure(exception)

        var successCalled = false
        viewModel.onLogin {
            successCalled = true
        }

        testDispatcher.scheduler.advanceUntilIdle()

        assertFalse(viewModel.state.value.isLoading)
        assertFalse(successCalled)
        assertNotNull(viewModel.state.value.error)
    }
}
