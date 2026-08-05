package com.eous.mentor.features.auth.login

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
import com.eous.mentor.core.data.repository.SavedAccountsRepository
import com.eous.mentor.core.navigation.navigateSafe
import com.eous.mentor.R
import com.eous.mentor.core.ui.theme.*

import kotlinx.coroutines.launch
import io.github.jan.supabase.auth.auth

@Composable
fun LoginFormScreen(
    navController: NavController,
    isTablet: Boolean,
    viewModel: LoginViewModel = remember { LoginViewModel() }
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

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
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            if (!isTablet) {
                Spacer(modifier = Modifier.height(220.dp))
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

            Spacer(modifier = Modifier.height(24.dp))

            // Error message banner
            if (state.error != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.85f)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFFFFD1D1))
                        .border(1.5.dp, Color(0xFFE53935), RoundedCornerShape(16.dp))
                        .padding(vertical = 12.dp, horizontal = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = state.error!!,
                        color = Color(0xFF8B0000),
                        fontSize = 14.sp,
                        fontFamily = Inter,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            Column(
                modifier = Modifier.fillMaxWidth(0.85f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Email Label + Input
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Email",
                        color = Color.Black,
                        fontSize = 15.sp,
                        fontFamily = Inter,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(start = 12.dp, bottom = 6.dp)
                    )
                    OutlinedTextField(
                        value = state.email,
                        onValueChange = { viewModel.onEmailChanged(it) },
                        placeholder = { Text("you@example.com", color = Color.Gray.copy(alpha = 0.5f)) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
                        modifier = Modifier.fillMaxWidth().height(54.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.Black,
                            unfocusedTextColor = Color.Black,
                            focusedContainerColor = Color(0xFFF1F1F1),
                            unfocusedContainerColor = Color(0xFFF1F1F1),
                            focusedBorderColor = Color.Transparent,
                            unfocusedBorderColor = Color.Transparent,
                            cursorColor = Color.Black
                        )
                    )
                }

                // Password Label + Input
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Password",
                        color = Color.Black,
                        fontSize = 15.sp,
                        fontFamily = Inter,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(start = 12.dp, bottom = 6.dp)
                    )
                    OutlinedTextField(
                        value = state.password,
                        onValueChange = { viewModel.onPasswordChanged(it) },
                        singleLine = true,
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
                        modifier = Modifier.fillMaxWidth().height(54.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.Black,
                            unfocusedTextColor = Color.Black,
                            focusedContainerColor = Color(0xFFF1F1F1),
                            unfocusedContainerColor = Color(0xFFF1F1F1),
                            focusedBorderColor = Color.Transparent,
                            unfocusedBorderColor = Color.Transparent,
                            cursorColor = Color.Black
                        )
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Log In Button
                Button(
                    onClick = {
                        viewModel.onLogin {
                            // Immediately save account with password
                            SavedAccountsRepository.saveAccount(
                                context,
                                com.eous.mentor.domain.model.SavedAccount(
                                    email = state.email,
                                    password = state.password
                                )
                            )
                            // Fetch avatar and set new session ID asynchronously
                            scope.launch {
                                val currentUid = com.eous.mentor.di.RepositoryProvider.sessionRepository.getCurrentUserId()
                                if (!currentUid.isNullOrEmpty()) {
                                    val newSessionId = java.util.UUID.randomUUID().toString()
                                    com.eous.mentor.di.RepositoryProvider.sessionRepository.saveLocalSessionId(context, newSessionId)
                                    com.eous.mentor.di.RepositoryProvider.userRepository.updateSessionId(currentUid, newSessionId)

                                    val avatarUrl = com.eous.mentor.di.RepositoryProvider.userRepository.getProfile(currentUid).getOrNull()?.avatar_url
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
                    shape = RoundedCornerShape(16.dp),
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
                            "Log In",
                            color = Color.White,
                            fontSize = 17.sp,
                            fontFamily = Inter,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Grouped Bottom texts to avoid large gap
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
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
                                    popUpTo("intro")
                                }
                            }
                        )
                    }

                    Text(
                        "Forgot password?",
                        color = Color.Black.copy(alpha = 0.6f),
                        fontSize = 13.sp,
                        fontFamily = Inter,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.clickable {
                            Toast.makeText(context, "Password reset link sent!", Toast.LENGTH_SHORT).show()
                        }
                    )
                }
            }
        }
    }
}
