package com.eous.mentor.features.auth

fun friendlyAuthError(e: Throwable): String {
    val msg = (e.localizedMessage ?: e.message ?: "").lowercase()
    return when {
        "mfa_verification_failed" in msg || "invalid totp code" in msg ->
            "Incorrect verification code. Please try again."
        "mfa_challenge_expired" in msg ->
            "The verification code expired. Please enter a new code."
        "invalid_credentials" in msg || "invalid login" in msg ->
            "Incorrect email or password. Please try again."
        "email not confirmed" in msg ->
            "Please verify your email before logging in."
        "user already registered" in msg || "already been registered" in msg ->
            "An account with this email already exists."
        "rate limit" in msg || "too many requests" in msg ->
            "Too many attempts. Please wait a moment and try again."
        "network" in msg || "unable to resolve" in msg || "timeout" in msg ->
            "Network error. Please check your connection."
        "weak password" in msg ->
            "Password is too weak. Use at least 8 characters with mixed case and digits."
        "invalid email" in msg ->
            "Please enter a valid email address."
        else ->
            "Something went wrong. Please try again later."
    }
}
