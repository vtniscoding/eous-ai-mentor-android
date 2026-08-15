package com.eous.mentor.features.auth.relogin

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.paint
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import com.eous.mentor.R
import com.eous.mentor.core.data.repository.SavedAccountsRepository
import com.eous.mentor.core.navigation.navigateSafe
import com.eous.mentor.core.ui.theme.Inter
import com.eous.mentor.domain.model.SavedAccount
import com.eous.mentor.features.auth.friendlyAuthError
import kotlinx.coroutines.launch
import io.github.jan.supabase.auth.auth
import com.eous.mentor.di.supabase
import io.github.jan.supabase.compose.auth.composeAuth
import io.github.jan.supabase.compose.auth.composable.rememberSignInWithGoogle
import io.github.jan.supabase.compose.auth.composable.NativeSignInResult
import com.eous.mentor.di.UseCaseProvider

private val HeaderPurple = Color(0xFF5B29A2)

@Composable
fun ReLoginScreen(
        navController: NavController,
        viewModel: ReLoginViewModel = viewModel(),
        onAddAccount: () -> Unit = { navController.navigateSafe("login") },
        onLoginSuccess: () -> Unit = {
            navController.navigateSafe("dashboard") { popUpTo("relogin") { inclusive = true } }
        }
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val state by viewModel.state.collectAsState()

    val googleAuthAction = supabase.composeAuth.rememberSignInWithGoogle(
        onResult = { result ->
            when (result) {
                is NativeSignInResult.Success -> {
                    scope.launch {
                        val sessionRepository = com.eous.mentor.di.RepositoryProvider.sessionRepository
                        val currentUid = sessionRepository.getCurrentUserId()
                        val currentEmail = sessionRepository.getCurrentUserEmail() ?: ""
                        if (!currentUid.isNullOrEmpty()) {
                            UseCaseProvider.issueLocalSession(context, currentUid)
                            val avatarUrl = UseCaseProvider.getProfile(currentUid).getOrNull()?.avatar_url
                            SavedAccountsRepository.saveAccount(
                                context,
                                SavedAccount(
                                    email = currentEmail,
                                    password = "", // Google sign-in has no password
                                    avatarUrl = avatarUrl
                                )
                            )
                        }
                        try {
                            val (current, next) = com.eous.mentor.di.supabase.auth.mfa.getAuthenticatorAssuranceLevel()
                            if (current == io.github.jan.supabase.auth.mfa.AuthenticatorAssuranceLevel.AAL1 &&
                                next == io.github.jan.supabase.auth.mfa.AuthenticatorAssuranceLevel.AAL2) {
                                Toast.makeText(context, "MFA Verification Required", Toast.LENGTH_SHORT).show()
                                navController.navigateSafe("mfa_verify") {
                                    popUpTo("relogin") { inclusive = true }
                                }
                            } else {
                                Toast.makeText(context, "Welcome back!", Toast.LENGTH_SHORT).show()
                                onLoginSuccess()
                            }
                        } catch (e: Throwable) {
                            Toast.makeText(context, "Welcome back!", Toast.LENGTH_SHORT).show()
                            onLoginSuccess()
                        }
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

    // Load from SharedPreferences off the composition phase to avoid blocking the UI thread
    LaunchedEffect(Unit) {
        viewModel.updateSavedAccounts(SavedAccountsRepository.getSavedAccounts(context))
    }

    if (state.accountToRemove != null) {
        AlertDialog(
                onDismissRequest = { viewModel.setAccountToRemove(null) },
                title = {
                    Text(
                            text = "Remove account",
                            fontFamily = Inter,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = Color(0xFF1E1D22)
                    )
                },
                text = {
                    Text(
                            text = "Are you sure you want to remove this account?",
                            fontFamily = Inter,
                            fontSize = 14.sp,
                            color = Color(0xFF64748B)
                    )
                },
                confirmButton = {
                    TextButton(
                            onClick = {
                                val target = state.accountToRemove
                                viewModel.setAccountToRemove(null)
                                if (target != null) {
                                    val updated =
                                            SavedAccountsRepository.removeAccount(
                                                    context,
                                                    target.email
                                            )
                                    viewModel.updateSavedAccounts(updated)
                                    Toast.makeText(context, "Account removed", Toast.LENGTH_SHORT)
                                            .show()
                                    if (updated.isEmpty()) {
                                        viewModel.setManageMode(false)
                                        onAddAccount()
                                    }
                                }
                            }
                    ) {
                        Text(
                                "Remove",
                                color = Color(0xFFE53935),
                                fontWeight = FontWeight.Bold,
                                fontFamily = Inter
                        )
                    }
                },
                dismissButton = {
                    TextButton(onClick = { viewModel.setAccountToRemove(null) }) {
                        Text("Cancel", color = Color(0xFF64748B), fontFamily = Inter)
                    }
                },
                containerColor = Color.White,
                shape = RoundedCornerShape(16.dp)
        )
    }

    Box(
            modifier =
                    Modifier.fillMaxSize()
                            .background(Color.White)
                            .paint(
                                    painter = painterResource(id = R.drawable.relogin_background),
                                    contentScale = ContentScale.FillBounds
                            )
    ) {
        Column(
                modifier =
                        Modifier.fillMaxSize()
                                .statusBarsPadding()
                                .navigationBarsPadding()
                                .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Content
            Column(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
            ) {
                // Mascot Image
                Image(
                        painter = painterResource(id = R.drawable.ic_greeting_eous_2),
                        contentDescription = "Eous Relogin Mascot",
                        modifier = Modifier.size(105.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Welcome back! Title
                Text(
                        text = "Welcome back!",
                        color = Color(0xFF1E1D22),
                        fontSize = 24.sp,
                        fontFamily = Inter,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Saved Accounts Card
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Color.White,
                    border = BorderStroke(1.5.dp, Color(0xFFC5C5C5)),
                    modifier = Modifier.fillMaxWidth(0.9f)
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        // Limit height to ~3.5 items and enable vertical scrolling when accounts list is large
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 220.dp)
                                .verticalScroll(rememberScrollState())
                        ) {
                            state.savedAccounts.forEach { account ->
                                SavedAccountItemRow(
                                    account = account,
                                    isManageMode = state.isManageMode,
                                    isLoggingIn = state.loggingInEmail == account.email,
                                    onSelect = {
                                        if (state.loggingInEmail == null && !state.isManageMode) {
                                            viewModel.loginAccount(context, account) { result ->
                                                result.onSuccess {
                                                    try {
                                                        val (current, next) = com.eous.mentor.di.supabase.auth.mfa.getAuthenticatorAssuranceLevel()
                                                        if (current == io.github.jan.supabase.auth.mfa.AuthenticatorAssuranceLevel.AAL1 &&
                                                            next == io.github.jan.supabase.auth.mfa.AuthenticatorAssuranceLevel.AAL2) {
                                                            Toast.makeText(context, "MFA Verification Required", Toast.LENGTH_SHORT).show()
                                                            navController.navigateSafe("mfa_verify") {
                                                                popUpTo("relogin") { inclusive = true }
                                                            }
                                                        } else {
                                                            Toast.makeText(
                                                                context,
                                                                "Welcome back, ${account.email}!",
                                                                Toast.LENGTH_SHORT
                                                            ).show()
                                                            onLoginSuccess()
                                                        }
                                                    } catch (e: Throwable) {
                                                        Toast.makeText(
                                                            context,
                                                            "Welcome back, ${account.email}!",
                                                            Toast.LENGTH_SHORT
                                                        ).show()
                                                        onLoginSuccess()
                                                    }
                                                }.onFailure { e ->
                                                    val msg = friendlyAuthError(e)
                                                    Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                                                    onAddAccount()
                                                }
                                            }
                                        }
                                    },
                                    onRemove = { viewModel.setAccountToRemove(account) }
                                )

                                HorizontalDivider(color = Color(0xFFE2E8F0), thickness = 1.dp)
                            }
                        }

                        // "+ Add an account" Row
                        AddAccountRow(onClick = { onAddAccount() })
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Sign In with Google Button
                Button(
                    onClick = { googleAuthAction.startFlow() },
                    shape = RoundedCornerShape(28.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.6f)),
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .height(52.dp)
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
                            fontSize = 16.sp,
                            fontFamily = Inter,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Bottom "Account Manage" Button
            Box(
                    modifier =
                            Modifier.padding(bottom = 36.dp).clickable {
                                viewModel.setManageMode(!state.isManageMode)
                            },
                    contentAlignment = Alignment.Center
            ) {
                Text(
                        text = if (state.isManageMode) "Done" else "Account Manage",
                        color = if (state.isManageMode) Color(0xFF5B29A2) else Color(0xFF1E1D22),
                        fontSize = 16.sp,
                        fontFamily = Inter,
                        fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun SavedAccountItemRow(
        account: SavedAccount,
        isManageMode: Boolean,
        isLoggingIn: Boolean,
        onSelect: () -> Unit,
        onRemove: () -> Unit
) {
    val initial = account.email.trim().firstOrNull()?.uppercaseChar()?.toString() ?: "U"

    Row(
            modifier =
                    Modifier.fillMaxWidth()
                            .height(60.dp)
                            .clickable { onSelect() }
                            .padding(horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically
    ) {
        // Avatar Circle
        Box(
                modifier = Modifier.size(36.dp).clip(CircleShape).background(Color(0xFFEBE6FF)),
                contentAlignment = Alignment.Center
        ) {
            if (!account.avatarUrl.isNullOrEmpty()) {
                AsyncImage(
                        model = account.avatarUrl,
                        contentDescription = "User Avatar",
                        modifier = Modifier.fillMaxSize().clip(CircleShape),
                        contentScale = ContentScale.Crop
                )
            } else {
                Text(
                        text = initial,
                        color = Color(0xFF7F43D4),
                        fontSize = 16.sp,
                        fontFamily = Inter,
                        fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.width(16.dp))

        // Email
        Text(
                text = account.email,
                color = Color(0xFF1E1D22),
                fontSize = 15.sp,
                fontFamily = Inter,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
        )

        if (isLoggingIn) {
            CircularProgressIndicator(
                    color = HeaderPurple,
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp
            )
        } else if (isManageMode) {
            IconButton(onClick = { onRemove() }, modifier = Modifier.size(28.dp)) {
                Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Remove account",
                        tint = Color(0xFFE53935)
                )
            }
        }
    }
}

@Composable
private fun AddAccountRow(onClick: () -> Unit) {
    Row(
            modifier =
                    Modifier.fillMaxWidth()
                            .height(60.dp)
                            .clickable { onClick() }
                            .padding(horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.size(36.dp), contentAlignment = Alignment.Center) {
            Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add account",
                    tint = Color(0xFF64748B),
                    modifier = Modifier.size(22.dp)
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Text(
                text = "Add an account",
                color = Color(0xFF64748B),
                fontSize = 15.sp,
                fontFamily = Inter,
                fontWeight = FontWeight.Medium
        )
    }
}
