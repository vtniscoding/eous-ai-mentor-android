package com.eous.mentor.di

import com.eous.mentor.core.data.repository.AuthRepositoryImpl
import com.eous.mentor.core.data.repository.ChatRepositoryImpl
import com.eous.mentor.core.data.repository.SessionRepositoryImpl
import com.eous.mentor.core.data.repository.UserRepositoryImpl
import com.eous.mentor.domain.repository.AuthRepository
import com.eous.mentor.domain.repository.ChatRepository
import com.eous.mentor.domain.repository.SessionRepository
import com.eous.mentor.domain.repository.UserRepository

/**
 * Simple service locator providing singleton repository instances.
 * Keeps ViewModels decoupled from concrete implementations.
 */
object RepositoryProvider {
    val authRepository: AuthRepository by lazy { AuthRepositoryImpl() }
    val chatRepository: ChatRepository by lazy { ChatRepositoryImpl() }
    val userRepository: UserRepository by lazy { UserRepositoryImpl() }
    val sessionRepository: SessionRepository by lazy { SessionRepositoryImpl() }
}

