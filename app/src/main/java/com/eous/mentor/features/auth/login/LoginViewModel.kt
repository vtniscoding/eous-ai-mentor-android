package com.eous.mentor.features.auth.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eous.mentor.features.auth.friendlyAuthError
import com.eous.mentor.di.UseCaseProvider
import com.eous.mentor.domain.usecase.auth.LoginUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class LoginViewModel(
    private val loginUseCase: LoginUseCase = UseCaseProvider.login
) : ViewModel() {
    private val _state = MutableStateFlow(LoginState())
    val state: StateFlow<LoginState> = _state.asStateFlow()

    fun onEmailChanged(email: String) {
        _state.update { it.copy(email = email, error = null) }
    }

    fun onPasswordChanged(password: String) {
        _state.update { it.copy(password = password, error = null) }
    }

    fun onTogglePasswordVisibility() {
        _state.update { it.copy(isPasswordVisible = !_state.value.isPasswordVisible) }
    }

    fun onLogin(onSuccess: () -> Unit) {
        val currentState = _state.value
        if (currentState.email.isEmpty() || currentState.password.isEmpty()) {
            _state.update { it.copy(error = "Email and password are required.") }
            return
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(currentState.email).matches()) {
            _state.update { it.copy(error = "Please enter a valid email address.") }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            loginUseCase(currentState.email, currentState.password)
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
