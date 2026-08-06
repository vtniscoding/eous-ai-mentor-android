package com.eous.mentor.domain.model

data class FriendsOverview(
    val friends: List<Profile>,
    val pendingRequests: List<FriendshipWithProfile>,
    val suggestedUsers: List<Profile>
)
