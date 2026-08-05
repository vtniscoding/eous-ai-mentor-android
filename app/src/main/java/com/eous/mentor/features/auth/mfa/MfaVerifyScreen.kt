package com.eous.mentor.features.auth.mfa

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.eous.mentor.core.navigation.navigateSafe
import com.eous.mentor.core.ui.theme.Inter
import com.eous.mentor.features.auth.friendlyAuthError
import com.eous.mentor.di.supabase
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.launch

@Composable
fun MfaVerifyScreen(navController: NavController) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var code by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    // Store only the factorId string, not the MfaFactor object
    var verifiedFactorId by remember { mutableStateOf<String?>(null) }

    // Fetch verified factor to challenge
    LaunchedEffect(Unit) {
        try {
            val factors = supabase.auth.mfa.verifiedFactors
            val firstFactor = factors.firstOrNull()
            if (firstFactor != null) {
                verifiedFactorId = firstFactor.id
            } else {
                errorMessage = "No verified 2FA factor found. Please register 2FA first."
            }
        } catch (e: Throwable) {
            errorMessage = friendlyAuthError(e)
        }
    }

    val handleCancel = {
        scope.launch {
            try {
                supabase.auth.signOut()
            } catch (e: Throwable) {
                // Ignore sign out error
            }
            navController.navigateSafe("login") {
                popUpTo(0) { inclusive = true }
            }
        }
    }

    BackHandler {
        handleCancel()
    }

    // Custom premium background styling
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF965BE9)), // Matched theme color
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "2-Step Verification",
                color = Color.Black,
                fontSize = 28.sp,
                fontFamily = Inter,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Enter the 6-digit code from your authenticator app to access your account.",
                color = Color.Black.copy(alpha = 0.7f),
                fontSize = 15.sp,
                fontFamily = Inter,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(32.dp))

            if (errorMessage != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFFFFD1D1))
                        .border(1.5.dp, Color(0xFFE53935), RoundedCornerShape(16.dp))
                        .padding(vertical = 12.dp, horizontal = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = errorMessage!!,
                        color = Color(0xFF8B0000),
                        fontSize = 14.sp,
                        fontFamily = Inter,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            OutlinedTextField(
                value = code,
                onValueChange = {
                    if (it.length <= 6) code = it
                    errorMessage = null
                },
                placeholder = { Text("000000", color = Color.Gray.copy(alpha = 0.5f)) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Done
                ),
                textStyle = LocalTextStyle.current.copy(
                    textAlign = TextAlign.Center,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                ),
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .height(64.dp),
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

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    val factorId = verifiedFactorId
                    if (factorId == null) {
                        errorMessage = "No factor loaded yet."
                        return@Button
                    }
                    if (code.length != 6) {
                        errorMessage = "Please enter a valid 6-digit code."
                        return@Button
                    }
                    isLoading = true
                    scope.launch {
                        try {
                            supabase.auth.mfa.createChallengeAndVerify(
                                factorId = factorId,
                                code = code
                            )
                            Toast.makeText(context, "Verification successful!", Toast.LENGTH_SHORT).show()
                            navController.navigateSafe("dashboard") {
                                popUpTo(0) { inclusive = true }
                            }
                        } catch (e: Throwable) {
                            errorMessage = friendlyAuthError(e)
                        } finally {
                            isLoading = false
                        }
                    }
                },
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF252425)),
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .height(54.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = "Verify",
                        color = Color.White,
                        fontSize = 17.sp,
                        fontFamily = Inter,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            TextButton(
                onClick = { handleCancel() }
            ) {
                Text(
                    text = "Cancel & Sign Out",
                    color = Color.Black,
                    fontSize = 15.sp,
                    fontFamily = Inter,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
