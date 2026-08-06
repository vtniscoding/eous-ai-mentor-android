package com.eous.mentor.domain.usecase.friend

import com.eous.mentor.domain.repository.UserRepository

class RemoveFriendshipUseCase(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(senderId: String, receiverId: String): Result<Unit> =
        userRepository.declineOrRemoveFriendship(senderId, receiverId)
}
