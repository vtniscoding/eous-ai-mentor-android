package com.eous.mentor.domain.usecase.friend

import com.eous.mentor.domain.model.Profile
import com.eous.mentor.domain.repository.UserRepository

class SearchUsersUseCase(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(query: String): Result<List<Profile>> =
        userRepository.searchUsers(query)
}
