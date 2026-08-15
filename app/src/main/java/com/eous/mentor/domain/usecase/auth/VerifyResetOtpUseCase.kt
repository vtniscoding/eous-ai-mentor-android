package com.eous.mentor.domain.usecase.auth

import com.eous.mentor.domain.repository.AuthRepository

class VerifyResetOtpUseCase(private val repository: AuthRepository) {
    suspend operator fun invoke(email: String, token: String): Result<Unit> =
        repository.verifyResetOtp(email, token)
}
