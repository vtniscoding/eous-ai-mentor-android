package com.eous.mentor.testutil

import android.content.Context
import com.eous.mentor.domain.repository.SessionRepository
import com.eous.mentor.domain.repository.SessionState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

/**
 * Fake implementation of [SessionRepository] for testing.
 */
class FakeSessionRepository : SessionRepository {

    var localSessionId: String = ""
    var savedLocalSessionId: String? = null; private set
    private var _currentUserId: String? = null
    private var _currentUserEmail: String? = null

    fun setUserId(id: String?) { _currentUserId = id }
    fun setUserEmail(email: String?) { _currentUserEmail = email }

    override fun observeSessionStatus(): Flow<SessionState> = flowOf(SessionState.AUTHENTICATED)
    override fun getCurrentUserId(): String? = _currentUserId
    override fun getCurrentUserEmail(): String? = _currentUserEmail

    override fun getLocalSessionId(context: Context): String = localSessionId

    override fun saveLocalSessionId(context: Context, sessionId: String) {
        savedLocalSessionId = sessionId
        localSessionId = sessionId
    }

    override fun clearLocalSessionId(context: Context) {
        localSessionId = ""
        savedLocalSessionId = null
    }
}
