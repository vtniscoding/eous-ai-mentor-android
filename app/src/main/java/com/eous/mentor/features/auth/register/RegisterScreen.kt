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

@Composable
fun RegisterFormScreen(
    navController: NavController,
    isTablet: Boolean,
    viewModel: RegisterViewModel = remember { RegisterViewModel() }
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current

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
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            if (!isTablet) {
                Spacer(modifier = Modifier.height(80.dp))
            } else {
                Spacer(modifier = Modifier.height(40.dp))
            }

            Column(
                modifier = Modifier.fillMaxWidth(0.85f),
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

                Spacer(modifier = Modifier.height(24.dp))

                // Error message banner
                if (state.error != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
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
                    Spacer(modifier = Modifier.height(12.dp))
                }

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Email
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

                    // Password
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
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Next),
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
                        
                        // Password Strength Meter
                        if (state.password.isNotEmpty()) {
                            PasswordStrengthMeter(password = state.password)
                        }
                    }

                    // Confirm Password
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "Confirm Password",
                            color = Color.Black,
                            fontSize = 15.sp,
                            fontFamily = Inter,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(start = 12.dp, bottom = 6.dp)
                        )
                        OutlinedTextField(
                            value = state.confirmPassword,
                            onValueChange = { viewModel.onConfirmPasswordChanged(it) },
                            singleLine = true,
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

                    // Group button and switch link closer together
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
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
                                    Toast.makeText(context, "Registration successful!", Toast.LENGTH_SHORT).show()
                                    navController.navigateSafe("dashboard") {
                                        popUpTo("intro") { inclusive = true }
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
                                    "Sign Up",
                                    color = Color.White,
                                    fontSize = 17.sp,
                                    fontFamily = Inter,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

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
                                        popUpTo("intro")
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
