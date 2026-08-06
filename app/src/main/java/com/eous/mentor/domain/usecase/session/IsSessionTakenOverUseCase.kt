package com.eous.mentor.domain.usecase.session

import android.content.Context
import com.eous.mentor.domain.repository.SessionRepository

/**
 * Compares the locally stored session ID against the remote one to detect
 * that the account has been signed in on another device.
 */
class IsSessionTakenOverUseCase(
    private val sessionRepository: SessionRepository
) {
    operator fun invoke(
        context: Context,
        remoteSessionId: String?,
        treatMissingLocalAsTakenOver: Boolean
    ): Boolean {
        if (remoteSessionId.isNullOrEmpty()) return false
        val localSessionId = sessionRepository.getLocalSessionId(context)
        if (localSessionId.isEmpty()) return treatMissingLocalAsTakenOver
        return localSessionId != remoteSessionId
    }
}
