package com.eous.mentor.domain.repository

import kotlinx.coroutines.flow.Flow

interface SessionRepository {
    fun observeSessionStatus(): Flow<SessionState>
    fun getCurrentUserId(): String?
    fun getCurrentUserEmail(): String?
    fun getLocalSessionId(context: android.content.Context): String
    fun saveLocalSessionId(context: android.content.Context, sessionId: String)
    fun clearLocalSessionId(context: android.content.Context)
}

enum class SessionState {
    INITIALIZING,
    AUTHENTICATED,
    NOT_AUTHENTICATED
}
