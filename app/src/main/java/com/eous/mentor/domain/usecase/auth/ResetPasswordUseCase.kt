package com.eous.mentor.domain.usecase.auth

import com.eous.mentor.domain.repository.AuthRepository

class ResetPasswordUseCase(private val repository: AuthRepository) {
    suspend operator fun invoke(email: String): Result<Unit> =
        repository.resetPassword(email)
}
