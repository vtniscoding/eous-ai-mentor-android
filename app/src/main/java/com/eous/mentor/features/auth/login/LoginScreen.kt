package com.eous.mentor.features.auth.login

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.draw.paint
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.eous.mentor.core.data.repository.SavedAccountsRepository
import com.eous.mentor.core.navigation.navigateSafe
import com.eous.mentor.R
import com.eous.mentor.core.ui.theme.*
import kotlinx.coroutines.launch
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.compose.auth.composeAuth
import io.github.jan.supabase.compose.auth.composable.rememberSignInWithGoogle
import io.github.jan.supabase.compose.auth.composable.NativeSignInResult
import androidx.compose.foundation.BorderStroke
import com.eous.mentor.di.supabase

@Composable
fun LoginFormScreen(
    navController: NavController,
    isTablet: Boolean,
    viewModel: LoginViewModel = remember { LoginViewModel() }
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val googleAuthAction = supabase.composeAuth.rememberSignInWithGoogle(
        onResult = { result ->
            when (result) {
                is NativeSignInResult.Success -> {
                    Toast.makeText(context, "Logged in with Google successfully!", Toast.LENGTH_SHORT).show()
                    navController.navigateSafe("dashboard") {
                        popUpTo("intro") { inclusive = true }
                    }
                }
                is NativeSignInResult.Error -> {
                    Toast.makeText(context, "Google Sign-In failed: ${result.message}", Toast.LENGTH_SHORT).show()
                }
                is NativeSignInResult.ClosedByUser -> {}
                is NativeSignInResult.NetworkError -> {
                    Toast.makeText(context, "Network error. Please check your connection.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF965BE9))
            .paint(
                painter = painterResource(id = R.drawable.login_background),
                contentScale = ContentScale.FillBounds
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            // Space reserved for Eous logo on background
            if (!isTablet) {
                Spacer(modifier = Modifier.height(200.dp))
            } else {
                Spacer(modifier = Modifier.height(40.dp))
            }

            Text(
                text = "Ready to focus?",
                color = Color.Black,
                fontSize = 32.sp,
                fontFamily = Inter,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(28.dp))

            // Parse error messages for specific fields
            val emailError = if (state.error != null && state.email.isEmpty()) "Username/Email can't be blank" else if (state.error != null && state.error!!.contains("email address", ignoreCase = true)) "Please enter a valid email address." else null
            val passwordError = if (state.error != null && state.password.isEmpty()) "Password can't be blank" else null
            val generalError = if (state.error != null && emailError == null && passwordError == null) state.error else null

            // --- Card white containing Form ---
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(32.dp))
                    .background(Color.White)
                    .padding(horizontal = 24.dp, vertical = 24.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Email/Username Input
                    Column(modifier = Modifier.fillMaxWidth()) {
                        val isEmailError = emailError != null || generalError != null
                        OutlinedTextField(
                            value = state.email,
                            onValueChange = { viewModel.onEmailChanged(it) },
                            placeholder = { Text("Username/Email", color = Color.Gray.copy(alpha = 0.5f)) },
                            singleLine = true,
                            isError = isEmailError,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp), // Height set to 56.dp
                            shape = RoundedCornerShape(28.dp), // Completely rounded (Capsule shape)
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.Black,
                                unfocusedTextColor = Color.Black,
                                focusedContainerColor = Color(0xFFF1F1F1),
                                unfocusedContainerColor = Color(0xFFF1F1F1),
                                focusedBorderColor = Color(0xFF7F43D4),
                                unfocusedBorderColor = Color.Transparent,
                                errorContainerColor = Color(0xFFF1F1F1),
                                errorBorderColor = Color.Red,
                                cursorColor = Color.Black
                            )
                        )
                        if (emailError != null) {
                            Text(
                                text = emailError,
                                color = Color.Red,
                                fontSize = 12.sp,
                                fontFamily = Inter,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(start = 16.dp, top = 6.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Password Input
                    Column(modifier = Modifier.fillMaxWidth()) {
                        val isPasswordError = passwordError != null || generalError != null
                        OutlinedTextField(
                            value = state.password,
                            onValueChange = { viewModel.onPasswordChanged(it) },
                            placeholder = { Text("Password", color = Color.Gray.copy(alpha = 0.5f)) },
                            singleLine = true,
                            isError = isPasswordError,
                            visualTransformation = if (state.isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            trailingIcon = {
                                IconButton(onClick = { viewModel.onTogglePasswordVisibility() }) {
                                    Icon(
                                        imageVector = if (state.isPasswordVisible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                                        contentDescription = "Toggle password visibility",
                                        tint = Color.Gray
                                    )
                                }
                            },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp), // Height set to 56.dp
                            shape = RoundedCornerShape(28.dp), // Completely rounded (Capsule shape)
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.Black,
                                unfocusedTextColor = Color.Black,
                                focusedContainerColor = Color(0xFFF1F1F1),
                                unfocusedContainerColor = Color(0xFFF1F1F1),
                                focusedBorderColor = Color(0xFF7F43D4),
                                unfocusedBorderColor = Color.Transparent,
                                errorContainerColor = Color(0xFFF1F1F1),
                                errorBorderColor = Color.Red,
                                cursorColor = Color.Black
                            )
                        )
                        if (passwordError != null) {
                            Text(
                                text = passwordError,
                                color = Color.Red,
                                fontSize = 12.sp,
                                fontFamily = Inter,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(start = 16.dp, top = 6.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Forgot password link
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Start
                    ) {
                        Text(
                            text = "Forgot password?",
                            color = Color.Black.copy(alpha = 0.8f),
                            fontSize = 14.sp,
                            fontFamily = Inter,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier
                                .clickable {
                                    navController.navigateSafe("forgot_password")
                                }
                                .padding(start = 16.dp)
                        )
                    }

                    // General login error displayed in the whitespace above Log In button
                    if (generalError != null) {
                        Text(
                            text = generalError,
                            color = Color.Red,
                            fontSize = 13.sp,
                            fontFamily = Inter,
                            fontWeight = FontWeight.Medium,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, top = 8.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // Log In Button (purple background, matches screenshot)
                    Button(
                        onClick = {
                            viewModel.onLogin {
                                SavedAccountsRepository.saveAccount(
                                    context,
                                    com.eous.mentor.domain.model.SavedAccount(
                                        email = state.email,
                                        password = state.password
                                    )
                                )
                                scope.launch {
                                    val currentUid = viewModel.currentUserId()
                                    if (!currentUid.isNullOrEmpty()) {
                                        viewModel.issueSession(context, currentUid)

                                        val avatarUrl = viewModel.fetchAvatarUrl(currentUid)
                                        if (!avatarUrl.isNullOrEmpty()) {
                                            SavedAccountsRepository.saveAccount(
                                                context,
                                                com.eous.mentor.domain.model.SavedAccount(
                                                    email = state.email,
                                                    password = state.password,
                                                    avatarUrl = avatarUrl
                                                )
                                            )
                                        }
                                    }
                                    try {
                                        val (current, next) = com.eous.mentor.di.supabase.auth.mfa.getAuthenticatorAssuranceLevel()
                                        if (current == io.github.jan.supabase.auth.mfa.AuthenticatorAssuranceLevel.AAL1 &&
                                            next == io.github.jan.supabase.auth.mfa.AuthenticatorAssuranceLevel.AAL2) {
                                            Toast.makeText(context, "MFA Verification Required", Toast.LENGTH_SHORT).show()
                                            navController.navigateSafe("mfa_verify") {
                                                popUpTo("intro") { inclusive = true }
                                            }
                                        } else {
                                            Toast.makeText(context, "Logged in successfully!", Toast.LENGTH_SHORT).show()
                                            navController.navigateSafe("dashboard") {
                                                popUpTo("intro") { inclusive = true }
                                            }
                                        }
                                    } catch (e: Throwable) {
                                        Toast.makeText(context, "Logged in successfully!", Toast.LENGTH_SHORT).show()
                                        navController.navigateSafe("dashboard") {
                                            popUpTo("intro") { inclusive = true }
                                        }
                                    }
                                }
                            }
                        },
                        shape = RoundedCornerShape(28.dp), // Capsule button
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7F43D4)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                    ) {
                        if (state.isLoading) {
                            CircularProgressIndicator(
                                color = Color.White,
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                "Log In",
                                color = Color.White,
                                fontSize = 17.sp,
                                fontFamily = Inter,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Or separator
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
                    ) {
                        HorizontalDivider(modifier = Modifier.weight(1f), color = Color.LightGray.copy(alpha = 0.5f))
                        Text(
                            text = "or",
                            color = Color.Gray,
                            fontSize = 14.sp,
                            fontFamily = Inter,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                        HorizontalDivider(modifier = Modifier.weight(1f), color = Color.LightGray.copy(alpha = 0.5f))
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Sign In with Google Button
                    Button(
                        onClick = { googleAuthAction.startFlow() },
                        shape = RoundedCornerShape(28.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                        border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.6f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.ic_google_logo),
                                contentDescription = "Google Logo",
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Continue with Google",
                                color = Color.Black,
                                fontSize = 17.sp,
                                fontFamily = Inter,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp)) // Large spacer before footer link

                    // Switch to Sign Up
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Don't have an account? ", color = Color.Black.copy(alpha = 0.8f), fontSize = 14.sp, fontFamily = Inter, fontWeight = FontWeight.Normal)
                        Text(
                            "Sign up",
                            color = Color.Black,
                            fontSize = 14.sp,
                            fontFamily = Inter,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.clickable {
                                navController.navigateSafe("register") {
                                    popUpTo("login") { inclusive = true }
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}
