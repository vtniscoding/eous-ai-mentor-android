package com.eous.mentor.domain.usecase.session

import android.content.Context
import com.eous.mentor.domain.repository.SessionRepository
import com.eous.mentor.domain.repository.UserRepository
import java.util.UUID

/**
 * Issues a fresh session ID for the current device and publishes it remotely,
 * so any other device holding an older ID is treated as stale.
 */
class IssueLocalSessionUseCase(
    private val sessionRepository: SessionRepository,
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(context: Context, userId: String): Result<String> = runCatching {
        val newSessionId = UUID.randomUUID().toString()
        sessionRepository.saveLocalSessionId(context, newSessionId)
        userRepository.updateSessionId(userId, newSessionId).getOrThrow()
        newSessionId
    }
}
