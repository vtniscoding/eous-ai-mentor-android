package com.eous.mentor.domain.repository

interface AuthRepository {
    suspend fun login(email: String, password: String): Result<Unit>
    suspend fun register(email: String, password: String): Result<Unit>
    suspend fun logout(): Result<Unit>
    suspend fun resetPassword(email: String): Result<Unit>
    suspend fun verifyResetOtp(email: String, token: String): Result<Unit>
    suspend fun updatePassword(password: String): Result<Unit>
}
