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

@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onLogout: () -> Unit,
    onOpenProfile: () -> Unit = {}
) {
    val context = LocalContext.current
    var showLogoutDialog by remember { mutableStateOf(false) }

    // States for toggles
    var pushEnabled by remember { mutableStateOf(NotificationRepository.isPushEnabled(context)) }
    var twoFactorEnabled by remember { mutableStateOf(true) }
    var darkModeEnabled by remember { mutableStateOf(true) }

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
                        onCheckedChange = { twoFactorEnabled = it }
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
