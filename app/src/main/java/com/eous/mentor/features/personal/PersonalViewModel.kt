package com.eous.mentor.features.personal

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eous.mentor.di.RepositoryProvider
import com.eous.mentor.di.UseCaseProvider
import com.eous.mentor.domain.model.DashboardStats
import com.eous.mentor.domain.model.Profile
import com.eous.mentor.domain.usecase.profile.DeleteAvatarUseCase
import com.eous.mentor.domain.usecase.profile.GetProfileUseCase
import com.eous.mentor.domain.usecase.profile.UploadAvatarUseCase
import com.eous.mentor.domain.usecase.progress.GetProgressStatsUseCase
import com.eous.mentor.domain.usecase.friend.GetFriendsListUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
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
    val friendsWithXp: List<Pair<Profile, Int>> = emptyList(),
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false
)

class PersonalViewModel(
    private val userId: String,
    private val getProgressStatsUseCase: GetProgressStatsUseCase = UseCaseProvider.getProgressStats,
    private val getProfileUseCase: GetProfileUseCase = UseCaseProvider.getProfile,
    private val uploadAvatarUseCase: UploadAvatarUseCase = UseCaseProvider.uploadAvatar,
    private val deleteAvatarUseCase: DeleteAvatarUseCase = UseCaseProvider.deleteAvatar,
    private val getFriendsListUseCase: GetFriendsListUseCase = UseCaseProvider.getFriendsList
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
                    getProfileUseCase(userId)
                }
                val fetchedProfile = profRes.getOrNull()

                val statsRes = withContext(Dispatchers.IO) {
                    getProgressStatsUseCase(userId)
                }
                val fetchedStats = statsRes.getOrNull()

                val friendsRes = withContext(Dispatchers.IO) {
                    getFriendsListUseCase(userId)
                }
                val fetchedFriends = friendsRes.getOrDefault(emptyList())

                // Map friends to their XP points directly from their Profile record
                val friendsWithXpList = fetchedFriends.map { friend ->
                    friend to friend.xp
                }

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
                        friendsWithXp = friendsWithXpList,
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
                    uploadAvatarUseCase(userId, imageBytes)
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
                    deleteAvatarUseCase(userId)
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
