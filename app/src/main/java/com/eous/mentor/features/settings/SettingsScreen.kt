package com.eous.mentor.features.settings

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.eous.mentor.core.ui.theme.Inter
import com.eous.mentor.core.ui.components.EousConfirmDialog
import com.eous.mentor.core.data.repository.NotificationRepository
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.mfa.FactorType
import coil3.compose.AsyncImage
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import kotlinx.coroutines.launch
import android.widget.Toast
import androidx.compose.ui.text.style.TextAlign

@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onLogout: () -> Unit,
    onOpenProfile: () -> Unit = {}
) {
    val context = LocalContext.current
    var showLogoutDialog by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    // States for toggles
    var pushEnabled by remember { mutableStateOf(NotificationRepository.isPushEnabled(context)) }
    var twoFactorEnabled by remember { mutableStateOf(false) }
    var darkModeEnabled by remember { mutableStateOf(true) }

    var showEnrollDialog by remember { mutableStateOf(false) }
    var showDisableConfirmDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        try {
            val status = com.eous.mentor.di.supabase.auth.mfa.status
            twoFactorEnabled = status.enabled
        } catch (e: Throwable) {
            twoFactorEnabled = false
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFFFAFAFA)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            // Top Header Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Setting",
                    color = Color.Black,
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = Inter
                )

                // Styled Save Button (No 3D shadow, Purple background & text according to new design)
                val saveInteractionSource = remember { MutableInteractionSource() }
                val isSaveHovered by saveInteractionSource.collectIsHoveredAsState()
                val isSavePressed by saveInteractionSource.collectIsPressedAsState()
                val saveScale by animateFloatAsState(
                    targetValue = if (isSavePressed) 0.94f else if (isSaveHovered) 1.04f else 1.0f,
                    animationSpec = spring(stiffness = Spring.StiffnessLow),
                    label = "save_scale"
                )

                Box(
                    modifier = Modifier
                        .graphicsLayer {
                            scaleX = saveScale
                            scaleY = saveScale
                        }
                        .background(
                            if (isSavePressed || isSaveHovered) Color(0xFFC7B1FF) else Color(0xFFD6C7FF),
                            RoundedCornerShape(20.dp)
                        )
                        .clickable(
                            interactionSource = saveInteractionSource,
                            indication = null
                        ) { onBack() }
                        .padding(horizontal = 22.dp, vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Save",
                        color = Color(0xFF6A3DE8),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = Inter
                    )
                }
            }

            HorizontalDivider(color = Color(0xFFE2E8F0), thickness = 1.dp)

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 20.dp)
            ) {
                // 1. ACCOUNT & SECURITY SECTION
                SettingsSectionTitle("Account & Security")
                Spacer(modifier = Modifier.height(8.dp))
                SettingsGroupCard {
                    SettingsNavigationRow(title = "Private Setting") {
                        // Action for Private Setting
                    }
                    HorizontalDivider(color = Color(0xFFE2E8F0), thickness = 1.dp)
                    SettingsNavigationRow(title = "Profile") {
                        onOpenProfile()
                    }
                    HorizontalDivider(color = Color(0xFFE2E8F0), thickness = 1.dp)
                    SettingsSwitchRow(
                        title = "Push Notification",
                        checked = pushEnabled,
                        onCheckedChange = {
                            pushEnabled = it
                            NotificationRepository.setPushEnabled(context, it)
                        }
                    )
                    HorizontalDivider(color = Color(0xFFE2E8F0), thickness = 1.dp)
                    SettingsSwitchRow(
                        title = "2-Step Verification",
                        checked = twoFactorEnabled,
                        onCheckedChange = { checked ->
                            if (checked) {
                                showEnrollDialog = true
                            } else {
                                showDisableConfirmDialog = true
                            }
                        }
                    )
                }

                Spacer(modifier = Modifier.height(22.dp))

                // 2. SYSTEM & APPEARANCE SECTION
                SettingsSectionTitle("System & Appearance")
                Spacer(modifier = Modifier.height(8.dp))
                SettingsGroupCard {
                    SettingsSwitchRow(
                        title = "Dark Mode",
                        checked = darkModeEnabled,
                        onCheckedChange = { darkModeEnabled = it }
                    )
                    HorizontalDivider(color = Color(0xFFE2E8F0), thickness = 1.dp)
                    SettingsNavigationRow(title = "Language") {
                        // Action for Language
                    }
                }

                Spacer(modifier = Modifier.height(22.dp))

                // 3. SUBSCRIPTION SECTION
                SettingsSectionTitle("Subscription")
                Spacer(modifier = Modifier.height(8.dp))
                SettingsGroupCard {
                    SettingsNavigationRow(title = "Choose Plan") {
                        // Action for Choose Plan
                    }
                }

                Spacer(modifier = Modifier.height(28.dp))

                // 4. LOG OUT BUTTON (Red-orange background, dark red text, no 3D shadow)
                val logoutInteractionSource = remember { MutableInteractionSource() }
                val isLogoutHovered by logoutInteractionSource.collectIsHoveredAsState()
                val isLogoutPressed by logoutInteractionSource.collectIsPressedAsState()
                val logoutScale by animateFloatAsState(
                    targetValue = if (isLogoutPressed) 0.96f else if (isLogoutHovered) 1.02f else 1.0f,
                    animationSpec = spring(stiffness = Spring.StiffnessLow),
                    label = "logout_scale"
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .graphicsLayer {
                            scaleX = logoutScale
                            scaleY = logoutScale
                        }
                        .background(
                            if (isLogoutPressed || isLogoutHovered) Color(0xFFFF9485) else Color(0xFFFFA599),
                            RoundedCornerShape(16.dp)
                        )
                        .clickable(
                            interactionSource = logoutInteractionSource,
                            indication = null
                        ) { showLogoutDialog = true }
                        .padding(vertical = 14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Log Out",
                        color = Color(0xFF8F1D1D),
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = Inter
                    )
                }

                Spacer(modifier = Modifier.height(48.dp))

                // 5. FOOTER LINKS (Purple text)
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "Term Of Service",
                        color = Color(0xFF6A3DE8),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = Inter,
                        modifier = Modifier.clickable { }
                    )
                    Text(
                        text = "Security Policy",
                        color = Color(0xFF6A3DE8),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = Inter,
                        modifier = Modifier.clickable { }
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    // Confirmation dialog before logging out
    if (showLogoutDialog) {
        EousConfirmDialog(
            title = "Log Out",
            message = "Are you sure you want to log out of your account?",
            confirmText = "Log Out",
            dismissText = "Cancel",
            isDestructive = true,
            onConfirm = {
                showLogoutDialog = false
                onLogout()
            },
            onDismiss = { showLogoutDialog = false }
        )
    }

    if (showEnrollDialog) {
        MfaEnrollDialog(
            onDismiss = { showEnrollDialog = false },
            onSuccess = {
                showEnrollDialog = false
                twoFactorEnabled = true
            }
        )
    }

    if (showDisableConfirmDialog) {
        EousConfirmDialog(
            title = "Disable 2-Step Verification?",
            message = "Are you sure you want to disable 2-Step Verification? Your account will be less secure.",
            confirmText = "Disable",
            dismissText = "Cancel",
            isDestructive = true,
            onConfirm = {
                showDisableConfirmDialog = false
                scope.launch {
                    try {
                        val factors = com.eous.mentor.di.supabase.auth.mfa.retrieveFactorsForCurrentUser()
                        for (factor in factors) {
                            com.eous.mentor.di.supabase.auth.mfa.unenroll(factor.id)
                        }
                        twoFactorEnabled = false
                        Toast.makeText(context, "2-Step Verification disabled.", Toast.LENGTH_SHORT).show()
                    } catch (e: Throwable) {
                        Toast.makeText(context, e.message ?: "Failed to disable 2FA.", Toast.LENGTH_LONG).show()
                    }
                }
            },
            onDismiss = {
                showDisableConfirmDialog = false
            }
        )
    }
}

@Composable
fun MfaEnrollDialog(
    onDismiss: () -> Unit,
    onSuccess: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var factorId by remember { mutableStateOf<String?>(null) }
    var totpQrCode by remember { mutableStateOf<String?>(null) }
    var totpSecret by remember { mutableStateOf<String?>(null) }
    var totpUri by remember { mutableStateOf<String?>(null) }
    var code by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Start enrollment on dialog launch
    LaunchedEffect(Unit) {
        try {
            val res = com.eous.mentor.di.supabase.auth.mfa.enroll(
                factorType = FactorType.TOTP,
                friendlyName = "Eous Mentor"
            ) {
                issuer = "Eous Mentor"
            }
            factorId = res.id
            val (_, _, qrCode) = res.data
            totpQrCode = qrCode
            // Extract secret and URI from the TOTP URI in qrCode data
            // The data contains (id, type, qrCode) for TOTP
            totpUri = res.data.uri
            totpSecret = res.data.secret
        } catch (e: Throwable) {
            errorMessage = e.message ?: "MFA enrollment failed to initiate."
        }
    }

    val handleCancel = {
        scope.launch {
            val fId = factorId
            if (fId != null) {
                try {
                    com.eous.mentor.di.supabase.auth.mfa.unenroll(fId)
                } catch (e: Throwable) {
                    // Ignore
                }
            }
            onDismiss()
        }
    }

    AlertDialog(
        onDismissRequest = { handleCancel() },
        title = {
            Text(
                text = "Setup 2-Step Verification",
                fontFamily = Inter,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = Color.Black
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                val fId = factorId
                if (fId == null && errorMessage == null) {
                    CircularProgressIndicator(
                        color = Color(0xFF6A3DE8),
                        modifier = Modifier.size(36.dp),
                        strokeWidth = 3.dp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Generating QR Code...",
                        fontFamily = Inter,
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                } else if (errorMessage != null) {
                    Text(
                        text = errorMessage!!,
                        color = Color.Red,
                        fontFamily = Inter,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center
                    )
                } else if (fId != null) {
                    val secret = totpSecret ?: ""
                    val uri = totpUri ?: ""
                    Text(
                        text = "Scan the QR code below using your authenticator app (Google Authenticator, Authy, etc.):",
                        fontFamily = Inter,
                        fontSize = 14.sp,
                        color = Color(0xFF64748B),
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    val qrUrl = "https://api.qrserver.com/v1/create-qr-code/?size=300x300&data=${android.net.Uri.encode(uri)}"
                    AsyncImage(
                        model = qrUrl,
                        contentDescription = "2FA QR Code",
                        modifier = Modifier
                            .size(180.dp)
                            .background(Color.White)
                            .border(1.dp, Color(0xFFCBD5E1), RoundedCornerShape(8.dp))
                            .padding(8.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Or enter the secret key manually:",
                        fontFamily = Inter,
                        fontSize = 14.sp,
                        color = Color(0xFF64748B)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFF1F5F9), RoundedCornerShape(8.dp))
                            .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(8.dp))
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = secret,
                            fontFamily = Inter,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF1E293B),
                            modifier = Modifier.weight(1f)
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        Text(
                            text = "Copy",
                            color = Color(0xFF6A3DE8),
                            fontFamily = Inter,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.clickable {
                                val clipboard = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                                val clip = android.content.ClipData.newPlainText("2FA Secret", secret)
                                clipboard.setPrimaryClip(clip)
                                Toast.makeText(context, "Secret key copied!", Toast.LENGTH_SHORT).show()
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Text(
                        text = "Enter the 6-digit verification code:",
                        fontFamily = Inter,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )

                    Spacer(modifier = Modifier.height(8.dp))

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
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        modifier = Modifier
                            .fillMaxWidth(0.8f)
                            .height(56.dp),
                        shape = RoundedCornerShape(12.dp),
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
            }
        },
        confirmButton = {
            val fId = factorId
            TextButton(
                enabled = fId != null && code.length == 6 && !isLoading,
                onClick = {
                    if (fId == null) return@TextButton
                    isLoading = true
                    scope.launch {
                        try {
                            com.eous.mentor.di.supabase.auth.mfa.createChallengeAndVerify(
                                factorId = fId,
                                code = code
                            )
                            Toast.makeText(context, "2-Step Verification enabled!", Toast.LENGTH_SHORT).show()
                            onSuccess()
                        } catch (e: Throwable) {
                            errorMessage = e.message ?: "Invalid verification code. Please try again."
                        } finally {
                            isLoading = false
                        }
                    }
                }
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        color = Color(0xFF6A3DE8),
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = "Verify & Enable",
                        color = Color(0xFF6A3DE8),
                        fontWeight = FontWeight.Bold,
                        fontFamily = Inter
                    )
                }
            }
        },
        dismissButton = {
            TextButton(onClick = { handleCancel() }) {
                Text("Cancel", color = Color(0xFF64748B), fontFamily = Inter)
            }
        },
        containerColor = Color.White,
        shape = RoundedCornerShape(20.dp)
    )
}

@Composable
private fun SettingsSectionTitle(title: String) {
    Text(
        text = title,
        color = Color(0xFF64748B),
        fontSize = 14.sp,
        fontWeight = FontWeight.Bold,
        fontFamily = Inter
    )
}

@Composable
private fun SettingsGroupCard(
    content: @Composable ColumnScope.() -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White, RoundedCornerShape(16.dp))
            .border(1.5.dp, Color(0xFF94A3B8), RoundedCornerShape(16.dp))
    ) {
        Column(content = content)
    }
}

@Composable
private fun SettingsNavigationRow(
    title: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            color = Color.Black,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = Inter
        )
        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = Color.Black,
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
private fun SettingsSwitchRow(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            color = Color.Black,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = Inter
        )
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = Color(0xFF6A3DE8),
                uncheckedThumbColor = Color(0xFF94A3B8),
                uncheckedTrackColor = Color(0xFFCBD5E1),
                checkedBorderColor = Color.Transparent,
                uncheckedBorderColor = Color.Transparent
            )
        )
    }
}
