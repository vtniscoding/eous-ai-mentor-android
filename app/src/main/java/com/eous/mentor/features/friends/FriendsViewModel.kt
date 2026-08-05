package com.eous.mentor.features.friends

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eous.mentor.di.RepositoryProvider
import com.eous.mentor.di.supabase
import com.eous.mentor.domain.model.Profile
import com.eous.mentor.domain.model.Friendship
import com.eous.mentor.domain.model.FriendshipWithProfile
import com.eous.mentor.domain.model.NotificationItem
import com.eous.mentor.core.data.repository.NotificationRepository
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class SuggestedUser(
    val id: String,
    val name: String,
    val avatarLetter: String,
    val email: String,
    val isAdded: Boolean = false
)

data class FriendsState(
    val friends: List<Profile> = emptyList(),
    val pendingRequests: List<FriendshipWithProfile> = emptyList(),
    val allFriendships: List<Friendship> = emptyList(),
    val searchResults: List<Profile> = emptyList(),
    val suggestedUsers: List<SuggestedUser> = emptyList(),
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
        // Suggestions are loaded from DB dynamically during loadData()
    }

    fun loadData(context: android.content.Context? = null) {
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

                // Tải danh sách gợi ý thực tế từ DB
                val suggestedRes = withContext(Dispatchers.IO) {
                    RepositoryProvider.userRepository.getSuggestedUsers(userId, 15)
                }
                val rawSuggested = suggestedRes.getOrDefault(emptyList())

                // Lọc bỏ những user đã có quan hệ bạn bè (đã gửi hoặc nhận request, hoặc đã kết bạn)
                val filteredSuggested = rawSuggested.filter { u ->
                    allFriendships.none { f ->
                        f.sender_id == u.id || f.receiver_id == u.id
                    }
                }.map { u ->
                    val name = u.display_name ?: u.email?.substringBefore("@") ?: "User"
                    val initial = name.trim().split("\\s+".toRegex()).lastOrNull()?.firstOrNull()?.uppercase() ?: "U"
                    SuggestedUser(
                        id = u.id,
                        name = name,
                        avatarLetter = initial,
                        email = u.email ?: "",
                        isAdded = false
                    )
                }

                _state.update {
                    it.copy(
                        friends = friendsList,
                        pendingRequests = pendingRequests,
                        allFriendships = allFriendships,
                        suggestedUsers = filteredSuggested,
                        isLoading = false
                    )
                }

                // Tự động kiểm tra friend request mới
                if (context != null && pendingRequests.isNotEmpty()) {
                    pendingRequests.forEach { req ->
                        val reqId = req.id
                        if (reqId != null && !NotificationRepository.isFriendRequestShown(context, userId, reqId)) {
                            val senderName = req.sender?.display_name 
                                ?: req.sender?.email?.substringBefore("@") 
                                ?: "Someone"
                            val item = NotificationItem(
                                id = "friend_req_$reqId",
                                title = senderName,
                                description = "$senderName send you friend request.",
                                time = "Just now",
                                isFriendRequest = true,
                                avatarLetter = senderName.trim().firstOrNull()?.uppercase() ?: "U",
                                section = "Recent"
                            )
                            NotificationRepository.addNotification(context, userId, item)
                            NotificationRepository.showSystemNotification(
                                context = context,
                                title = item.title,
                                message = item.description,
                                navigateToRoute = "friends?tab=2"
                            )
                            NotificationRepository.markFriendRequestAsShown(context, userId, reqId)
                        }
                    }
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

    fun dismissSuggested(id: String) {
        _state.update {
            it.copy(suggestedUsers = it.suggestedUsers.filter { u -> u.id != id })
        }
    }

    fun addSuggestedFriend(id: String) {
        _state.update {
            it.copy(suggestedUsers = it.suggestedUsers.map { u ->
                if (u.id == id) u.copy(isAdded = true) else u
            })
        }
        sendFriendRequest(id)
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
