package com.eous.mentor.features.auth.register

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eous.mentor.features.auth.friendlyAuthError
import com.eous.mentor.di.UseCaseProvider
import com.eous.mentor.domain.usecase.auth.RegisterUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

import com.eous.mentor.di.RepositoryProvider
import com.eous.mentor.domain.repository.SessionRepository
import com.eous.mentor.domain.usecase.session.IssueLocalSessionUseCase

class RegisterViewModel(
    private val registerUseCase: RegisterUseCase = UseCaseProvider.register,
    private val issueLocalSessionUseCase: IssueLocalSessionUseCase = UseCaseProvider.issueLocalSession,
    private val sessionRepository: SessionRepository = RepositoryProvider.sessionRepository
) : ViewModel() {

    fun currentUserId(): String? = sessionRepository.getCurrentUserId()

    suspend fun issueSession(context: android.content.Context, userId: String) {
        issueLocalSessionUseCase(context, userId)
    }
    private val _state = MutableStateFlow(RegisterState())
    val state: StateFlow<RegisterState> = _state.asStateFlow()

    fun onEmailChanged(email: String) {
        _state.update { it.copy(email = email, error = null) }
    }

    fun onPasswordChanged(password: String) {
        _state.update { it.copy(password = password, error = null) }
    }

    fun onConfirmPasswordChanged(confirmPassword: String) {
        _state.update { it.copy(confirmPassword = confirmPassword, error = null) }
    }

    fun onTogglePasswordVisibility() {
        _state.update { it.copy(isPasswordVisible = !_state.value.isPasswordVisible) }
    }

    fun onToggleConfirmPasswordVisibility() {
        _state.update { it.copy(isConfirmPasswordVisible = !_state.value.isConfirmPasswordVisible) }
    }

    fun onRegister(onSuccess: () -> Unit) {
        val currentState = _state.value
        val strength = getPasswordStrength(currentState.password)

        if (currentState.email.isEmpty() || currentState.password.isEmpty() || currentState.confirmPassword.isEmpty()) {
            _state.update { it.copy(error = "All fields are required.") }
            return
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(currentState.email).matches()) {
            _state.update { it.copy(error = "Please enter a valid email address.") }
            return
        }
        if (currentState.password.length < 6) {
            _state.update { it.copy(error = "Password must be at least 6 characters long.") }
            return
        }
        val hasDigit = currentState.password.any { it.isDigit() }
        val hasUpper = currentState.password.any { it.isUpperCase() }
        val hasLower = currentState.password.any { it.isLowerCase() }

        if (!hasDigit || !hasUpper || !hasLower) {
            _state.update { it.copy(error = "Password must contain uppercase, lowercase letters and numbers.") }
            return
        }
        if (currentState.password != currentState.confirmPassword) {
            _state.update { it.copy(error = "Passwords do not match.") }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            registerUseCase(currentState.email, currentState.password)
                .onSuccess {
                    onSuccess()
                }
                .onFailure { e ->
                    _state.update { it.copy(error = friendlyAuthError(e)) }
                }
            _state.update { it.copy(isLoading = false) }
        }
    }
}
