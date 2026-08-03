package com.eous.mentor.features.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eous.mentor.di.RepositoryProvider
import com.eous.mentor.domain.usecase.auth.LogoutUseCase
import com.eous.mentor.domain.usecase.home.GetHomeStatsUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class HomeViewModel(
    private val userId: String,
    private val getHomeStatsUseCase: GetHomeStatsUseCase = GetHomeStatsUseCase(RepositoryProvider.userRepository, RepositoryProvider.chatRepository),
    private val logoutUseCase: LogoutUseCase = LogoutUseCase(RepositoryProvider.authRepository)
) : ViewModel() {
    private val _state = MutableStateFlow(HomeState())
    val state: StateFlow<HomeState> = _state.asStateFlow()

    private var isInitialLoadDone = false

    init {
        // Set initial display name from cached local session to prevent flickering
        val cachedEmail = RepositoryProvider.sessionRepository.getCurrentUserEmail()
        if (!cachedEmail.isNullOrEmpty()) {
            val initialName = cachedEmail.substringBefore("@").replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
            _state.update {
                it.copy(stats = it.stats.copy(displayName = initialName))
            }
        }
        loadDashboardStats()
    }

    fun loadDashboardStats() {
        if (userId.isEmpty()) {
            _state.update { it.copy(isLoading = false) }
            return
        }

        viewModelScope.launch {
            val startTime = System.currentTimeMillis()
            if (!isInitialLoadDone) {
                _state.update { it.copy(isLoading = true) }
            }

            getHomeStatsUseCase(userId)
                .onSuccess { fetchedStats ->
                    if (!isInitialLoadDone) {
                        val elapsedTime = System.currentTimeMillis() - startTime
                        val minDurationMs = 2600L
                        if (elapsedTime < minDurationMs) {
                            kotlinx.coroutines.delay(minDurationMs - elapsedTime)
                        }
                    }
                    isInitialLoadDone = true
                    _state.update { it.copy(stats = fetchedStats, isLoading = false) }
                }
                .onFailure { e ->
                    e.printStackTrace()
                    _state.update { it.copy(isLoading = false) }
                }
        }
    }

    fun logout(onSuccess: () -> Unit, onError: () -> Unit) {
        viewModelScope.launch {
            logoutUseCase()
                .onSuccess {
                    _state.update { it.copy(isLoggedOut = true) }
                    onSuccess()
                }
                .onFailure { e ->
                    e.printStackTrace()
                    onError()
                }
        }
    }
}
