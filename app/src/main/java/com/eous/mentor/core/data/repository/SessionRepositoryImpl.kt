package com.eous.mentor.core.data.repository

import com.eous.mentor.di.supabase
import com.eous.mentor.domain.repository.SessionRepository
import com.eous.mentor.domain.repository.SessionState
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.status.SessionStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class SessionRepositoryImpl : SessionRepository {
    override fun observeSessionStatus(): Flow<SessionState> {
        return supabase.auth.sessionStatus.map { status ->
            when (status) {
                is SessionStatus.Initializing -> SessionState.INITIALIZING
                is SessionStatus.Authenticated -> SessionState.AUTHENTICATED
                else -> SessionState.NOT_AUTHENTICATED
            }
        }
    }

    override fun getCurrentUserId(): String? {
        return try {
            supabase.auth.currentSessionOrNull()?.user?.id
        } catch (e: Throwable) {
            null
        }
    }

    override fun getCurrentUserEmail(): String? {
        return try {
            supabase.auth.currentSessionOrNull()?.user?.email
        } catch (e: Throwable) {
            null
        }
    }

    override fun getLocalSessionId(context: android.content.Context): String {
        val prefs = context.getSharedPreferences("eous_session_prefs", android.content.Context.MODE_PRIVATE)
        return prefs.getString("local_session_id", "") ?: ""
    }

    override fun saveLocalSessionId(context: android.content.Context, sessionId: String) {
        val prefs = context.getSharedPreferences("eous_session_prefs", android.content.Context.MODE_PRIVATE)
        prefs.edit().putString("local_session_id", sessionId).apply()
    }

    override fun clearLocalSessionId(context: android.content.Context) {
        val prefs = context.getSharedPreferences("eous_session_prefs", android.content.Context.MODE_PRIVATE)
        prefs.edit().remove("local_session_id").apply()
    }
}
