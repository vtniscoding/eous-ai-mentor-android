package com.eous.mentor.domain.model

data class FriendProfileData(
    val profile: Profile?,
    val friendsList: List<Profile>
)
