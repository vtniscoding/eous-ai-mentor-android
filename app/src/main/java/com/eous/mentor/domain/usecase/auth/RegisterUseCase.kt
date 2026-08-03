package com.eous.mentor.domain.usecase.auth

import com.eous.mentor.domain.repository.AuthRepository

class RegisterUseCase(private val repository: AuthRepository) {
    suspend operator fun invoke(email: String, password: String): Result<Unit> =
        repository.register(email, password)
}
