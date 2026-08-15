package com.eous.mentor.features.auth.forgot

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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.eous.mentor.R
import com.eous.mentor.core.navigation.navigateSafe
import com.eous.mentor.core.ui.theme.Inter
import com.eous.mentor.features.auth.register.PasswordStrengthMeter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForgotPasswordScreen(
    navController: NavController,
    viewModel: ForgotPasswordViewModel = remember { ForgotPasswordViewModel() }
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        // --- Background Patterns (3 circles at top) ---
        Box(
            modifier = Modifier
                .size(220.dp)
                .offset(x = (-90).dp, y = (-70).dp)
                .background(Color(0xFFC4C4C4), CircleShape)
        )
        Box(
            modifier = Modifier
                .size(90.dp)
                .offset(x = 135.dp, y = 30.dp)
                .background(Color(0xFFE8EFFF), CircleShape)
        )
        Box(
            modifier = Modifier
                .size(170.dp)
                .offset(x = 290.dp, y = (-40).dp)
                .background(Color(0xFFD6C2FF), CircleShape)
        )

        // --- Bottom Left Confused Eous Decoration ---
        Image(
            painter = painterResource(id = R.drawable.ic_confused_eous),
            contentDescription = null,
            modifier = Modifier
                .size(260.dp) // Smaller size
                .align(Alignment.BottomStart)
                .offset(x = (-30).dp, y = 10.dp) // Pushed upwards
        )

        // --- Main Form Column ---
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            // Header with Back button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { 
                        navController.popBackStack()
                    },
                    modifier = Modifier
                        .size(40.dp)
                        .background(Color.Black.copy(alpha = 0.05f), CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.Black
                    )
                }
            }

            Spacer(modifier = Modifier.height(60.dp))

            Text(
                text = "Reset Password",
                color = Color.Black,
                fontSize = 32.sp,
                fontFamily = Inter,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Subtitle based on step
            val subtitle = when (state.step) {
                1 -> "Enter your email to receive a recovery code"
                2 -> "Enter the 6-digit recovery code sent to your email"
                else -> "Choose a strong new password for your account"
            }

            Text(
                text = subtitle,
                color = Color.Black.copy(alpha = 0.8f),
                fontSize = 16.sp,
                fontFamily = Inter,
                fontWeight = FontWeight.Normal,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(0.9f)
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Success banner (retained only for success messages)
            if (state.successMessage != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.85f)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFFD4EDDA))
                        .border(1.5.dp, Color(0xFF28A745), RoundedCornerShape(16.dp))
                        .padding(vertical = 12.dp, horizontal = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = state.successMessage!!,
                        color = Color(0xFF155724),
                        fontSize = 14.sp,
                        fontFamily = Inter,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Step Inputs
            Column(
                modifier = Modifier.fillMaxWidth(0.85f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                when (state.step) {
                    1 -> {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            // Email Input
                            Column(modifier = Modifier.fillMaxWidth()) {
                                val isError = state.step == 1 && state.error != null
                                OutlinedTextField(
                                    value = state.email,
                                    onValueChange = { viewModel.onEmailChanged(it) },
                                    placeholder = { Text("Email address", color = Color.Gray.copy(alpha = 0.5f)) },
                                    singleLine = true,
                                    isError = isError,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Done),
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
                                if (isError) {
                                    Text(
                                        text = state.error!!,
                                        color = Color.Red,
                                        fontSize = 12.sp,
                                        fontFamily = Inter,
                                        fontWeight = FontWeight.Medium,
                                        modifier = Modifier.padding(start = 12.dp, top = 4.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(20.dp))

                            // Send Button
                            Button(
                                onClick = { viewModel.onSendOtp() },
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
                                        "Send Recovery Code",
                                        color = Color.White,
                                        fontSize = 17.sp,
                                        fontFamily = Inter,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }

                    2 -> {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            // OTP Code Input
                            Column(modifier = Modifier.fillMaxWidth()) {
                                val isError = state.step == 2 && state.error != null
                                OutlinedTextField(
                                    value = state.otp,
                                    onValueChange = { viewModel.onOtpChanged(it) },
                                    placeholder = { Text("Enter 6-digit code", color = Color.Gray.copy(alpha = 0.5f)) },
                                    singleLine = true,
                                    isError = isError,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
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
                                if (isError) {
                                    Text(
                                        text = state.error!!,
                                        color = Color.Red,
                                        fontSize = 12.sp,
                                        fontFamily = Inter,
                                        fontWeight = FontWeight.Medium,
                                        modifier = Modifier.padding(start = 12.dp, top = 4.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(20.dp))

                            // Verify Button
                            Button(
                                onClick = { viewModel.onVerifyOtp() },
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
                                        "Verify Code",
                                        color = Color.White,
                                        fontSize = 17.sp,
                                        fontFamily = Inter,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp)) // Pushes the resend options closer/upwards

                            // Resend option
                            Row(
                                horizontalArrangement = Arrangement.Center,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Didn't receive the code? ", color = Color.Black.copy(alpha = 0.8f), fontSize = 14.sp, fontFamily = Inter, fontWeight = FontWeight.Normal)
                                Text(
                                    "Resend",
                                    color = Color.Black,
                                    fontSize = 14.sp,
                                    fontFamily = Inter,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.clickable {
                                        viewModel.onSendOtp()
                                    }
                                )
                            }
                        }
                    }

                    3 -> {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            // New Password
                            Column(modifier = Modifier.fillMaxWidth()) {
                                val isError = state.step == 3 && state.error != null
                                OutlinedTextField(
                                    value = state.newPassword,
                                    onValueChange = { viewModel.onNewPasswordChanged(it) },
                                    placeholder = { Text("Password", color = Color.Gray.copy(alpha = 0.5f)) },
                                    singleLine = true,
                                    isError = isError,
                                    visualTransformation = if (state.isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                    trailingIcon = {
                                        IconButton(onClick = { viewModel.onTogglePasswordVisibility() }) {
                                            Icon(
                                                imageVector = if (state.isPasswordVisible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                                                contentDescription = "Toggle visibility",
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
                                if (isError) {
                                    Text(
                                        text = state.error!!,
                                        color = Color.Red,
                                        fontSize = 12.sp,
                                        fontFamily = Inter,
                                        fontWeight = FontWeight.Medium,
                                        modifier = Modifier.padding(start = 12.dp, top = 4.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Confirm New Password
                            Column(modifier = Modifier.fillMaxWidth()) {
                                OutlinedTextField(
                                    value = state.confirmPassword,
                                    onValueChange = { viewModel.onConfirmPasswordChanged(it) },
                                    placeholder = { Text("Confirm password", color = Color.Gray.copy(alpha = 0.5f)) },
                                    singleLine = true,
                                    visualTransformation = if (state.isConfirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                    trailingIcon = {
                                        IconButton(onClick = { viewModel.onToggleConfirmPasswordVisibility() }) {
                                            Icon(
                                                imageVector = if (state.isConfirmPasswordVisible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                                                contentDescription = "Toggle visibility",
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
                                
                                // Password Strength Meter (added for step 3 password input)
                                if (state.newPassword.isNotEmpty()) {
                                    PasswordStrengthMeter(password = state.newPassword)
                                }
                            }

                            Spacer(modifier = Modifier.height(20.dp))

                            // Reset Button
                            Button(
                                onClick = {
                                    viewModel.onUpdatePassword {
                                        Toast.makeText(context, "Password reset successfully!", Toast.LENGTH_LONG).show()
                                        navController.navigateSafe("login") {
                                            popUpTo("login") { inclusive = true }
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
                                        "Save Password",
                                        color = Color.White,
                                        fontSize = 17.sp,
                                        fontFamily = Inter,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
