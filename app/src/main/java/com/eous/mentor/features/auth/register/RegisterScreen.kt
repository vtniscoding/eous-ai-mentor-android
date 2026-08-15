package com.eous.mentor.features.auth.register

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
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
import com.eous.mentor.core.navigation.navigateSafe
import com.eous.mentor.R
import com.eous.mentor.core.ui.theme.*
import kotlinx.coroutines.launch
import io.github.jan.supabase.compose.auth.composeAuth
import io.github.jan.supabase.compose.auth.composable.rememberSignInWithGoogle
import io.github.jan.supabase.compose.auth.composable.NativeSignInResult
import com.eous.mentor.di.supabase

@Composable
fun RegisterFormScreen(
    navController: NavController,
    isTablet: Boolean,
    viewModel: RegisterViewModel = remember { RegisterViewModel() }
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val googleAuthAction = supabase.composeAuth.rememberSignInWithGoogle(
        onResult = { result ->
            when (result) {
                is NativeSignInResult.Success -> {
                    Toast.makeText(context, "Registered/Logged in with Google successfully!", Toast.LENGTH_SHORT).show()
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
            .background(Color.White)
            .paint(
                painter = painterResource(id = R.drawable.signup_background),
                contentScale = ContentScale.FillBounds
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            if (!isTablet) {
                Spacer(modifier = Modifier.height(45.dp))
            } else {
                Spacer(modifier = Modifier.height(20.dp))
            }

            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = "Nice to meet you!",
                    color = Color.Black,
                    fontSize = 32.sp,
                    fontFamily = Inter,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Start,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(32.dp))

                // Parse error messages for specific fields
                val emailError = if (state.error != null && state.email.isEmpty()) "Email can't be blank" else if (state.error != null && state.error!!.contains("valid email", ignoreCase = true)) "Please enter a valid email address." else null
                val confirmPasswordError = if (state.error != null && state.confirmPassword.isEmpty()) "Confirm password can't be blank" else if (state.error != null && state.error!!.contains("match", ignoreCase = true)) "Passwords do not match." else null
                val passwordError = if (state.error != null && state.password.isEmpty()) "Password can't be blank" else if (state.error != null && state.error!!.contains("password", ignoreCase = true) && confirmPasswordError == null) state.error else null
                val generalError = if (state.error != null && emailError == null && passwordError == null && confirmPasswordError == null) state.error else null

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Email Input
                    Column(modifier = Modifier.fillMaxWidth()) {
                        val isEmailError = emailError != null || generalError != null
                        OutlinedTextField(
                            value = state.email,
                            onValueChange = { viewModel.onEmailChanged(it) },
                            placeholder = { Text("Email address", color = Color.Gray.copy(alpha = 0.5f)) },
                            singleLine = true,
                            isError = isEmailError,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(54.dp),
                            shape = RoundedCornerShape(24.dp),
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
                                modifier = Modifier.padding(start = 12.dp, top = 4.dp)
                            )
                        }
                    }

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
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Next),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(54.dp),
                            shape = RoundedCornerShape(24.dp),
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
                        
                        // Password Strength Meter
                        if (state.password.isNotEmpty()) {
                            PasswordStrengthMeter(password = state.password)
                        }
                        
                        if (passwordError != null) {
                            Text(
                                text = passwordError,
                                color = Color.Red,
                                fontSize = 12.sp,
                                fontFamily = Inter,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(start = 12.dp, top = 4.dp)
                            )
                        }
                    }

                    // Confirm Password Input
                    Column(modifier = Modifier.fillMaxWidth()) {
                        val isConfirmError = confirmPasswordError != null || generalError != null
                        OutlinedTextField(
                            value = state.confirmPassword,
                            onValueChange = { viewModel.onConfirmPasswordChanged(it) },
                            placeholder = { Text("Confirm password", color = Color.Gray.copy(alpha = 0.5f)) },
                            singleLine = true,
                            isError = isConfirmError,
                            visualTransformation = if (state.isConfirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            trailingIcon = {
                                IconButton(onClick = { viewModel.onToggleConfirmPasswordVisibility() }) {
                                    Icon(
                                        imageVector = if (state.isConfirmPasswordVisible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                                        contentDescription = "Toggle password visibility",
                                        tint = Color.Gray
                                    )
                                }
                            },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(54.dp),
                            shape = RoundedCornerShape(24.dp),
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
                        if (confirmPasswordError != null) {
                            Text(
                                text = confirmPasswordError,
                                color = Color.Red,
                                fontSize = 12.sp,
                                fontFamily = Inter,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(start = 12.dp, top = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Group button and switch link closer together
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // General signup error displayed in the whitespace above Sign Up button
                        if (generalError != null) {
                            Text(
                                text = generalError,
                                color = Color.Red,
                                fontSize = 13.sp,
                                fontFamily = Inter,
                                fontWeight = FontWeight.Medium,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, bottom = 8.dp)
                            )
                        }

                        // Sign Up Button
                        Button(
                            onClick = {
                                viewModel.onRegister {
                                    com.eous.mentor.core.data.repository.SavedAccountsRepository.saveAccount(
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
                                        }
                                    }
                                    Toast.makeText(context, "Registration successful!", Toast.LENGTH_SHORT).show()
                                    navController.navigateSafe("dashboard") {
                                        popUpTo("intro") { inclusive = true }
                                    }
                                }
                            },
                            shape = RoundedCornerShape(24.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF252425)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(54.dp)
                        ) {
                            if (state.isLoading) {
                                CircularProgressIndicator(
                                    color = Color.White,
                                    modifier = Modifier.size(20.dp),
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Text(
                                    "Sign Up",
                                    color = Color.White,
                                    fontSize = 17.sp,
                                    fontFamily = Inter,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

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

                        Spacer(modifier = Modifier.height(10.dp))

                        // Sign In with Google Button
                        Button(
                            onClick = { googleAuthAction.startFlow() },
                            shape = RoundedCornerShape(24.dp), // Match the Sign Up button shape
                            colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                            border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.6f)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(54.dp)
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

                        Spacer(modifier = Modifier.height(12.dp))

                        // Switch to Log In
                        Row(
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Already have an account? ", color = Color.Black.copy(alpha = 0.8f), fontSize = 14.sp, fontFamily = Inter, fontWeight = FontWeight.Normal)
                            Text(
                                "Log in",
                                color = Color.Black,
                                fontSize = 14.sp,
                                fontFamily = Inter,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.clickable {
                                    navController.navigateSafe("login") {
                                        popUpTo("register") { inclusive = true }
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
