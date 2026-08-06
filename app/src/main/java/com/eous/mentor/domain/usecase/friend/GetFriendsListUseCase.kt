package com.eous.mentor.domain.usecase.friend

import com.eous.mentor.domain.model.Profile
import com.eous.mentor.domain.repository.UserRepository

class GetFriendsListUseCase(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(userId: String): Result<List<Profile>> =
        userRepository.getFriendsList(userId)
}
