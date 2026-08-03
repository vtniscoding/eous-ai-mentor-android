package com.eous.mentor.domain.usecase.auth

import com.eous.mentor.domain.repository.AuthRepository

class LogoutUseCase(private val repository: AuthRepository) {
    suspend operator fun invoke(): Result<Unit> =
        repository.logout()
}
