package com.eous.mentor.features.auth.forgot

data class ForgotPasswordState(
    val step: Int = 1, // 1: Enter Email, 2: Enter OTP, 3: Enter New Password
    val email: String = "",
    val otp: String = "",
    val newPassword: String = "",
    val confirmPassword: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null,
    val isPasswordVisible: Boolean = false,
    val isConfirmPasswordVisible: Boolean = false
)
