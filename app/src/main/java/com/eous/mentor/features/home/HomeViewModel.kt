
package com.eous.mentor.features.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eous.mentor.di.RepositoryProvider
import com.eous.mentor.di.UseCaseProvider
import com.eous.mentor.domain.usecase.auth.LogoutUseCase
import com.eous.mentor.domain.usecase.home.GetHomeStatsUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class HomeViewModel(
    private val userId: String,
    private val getHomeStatsUseCase: GetHomeStatsUseCase = UseCaseProvider.getHomeStats,
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
        loadDashboardStats(null)
    }

    fun loadDashboardStats(context: android.content.Context? = null) {
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

                    // Check for notifications if context is provided
                    if (context != null) {
                        // 1. Check for streak notifications
                        if (fetchedStats.isStreak3Achieved) {
                            val item = com.eous.mentor.domain.model.NotificationItem(
                                id = "streak_3_${System.currentTimeMillis()}",
                                title = "You're on fire! Keep going!",
                                description = "Congratulations on starting your learning streak! Check-in daily to build consistency.",
                                time = "Just now",
                                iconRes = com.eous.mentor.R.drawable.ic_fire_eous,
                                section = "Today"
                            )
                            com.eous.mentor.core.data.repository.NotificationRepository.addNotification(context, userId, item)
                            com.eous.mentor.core.data.repository.NotificationRepository.showSystemNotification(
                                context,
                                item.title,
                                item.description,
                                "alert"
                            )
                        }
                        if (fetchedStats.isStreakLost) {
                            val item = com.eous.mentor.domain.model.NotificationItem(
                                id = "streak_lost_${System.currentTimeMillis()}",
                                title = "You lost your streak...",
                                description = "Am I not good enough or getting troubles with your motivations.",
                                time = "Just now",
                                iconRes = com.eous.mentor.R.drawable.ic_unhappy_eous,
                                section = "Today"
                            )
                            com.eous.mentor.core.data.repository.NotificationRepository.addNotification(context, userId, item)
                            com.eous.mentor.core.data.repository.NotificationRepository.showSystemNotification(
                                context,
                                item.title,
                                item.description,
                                "alert"
                            )
                        }

                        // 2. Check for friend request notifications
                        fetchedStats.pendingRequests.forEach { req ->
                            val reqId = req.id
                            if (reqId != null && !com.eous.mentor.core.data.repository.NotificationRepository.isFriendRequestShown(context, userId, reqId)) {
                                val senderName = req.sender?.display_name 
                                    ?: req.sender?.email?.substringBefore("@") 
                                    ?: "Someone"
                                val item = com.eous.mentor.domain.model.NotificationItem(
                                    id = "friend_req_$reqId",
                                    title = senderName,
                                    description = "$senderName send you friend request.",
                                    time = "Just now",
                                    isFriendRequest = true,
                                    avatarLetter = senderName.trim().firstOrNull()?.uppercase() ?: "U",
                                    section = "Recent"
                                )
                                com.eous.mentor.core.data.repository.NotificationRepository.addNotification(context, userId, item)
                                com.eous.mentor.core.data.repository.NotificationRepository.showSystemNotification(
                                    context = context,
                                    title = item.title,
                                    message = item.description,
                                    navigateToRoute = "friends?tab=2"
                                )
                                com.eous.mentor.core.data.repository.NotificationRepository.markFriendRequestAsShown(context, userId, reqId)
                            }
                        }
                    }
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
