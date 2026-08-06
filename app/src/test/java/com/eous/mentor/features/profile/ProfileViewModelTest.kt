package com.eous.mentor.features.profile

import com.eous.mentor.domain.model.Profile
import com.eous.mentor.domain.usecase.profile.GetProfileUseCase
import com.eous.mentor.domain.usecase.profile.SaveOnboardingProfileUseCase
import com.eous.mentor.testutil.FakeUserRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ProfileViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    private val fakeUserRepo = FakeUserRepository()

    // Instantiate real use cases with fake repositories
    private val getProfileUseCase = GetProfileUseCase(fakeUserRepo)
    private val saveOnboardingProfileUseCase = SaveOnboardingProfileUseCase(fakeUserRepo)

    private lateinit var viewModel: ProfileViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        viewModel = ProfileViewModel(
            getProfileUseCase,
            saveOnboardingProfileUseCase
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `loadProfile success updates state with profile values`() = runTest {
        val profile = Profile(
            id = "user-123",
            education_level = "university",
            explanation_style = "step_by_step",
            subjects = listOf("Math", "Chemistry")
        )
        fakeUserRepo.getProfileResult = Result.success(profile)

        viewModel.loadProfile("user-123")
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.state.value
        assertFalse(state.isLoading)
        assertEquals("university", state.selectedLevel)
        assertEquals("step_by_step", state.selectedStyle)
        assertEquals(listOf("Math", "Chemistry"), state.selectedSubjects)
    }

    @Test
    fun `setSelectedLevel updates state`() {
        viewModel.setSelectedLevel("middle_school")
        assertEquals("middle_school", viewModel.state.value.selectedLevel)
    }

    @Test
    fun `setSelectedStyle updates state`() {
        viewModel.setSelectedStyle("short")
        assertEquals("short", viewModel.state.value.selectedStyle)
    }

    @Test
    fun `setSelectedSubjects updates state`() {
        val list = listOf("Physics")
        viewModel.setSelectedSubjects(list)
        assertEquals(list, viewModel.state.value.selectedSubjects)
    }

    @Test
    fun `saveProfile success calls onSuccess callback`() = runTest {
        fakeUserRepo.saveOnboardingProfileResult = Result.success(Unit)

        var successCalled = false
        var errorCalled = false
        viewModel.saveProfile("user-123", onSuccess = { successCalled = true }, onError = { errorCalled = true })

        testDispatcher.scheduler.advanceUntilIdle()

        assertFalse(viewModel.state.value.isSaving)
        assertTrue(successCalled)
        assertFalse(errorCalled)
    }

    @Test
    fun `saveProfile failure calls onError callback`() = runTest {
        fakeUserRepo.saveOnboardingProfileResult = Result.failure(Exception("DB error"))

        var successCalled = false
        var errorCalled = false
        viewModel.saveProfile("user-123", onSuccess = { successCalled = true }, onError = { errorCalled = true })

        testDispatcher.scheduler.advanceUntilIdle()

        assertFalse(viewModel.state.value.isSaving)
        assertFalse(successCalled)
        assertTrue(errorCalled)
    }
}
