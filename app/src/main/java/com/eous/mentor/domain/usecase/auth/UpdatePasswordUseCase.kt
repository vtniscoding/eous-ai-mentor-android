package com.eous.mentor.domain.usecase.auth

import com.eous.mentor.domain.repository.AuthRepository

class UpdatePasswordUseCase(private val repository: AuthRepository) {
    suspend operator fun invoke(password: String): Result<Unit> =
        repository.updatePassword(password)
}
