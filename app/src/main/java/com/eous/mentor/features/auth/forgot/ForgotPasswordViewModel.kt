package com.eous.mentor.features.auth.forgot

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eous.mentor.di.UseCaseProvider
import com.eous.mentor.domain.usecase.auth.ResetPasswordUseCase
import com.eous.mentor.domain.usecase.auth.VerifyResetOtpUseCase
import com.eous.mentor.domain.usecase.auth.UpdatePasswordUseCase
import com.eous.mentor.features.auth.friendlyAuthError
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private val EMAIL_ADDRESS_PATTERN = java.util.regex.Pattern.compile(
    "[a-zA-Z0-9\\+\\.\\_\\%\\-\\+]{1,256}" +
    "\\@" +
    "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,64}" +
    "(" +
    "\\." +
    "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,25}" +
    ")+"
)

class ForgotPasswordViewModel(
    private val resetPasswordUseCase: ResetPasswordUseCase = UseCaseProvider.resetPassword,
    private val verifyResetOtpUseCase: VerifyResetOtpUseCase = UseCaseProvider.verifyResetOtp,
    private val updatePasswordUseCase: UpdatePasswordUseCase = UseCaseProvider.updatePassword
) : ViewModel() {

    private val _state = MutableStateFlow(ForgotPasswordState())
    val state: StateFlow<ForgotPasswordState> = _state.asStateFlow()

    fun onEmailChanged(email: String) {
        _state.update { it.copy(email = email, error = null, successMessage = null) }
    }

    fun onOtpChanged(otp: String) {
        _state.update { it.copy(otp = otp, error = null, successMessage = null) }
    }

    fun onNewPasswordChanged(password: String) {
        _state.update { it.copy(newPassword = password, error = null) }
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

    fun onSendOtp() {
        val email = _state.value.email.trim()
        if (email.isEmpty()) {
            _state.update { it.copy(error = "Email address is required.") }
            return
        }
        if (!EMAIL_ADDRESS_PATTERN.matcher(email).matches()) {
            _state.update { it.copy(error = "Please enter a valid email address.") }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null, successMessage = null) }
            resetPasswordUseCase(email)
                .onSuccess {
                    _state.update { 
                        it.copy(
                            step = 2, 
                            successMessage = "OTP recovery code sent to your email."
                        ) 
                    }
                }
                .onFailure { e ->
                    _state.update { it.copy(error = friendlyAuthError(e)) }
                }
            _state.update { it.copy(isLoading = false) }
        }
    }

    fun onVerifyOtp() {
        val email = _state.value.email.trim()
        val otp = _state.value.otp.trim()
        if (otp.isEmpty()) {
            _state.update { it.copy(error = "OTP code is required.") }
            return
        }
        if (otp.length < 6) {
            _state.update { it.copy(error = "OTP code must be at least 6 digits.") }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null, successMessage = null) }
            verifyResetOtpUseCase(email, otp)
                .onSuccess {
                    _state.update { it.copy(step = 3) }
                }
                .onFailure { e ->
                    _state.update { it.copy(error = friendlyAuthError(e)) }
                }
            _state.update { it.copy(isLoading = false) }
        }
    }

    fun onUpdatePassword(onSuccess: () -> Unit) {
        val newPassword = _state.value.newPassword
        val confirmPassword = _state.value.confirmPassword

        if (newPassword.isEmpty() || confirmPassword.isEmpty()) {
            _state.update { it.copy(error = "All password fields are required.") }
            return
        }

        if (newPassword.length < 8) {
            _state.update { it.copy(error = "Password must be at least 8 characters long.") }
            return
        }

        // Strength verification: standard checks
        val hasLowercase = newPassword.any { it.isLowerCase() }
        val hasUppercase = newPassword.any { it.isUpperCase() }
        val hasDigit = newPassword.any { it.isDigit() }
        if (!hasLowercase || !hasUppercase || !hasDigit) {
            _state.update { it.copy(error = "Password must contain lowercase, uppercase, and digit symbols.") }
            return
        }

        if (newPassword != confirmPassword) {
            _state.update { it.copy(error = "Passwords do not match.") }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            updatePasswordUseCase(newPassword)
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
