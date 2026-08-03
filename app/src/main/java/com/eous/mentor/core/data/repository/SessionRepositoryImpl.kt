package com.eous.mentor.data.repository

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
}
