package com.eous.mentor.features.leaderboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eous.mentor.di.RepositoryProvider
import com.eous.mentor.domain.model.Profile
import com.eous.mentor.domain.usecase.progress.GetProgressStatsUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class LeaderboardEntry(
    val id: String,
    val name: String,
    val xp: Int,
    val initials: String,
    val avatarUrl: String? = null,
    val isCurrentUser: Boolean = false
)

data class LeaderboardsState(
    val entries: List<LeaderboardEntry> = emptyList(),
    val hasFriends: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

class LeaderboardsViewModel(
    private val userId: String,
    private val getProgressStatsUseCase: GetProgressStatsUseCase = GetProgressStatsUseCase(
        RepositoryProvider.userRepository,
        RepositoryProvider.chatRepository
    )
) : ViewModel() {

    private val _state = MutableStateFlow(LeaderboardsState())
    val state: StateFlow<LeaderboardsState> = _state.asStateFlow()

    init {
        loadData()
    }

    fun loadData() {
        if (userId.isEmpty()) return

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            try {
                // Fetch profile
                val profRes = withContext(Dispatchers.IO) {
                    RepositoryProvider.userRepository.getProfile(userId)
                }
                val fetchedProfile = profRes.getOrNull()

                // Fetch stats
                val statsRes = withContext(Dispatchers.IO) {
                    getProgressStatsUseCase(userId)
                }
                val fetchedStats = statsRes.getOrNull()

                // Fetch friends list
                val friendsRes = withContext(Dispatchers.IO) {
                    RepositoryProvider.userRepository.getFriendsList(userId)
                }
                val friends = friendsRes.getOrDefault(emptyList())

                val name = fetchedProfile?.display_name
                    ?: fetchedProfile?.email?.substringBefore("@")
                    ?: "User"

                val totalQueries = fetchedStats?.totalQueries ?: 0
                val libraryItems = fetchedStats?.libraryItems ?: 0
                val userXp = totalQueries * 10 + libraryItems * 20
                val avatarUrl = fetchedProfile?.avatar_url

                // Get single initial for avatar matching design
                val userInitial = name.trim().split("\\s+".toRegex()).lastOrNull()?.firstOrNull()?.uppercase() ?: "U"

                // Create entries list
                val list = mutableListOf<LeaderboardEntry>()
                list.add(LeaderboardEntry(id = userId, name = name, xp = userXp, initials = userInitial, avatarUrl = avatarUrl, isCurrentUser = true))

                // Fetch and add each real friend's XP
                friends.forEach { friend ->
                    val friendStats = withContext(Dispatchers.IO) {
                        getProgressStatsUseCase(friend.id, recordActivity = false).getOrNull()
                    }
                    val friendQueries = friendStats?.totalQueries ?: 0
                    val friendBookmarks = friendStats?.libraryItems ?: 0
                    val friendXp = friendQueries * 10 + friendBookmarks * 20
                    val friendInitial = (friend.display_name ?: friend.email?.substringBefore("@") ?: "User")
                        .trim().split("\\s+".toRegex()).lastOrNull()?.firstOrNull()?.uppercase() ?: "U"
                    list.add(
                        LeaderboardEntry(
                            id = friend.id,
                            name = friend.display_name ?: friend.email?.substringBefore("@") ?: "User",
                            xp = friendXp,
                            initials = friendInitial,
                            avatarUrl = friend.avatar_url,
                            isCurrentUser = false
                        )
                    )
                }

                // Sort by XP descending
                list.sortByDescending { it.xp }

                _state.update {
                    it.copy(
                        entries = list,
                        hasFriends = friends.isNotEmpty(),
                        isLoading = false
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
}
