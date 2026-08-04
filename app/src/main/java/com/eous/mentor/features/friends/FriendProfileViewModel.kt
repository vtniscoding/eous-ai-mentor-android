package com.eous.mentor.features.friends

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eous.mentor.di.RepositoryProvider
import com.eous.mentor.di.supabase
import com.eous.mentor.domain.model.DashboardStats
import com.eous.mentor.domain.model.Friendship
import com.eous.mentor.domain.model.Profile
import com.eous.mentor.domain.usecase.progress.GetProgressStatsUseCase
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class FriendProfileState(
    val profile: Profile? = null,
    val stats: DashboardStats? = null,
    val friendship: Friendship? = null,
    val friendsCount: Int = 0,
    val isLoading: Boolean = false,
    val actionLoading: Boolean = false,
    val errorMessage: String? = null
)

class FriendProfileViewModel(
    private val currentUserId: String,
    private val targetUserId: String,
    private val getProgressStatsUseCase: GetProgressStatsUseCase = GetProgressStatsUseCase(
        RepositoryProvider.userRepository,
        RepositoryProvider.chatRepository
    )
) : ViewModel() {

    private val _state = MutableStateFlow(FriendProfileState())
    val state: StateFlow<FriendProfileState> = _state.asStateFlow()

    init {
        loadData()
    }

    fun loadData() {
        if (targetUserId.isEmpty()) return

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            try {
                // Fetch profile of target user
                val profileRes = withContext(Dispatchers.IO) {
                    RepositoryProvider.userRepository.getProfile(targetUserId)
                }
                val fetchedProfile = profileRes.getOrNull()

                // Fetch stats of target user, recordActivity = false
                val statsRes = withContext(Dispatchers.IO) {
                    getProgressStatsUseCase(targetUserId, recordActivity = false)
                }
                val fetchedStats = statsRes.getOrNull()

                // Fetch target user's friends to count them
                val friendsRes = withContext(Dispatchers.IO) {
                    RepositoryProvider.userRepository.getFriendsList(targetUserId)
                }
                val friends = friendsRes.getOrDefault(emptyList())

                // Fetch friendship relation between current and target user
                val sentFriendship = withContext(Dispatchers.IO) {
                    supabase.from("friendships")
                        .select {
                            filter {
                                eq("sender_id", currentUserId)
                                eq("receiver_id", targetUserId)
                            }
                        }
                        .decodeSingleOrNull<Friendship>()
                }
                val receivedFriendship = withContext(Dispatchers.IO) {
                    supabase.from("friendships")
                        .select {
                            filter {
                                eq("sender_id", targetUserId)
                                eq("receiver_id", currentUserId)
                            }
                        }
                        .decodeSingleOrNull<Friendship>()
                }
                val friendshipRelation = sentFriendship ?: receivedFriendship

                _state.update {
                    it.copy(
                        profile = fetchedProfile,
                        stats = fetchedStats,
                        friendship = friendshipRelation,
                        friendsCount = friends.size,
                        isLoading = false,
                        errorMessage = null
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _state.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = e.localizedMessage
                    )
                }
            }
        }
    }

    fun sendFriendRequest() {
        viewModelScope.launch {
            _state.update { it.copy(actionLoading = true) }
            try {
                val res = withContext(Dispatchers.IO) {
                    RepositoryProvider.userRepository.sendFriendRequest(currentUserId, targetUserId)
                }
                if (res.isSuccess) {
                    loadData()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _state.update { it.copy(actionLoading = false) }
            }
        }
    }

    fun acceptFriendRequest() {
        viewModelScope.launch {
            _state.update { it.copy(actionLoading = true) }
            try {
                val res = withContext(Dispatchers.IO) {
                    RepositoryProvider.userRepository.acceptFriendRequest(targetUserId, currentUserId)
                }
                if (res.isSuccess) {
                    loadData()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _state.update { it.copy(actionLoading = false) }
            }
        }
    }

    fun declineOrRemoveFriendship() {
        viewModelScope.launch {
            _state.update { it.copy(actionLoading = true) }
            try {
                val res = withContext(Dispatchers.IO) {
                    RepositoryProvider.userRepository.declineOrRemoveFriendship(currentUserId, targetUserId)
                }
                if (res.isSuccess) {
                    loadData()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _state.update { it.copy(actionLoading = false) }
            }
        }
    }
}
