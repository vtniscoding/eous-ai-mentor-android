package com.eous.mentor.features.main

import android.content.Context
import com.eous.mentor.domain.usecase.session.GetRemoteSessionIdUseCase
import com.eous.mentor.domain.usecase.session.IsSessionTakenOverUseCase
import com.eous.mentor.domain.usecase.session.IssueLocalSessionUseCase
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
class MainViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    private val fakeSessionRepo = FakeSessionRepository()
    private val fakeUserRepo = FakeUserRepository()

    // Instantiate real use cases with fake repositories
    private val issueLocalSessionUseCase = IssueLocalSessionUseCase(fakeSessionRepo, fakeUserRepo)
    private val isSessionTakenOverUseCase = IsSessionTakenOverUseCase(fakeSessionRepo)
    private val getRemoteSessionIdUseCase = GetRemoteSessionIdUseCase(fakeUserRepo)

    private val context = mockk<Context>()

    private lateinit var viewModel: MainScreenViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        viewModel = MainScreenViewModel(
            issueLocalSessionUseCase,
            isSessionTakenOverUseCase,
            getRemoteSessionIdUseCase,
            fakeSessionRepo
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `checkSessionOnProfileLoaded issues new session when remoteSessionId is null`() = runTest {
        fakeUserRepo.updateSessionIdResult = Result.success(Unit)

        viewModel.checkSessionOnProfileLoaded(
            context = context,
            remoteSessionId = null,
            email = "test@test.com",
            avatarUrl = null,
            userId = "user-123",
            onLogout = { _, _ -> }
        )
        testDispatcher.scheduler.advanceUntilIdle()

        // Verify a new local session ID was generated and saved
        assertNotNull(fakeSessionRepo.savedLocalSessionId)
        assertEquals(1, fakeUserRepo.updateSessionIdCallCount)
    }

    @Test
    fun `checkSessionOnProfileLoaded triggers session takeover event when session is taken over`() = runTest {
        fakeSessionRepo.localSessionId = "local-123"
        fakeUserRepo.getRemoteSessionIdResult = Result.success("remote-456")

        var logoutSuccessCalled = false
        viewModel.checkSessionOnProfileLoaded(
            context = context,
            remoteSessionId = "remote-456",
            email = "test@test.com",
            avatarUrl = "avatar.png",
            userId = "user-123",
            onLogout = { onSuccess, _ ->
                onSuccess()
                logoutSuccessCalled = true
            }
        )
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(logoutSuccessCalled)
        val event = viewModel.state.value.sessionExpiredEvent
        assertNotNull(event)
        assertEquals("test@test.com", event?.email)
        assertEquals("avatar.png", event?.avatarUrl)
        assertTrue(event?.logoutSucceeded == true)
    }

    @Test
    fun `consumeSessionExpiredEvent clears the one-time event`() = runTest {
        fakeSessionRepo.localSessionId = "local-123"
        fakeUserRepo.getRemoteSessionIdResult = Result.success("remote-456")

        viewModel.checkSessionOnProfileLoaded(
            context = context,
            remoteSessionId = "remote-456",
            email = "test@test.com",
            avatarUrl = "avatar.png",
            userId = "user-123",
            onLogout = { onSuccess, _ -> onSuccess() }
        )
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.consumeSessionExpiredEvent()
        assertNull(viewModel.state.value.sessionExpiredEvent)
    }
}
