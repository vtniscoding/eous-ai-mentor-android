package com.eous.mentor.features.home

import com.eous.mentor.di.RepositoryProvider
import com.eous.mentor.domain.model.HomeData
import com.eous.mentor.domain.usecase.auth.LogoutUseCase
import com.eous.mentor.domain.usecase.home.GetHomeStatsUseCase
import com.eous.mentor.testutil.FakeAuthRepository
import com.eous.mentor.testutil.FakeChatRepository
import com.eous.mentor.testutil.FakeSessionRepository
import com.eous.mentor.testutil.FakeUserRepository
import io.mockk.every
import io.mockk.mockkObject
import io.mockk.unmockkObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    private val fakeAuthRepo = FakeAuthRepository()
    private val fakeUserRepo = FakeUserRepository()
    private val fakeChatRepo = FakeChatRepository()
    private val fakeSessionRepo = FakeSessionRepository()

    // Instantiate real use cases with fake repositories
    private val getHomeStatsUseCase = GetHomeStatsUseCase(fakeUserRepo, fakeChatRepo, fakeSessionRepo)
    private val logoutUseCase = LogoutUseCase(fakeAuthRepo)

    private val userId = "user-123"

    private lateinit var viewModel: HomeViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)

        // Mock RepositoryProvider so it returns our fake session repository inside the init block of HomeViewModel
        mockkObject(RepositoryProvider)
        every { RepositoryProvider.sessionRepository } returns fakeSessionRepo

        // Set initial user info in fakeSessionRepo
        fakeSessionRepo.setUserEmail("alice@test.com")
        
        viewModel = HomeViewModel(
            userId,
            getHomeStatsUseCase,
            logoutUseCase
        )
        testDispatcher.scheduler.advanceUntilIdle()
    }

    @After
    fun tearDown() {
        unmockkObject(RepositoryProvider)
        Dispatchers.resetMain()
    }

    @Test
    fun `loadDashboardStats success updates state with HomeData`() = runTest {
        val homeData = HomeData(
            displayName = "Alice",
            streak = 5,
            xp = 250
        )
        // Stub the fake userRepository to return success for profile stats
        fakeUserRepo.getProfileResult = Result.success(com.eous.mentor.domain.model.Profile(id = userId, display_name = "Alice", current_streak = 5))
        fakeUserRepo.updateUserXpResult = Result.success(Unit)

        viewModel.loadDashboardStats(null)
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.state.value
        assertFalse(state.isLoading)
        assertEquals("Alice", state.stats.displayName)
        assertEquals(5, state.stats.streak)
    }

    @Test
    fun `loadDashboardStats failure resets loading state`() = runTest {
        fakeUserRepo.getProfileResult = Result.failure(Exception("Network error"))

        viewModel.loadDashboardStats(null)
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.state.value
        assertFalse(state.isLoading)
    }

    @Test
    fun `logout success triggers logout`() = runTest {
        fakeAuthRepo.logoutResult = Result.success(Unit)

        var successCalled = false
        viewModel.logout(
            onSuccess = { successCalled = true },
            onError = {}
        )

        testDispatcher.scheduler.advanceUntilIdle()
        assertTrue(successCalled)
        assertTrue(viewModel.state.value.isLoggedOut)
    }
}
