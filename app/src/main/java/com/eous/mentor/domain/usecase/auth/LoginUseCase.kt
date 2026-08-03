package com.eous.mentor.domain.usecase.auth

import com.eous.mentor.domain.repository.AuthRepository

class LoginUseCase(private val repository: AuthRepository) {
    suspend operator fun invoke(email: String, password: String): Result<Unit> =
        repository.login(email, password)
}
