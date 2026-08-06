package com.eous.mentor.domain.usecase.friend

import com.eous.mentor.domain.model.FriendsOverview
import com.eous.mentor.domain.repository.UserRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

class GetFriendsOverviewUseCase(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(userId: String): Result<FriendsOverview> = coroutineScope {
        runCatching {
            val friendsDeferred = async { userRepository.getFriendsList(userId).getOrDefault(emptyList()) }
            val requestsDeferred = async { userRepository.getPendingRequests(userId).getOrDefault(emptyList()) }
            val suggestedDeferred = async { userRepository.getSuggestedUsers(userId, 15).getOrDefault(emptyList()) }

            FriendsOverview(
                friends = friendsDeferred.await(),
                pendingRequests = requestsDeferred.await(),
                suggestedUsers = suggestedDeferred.await()
            )
        }
    }
}
