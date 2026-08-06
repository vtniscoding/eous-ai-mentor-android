package com.eous.mentor.domain.usecase.friend

import com.eous.mentor.domain.model.FriendProfileData
import com.eous.mentor.domain.repository.UserRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

class GetFriendProfileUseCase(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(userId: String): Result<FriendProfileData> = coroutineScope {
        runCatching {
            val profileDeferred = async { userRepository.getProfile(userId).getOrNull() }
            val friendsDeferred = async { userRepository.getFriendsList(userId).getOrDefault(emptyList()) }

            FriendProfileData(
                profile = profileDeferred.await(),
                friendsList = friendsDeferred.await()
            )
        }
    }
}
