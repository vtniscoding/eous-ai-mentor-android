package com.eous.mentor.domain.repository

import kotlinx.coroutines.flow.Flow

interface SessionRepository {
    fun observeSessionStatus(): Flow<SessionState>
    fun getCurrentUserId(): String?
    fun getCurrentUserEmail(): String?
}

enum class SessionState {
    INITIALIZING,
    AUTHENTICATED,
    NOT_AUTHENTICATED
}
