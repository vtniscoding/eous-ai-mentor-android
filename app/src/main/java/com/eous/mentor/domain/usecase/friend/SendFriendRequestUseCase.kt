package com.eous.mentor.domain.usecase.friend

import com.eous.mentor.domain.repository.UserRepository

class SendFriendRequestUseCase(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(senderId: String, receiverId: String): Result<Unit> =
        userRepository.sendFriendRequest(senderId, receiverId)
}
