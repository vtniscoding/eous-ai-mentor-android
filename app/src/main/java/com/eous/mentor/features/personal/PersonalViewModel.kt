package com.eous.mentor.features.personal

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eous.mentor.di.RepositoryProvider
import com.eous.mentor.domain.model.DashboardStats
import com.eous.mentor.domain.model.Profile
import com.eous.mentor.domain.usecase.progress.GetProgressStatsUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class PersonalState(
    val profile: Profile? = null,
    val dashboardStats: DashboardStats? = null,
    val cachedEmail: String = "",
    val displayName: String = "User",
    val displayEmail: String = "",
    val initials: String = "U",
    val friends: List<Profile> = emptyList(),
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false
)

class PersonalViewModel(
    private val userId: String,
    private val getProgressStatsUseCase: GetProgressStatsUseCase = GetProgressStatsUseCase(
        RepositoryProvider.userRepository,
        RepositoryProvider.chatRepository
    )
) : ViewModel() {

    private val _state = MutableStateFlow(PersonalState())
    val state: StateFlow<PersonalState> = _state.asStateFlow()

    init {
        // Pre-fill initial email/name from local session cache immediately (0s delay)
        val cachedEmail = RepositoryProvider.sessionRepository.getCurrentUserEmail() ?: ""
        val defaultName = if (cachedEmail.isNotEmpty()) {
            cachedEmail.substringBefore("@").replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
        } else "User"

        val initialInitials = defaultName.split(" ")
            .filter { it.isNotEmpty() }
            .map { it.first().uppercase() }
            .joinToString("")
            .take(2)
            .ifEmpty { "U" }

        _state.update {
            it.copy(
                cachedEmail = cachedEmail,
                displayName = defaultName,
                displayEmail = cachedEmail,
                initials = initialInitials,
                isLoading = false
            )
        }

        // Start pre-loading user profile & stats in background as soon as session starts
        loadData()
    }

    fun loadData(isSilentRefresh: Boolean = false) {
        if (userId.isEmpty()) return

        viewModelScope.launch {
            val startTime = System.currentTimeMillis()
            if (isSilentRefresh) {
                _state.update { it.copy(isRefreshing = true) }
            } else if (_state.value.profile == null && _state.value.dashboardStats == null) {
                _state.update { it.copy(isLoading = true) }
            }

            try {
                val profRes = withContext(Dispatchers.IO) {
                    RepositoryProvider.userRepository.getProfile(userId)
                }
                val fetchedProfile = profRes.getOrNull()

                val statsRes = withContext(Dispatchers.IO) {
                    getProgressStatsUseCase(userId)
                }
                val fetchedStats = statsRes.getOrNull()

                val friendsRes = withContext(Dispatchers.IO) {
                    RepositoryProvider.userRepository.getFriendsList(userId)
                }
                val fetchedFriends = friendsRes.getOrDefault(emptyList())

                val name = fetchedProfile?.display_name
                    ?: fetchedProfile?.email?.substringBefore("@")
                    ?: _state.value.displayName

                val email = fetchedProfile?.email ?: _state.value.cachedEmail

                val initStr = name.split(" ")
                    .filter { it.isNotEmpty() }
                    .map { it.first().uppercase() }
                    .joinToString("")
                    .take(2)
                    .ifEmpty { "U" }

                if (!isSilentRefresh) {
                    val elapsedTime = System.currentTimeMillis() - startTime
                    val minDurationMs = 2600L
                    if (elapsedTime < minDurationMs) {
                        kotlinx.coroutines.delay(minDurationMs - elapsedTime)
                    }
                }

                _state.update {
                    it.copy(
                        profile = fetchedProfile,
                        dashboardStats = fetchedStats,
                        displayName = name,
                        displayEmail = email,
                        initials = initStr,
                        friends = fetchedFriends,
                        isLoading = false,
                        isRefreshing = false
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _state.update { it.copy(isLoading = false, isRefreshing = false) }
            }
        }
    }

    fun uploadAvatar(imageBytes: ByteArray, onSuccess: (String) -> Unit = {}) {
        viewModelScope.launch {
            try {
                val res = withContext(Dispatchers.IO) {
                    RepositoryProvider.userRepository.uploadAvatar(userId, imageBytes)
                }
                res.getOrNull()?.let { newAvatarUrl ->
                    _state.update {
                        it.copy(profile = it.profile?.copy(avatar_url = newAvatarUrl))
                    }
                    onSuccess(newAvatarUrl)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun deleteAvatar(onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    RepositoryProvider.userRepository.deleteAvatar(userId)
                }
                _state.update {
                    it.copy(profile = it.profile?.copy(avatar_url = null))
                }
                onSuccess()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
