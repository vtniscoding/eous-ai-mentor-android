package com.eous.mentor.testutil

import com.eous.mentor.domain.repository.AuthRepository

/**
 * Fake implementation of [AuthRepository] for unit testing.
 * Avoids MockK's known issue with Kotlin Result value class double-wrapping.
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

    var resetPasswordResult: Result<Unit> = Result.success(Unit)
    var verifyResetOtpResult: Result<Unit> = Result.success(Unit)
    var updatePasswordResult: Result<Unit> = Result.success(Unit)

    var resetPasswordCallCount = 0; private set
    var lastResetPasswordEmail: String? = null; private set

    var verifyResetOtpCallCount = 0; private set
    var lastVerifyResetOtpEmail: String? = null; private set
    var lastVerifyResetOtpToken: String? = null; private set

    var updatePasswordCallCount = 0; private set
    var lastUpdatePasswordValue: String? = null; private set

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

    override suspend fun resetPassword(email: String): Result<Unit> {
        resetPasswordCallCount++
        lastResetPasswordEmail = email
        return resetPasswordResult
    }

    override suspend fun verifyResetOtp(email: String, token: String): Result<Unit> {
        verifyResetOtpCallCount++
        lastVerifyResetOtpEmail = email
        lastVerifyResetOtpToken = token
        return verifyResetOtpResult
    }

    override suspend fun updatePassword(password: String): Result<Unit> {
        updatePasswordCallCount++
        lastUpdatePasswordValue = password
        return updatePasswordResult
    }
}
