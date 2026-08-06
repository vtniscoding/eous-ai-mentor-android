package com.eous.mentor.testutil

import com.eous.mentor.domain.repository.AuthRepository

/**
 * Fake implementation of [AuthRepository] for testing.
 */
class FakeAuthRepository : AuthRepository {

    var loginResult: Result<Unit> = Result.success(Unit)
    var registerResult: Result<Unit> = Result.success(Unit)
    var logoutResult: Result<Unit> = Result.success(Unit)

    var loginCallCount = 0; private set
    var lastLoginEmail: String? = null; private set
    var lastLoginPassword: String? = null; private set

    var registerCallCount = 0; private set
    var lastRegisterEmail: String? = null; private set
    var lastRegisterPassword: String? = null; private set

    var logoutCallCount = 0; private set

    override suspend fun login(email: String, password: String): Result<Unit> {
        loginCallCount++
        lastLoginEmail = email
        lastLoginPassword = password
        return loginResult
    }

    override suspend fun register(email: String, password: String): Result<Unit> {
        registerCallCount++
        lastRegisterEmail = email
        lastRegisterPassword = password
        return registerResult
    }

    override suspend fun logout(): Result<Unit> {
        logoutCallCount++
        return logoutResult
    }
}
