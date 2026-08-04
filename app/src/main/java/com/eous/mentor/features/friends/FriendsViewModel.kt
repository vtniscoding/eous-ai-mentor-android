package com.eous.mentor.features.friends

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eous.mentor.di.RepositoryProvider
import com.eous.mentor.di.supabase
import com.eous.mentor.domain.model.Profile
import com.eous.mentor.domain.model.Friendship
import com.eous.mentor.domain.model.FriendshipWithProfile
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class FriendsState(
    val friends: List<Profile> = emptyList(),
    val pendingRequests: List<FriendshipWithProfile> = emptyList(),
    val allFriendships: List<Friendship> = emptyList(),
    val searchResults: List<Profile> = emptyList(),
    val searchQuery: String = "",
    val isLoading: Boolean = false,
    val isSearching: Boolean = false,
    val errorMessage: String? = null
)

class FriendsViewModel(
    private val userId: String
) : ViewModel() {

    private val _state = MutableStateFlow(FriendsState())
    val state: StateFlow<FriendsState> = _state.asStateFlow()

    init {
        loadData()
    }

    fun loadData() {
        if (userId.isEmpty()) return

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            try {
                val friendsRes = withContext(Dispatchers.IO) {
                    RepositoryProvider.userRepository.getFriendsList(userId)
                }
                val requestsRes = withContext(Dispatchers.IO) {
                    RepositoryProvider.userRepository.getPendingRequests(userId)
                }

                // Tải thêm toàn bộ các dòng friendships liên quan để phân loại trạng thái tìm kiếm
                val sentFriendships = withContext(Dispatchers.IO) {
                    supabase.from("friendships")
                        .select { filter { eq("sender_id", userId) } }
                        .decodeList<Friendship>()
                }
                val receivedFriendships = withContext(Dispatchers.IO) {
                    supabase.from("friendships")
                        .select { filter { eq("receiver_id", userId) } }
                        .decodeList<Friendship>()
                }
                val allFriendships = (sentFriendships + receivedFriendships).distinctBy { it.id }

                val friendsList = friendsRes.getOrDefault(emptyList())
                val pendingRequests = requestsRes.getOrDefault(emptyList())

                _state.update {
                    it.copy(
                        friends = friendsList,
                        pendingRequests = pendingRequests,
                        allFriendships = allFriendships,
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

    fun searchUsers(query: String) {
        _state.update { it.copy(searchQuery = query) }
        if (query.trim().isEmpty()) {
            _state.update { it.copy(searchResults = emptyList()) }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isSearching = true) }
            try {
                val res = withContext(Dispatchers.IO) {
                    RepositoryProvider.userRepository.searchUsers(query)
                }
                val list = res.getOrDefault(emptyList()).filter { it.id != userId } // Lọc đi chính mình
                _state.update {
                    it.copy(
                        searchResults = list,
                        isSearching = false
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _state.update {
                    it.copy(
                        isSearching = false,
                        errorMessage = e.localizedMessage
                    )
                }
            }
        }
    }

    fun sendFriendRequest(receiverId: String) {
        viewModelScope.launch {
            try {
                val res = withContext(Dispatchers.IO) {
                    RepositoryProvider.userRepository.sendFriendRequest(userId, receiverId)
                }
                if (res.isSuccess) {
                    loadData()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun acceptFriendRequest(senderId: String) {
        viewModelScope.launch {
            try {
                val res = withContext(Dispatchers.IO) {
                    RepositoryProvider.userRepository.acceptFriendRequest(senderId, userId)
                }
                if (res.isSuccess) {
                    loadData()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun declineOrRemoveFriendship(friendId: String) {
        viewModelScope.launch {
            try {
                val res = withContext(Dispatchers.IO) {
                    RepositoryProvider.userRepository.declineOrRemoveFriendship(userId, friendId)
                }
                if (res.isSuccess) {
                    loadData()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
