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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.eous.mentor.core.ui.theme.Inter
import com.eous.mentor.core.ui.components.EousConfirmDialog

private val PrimaryPurple = Color(0xFF5B29A2)

@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onLogout: () -> Unit,
    onOpenProfile: () -> Unit = {}
) {
    var showLogoutDialog by remember { mutableStateOf(false) }

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
                    text = "Settings",
                    color = Color.Black,
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = Inter
                )

                // Styled Done Button with 3D Solid Shadow & Hover effect
                val doneInteractionSource = remember { MutableInteractionSource() }
                val isDoneHovered by doneInteractionSource.collectIsHoveredAsState()
                val isDonePressed by doneInteractionSource.collectIsPressedAsState()
                val doneScale by animateFloatAsState(
                    targetValue = if (isDonePressed) 0.94f else if (isDoneHovered) 1.04f else 1.0f,
                    animationSpec = spring(stiffness = Spring.StiffnessLow),
                    label = "done_scale"
                )

                Box(
                    modifier = Modifier.graphicsLayer {
                        scaleX = doneScale
                        scaleY = doneScale
                    }
                ) {
                    // Solid dark shadow layer
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .offset(x = 3.dp, y = 3.dp)
                            .background(Color(0xFF4C1D95), RoundedCornerShape(20.dp))
                    )
                    // Upper layer pill button (darker when hovered/pressed)
                    Box(
                        modifier = Modifier
                            .background(
                                if (isDonePressed || isDoneHovered) Color(0xFFC4B2FF) else Color(0xFFD6C7FF),
                                RoundedCornerShape(20.dp)
                            )
                            .clickable(
                                interactionSource = doneInteractionSource,
                                indication = null
                            ) { onBack() }
                            .padding(horizontal = 22.dp, vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Done",
                            color = Color(0xFF4C1D95),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = Inter
                        )
                    }
                }
            }

            HorizontalDivider(color = Color(0xFFE2E8F0), thickness = 1.dp)

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 20.dp)
            ) {
                // 1. ACCOUNT SECTION
                SettingsSectionTitle("Account")
                Spacer(modifier = Modifier.height(8.dp))
                SettingsGroupCard(
                    items = listOf("Private Settings", "Profile", "Notification", "Security"),
                    onItemClick = { item ->
                        if (item == "Profile") {
                            onOpenProfile()
                        }
                    }
                )

                Spacer(modifier = Modifier.height(22.dp))

                // 2. SYSTEM SECTION ("Theme" changed to "Appearance")
                SettingsSectionTitle("System")
                Spacer(modifier = Modifier.height(8.dp))
                SettingsGroupCard(
                    items = listOf("Appearance", "Language")
                )

                Spacer(modifier = Modifier.height(22.dp))

                // 3. SUBSCRIPTION SECTION
                SettingsSectionTitle("Subscription")
                Spacer(modifier = Modifier.height(8.dp))
                SettingsGroupCard(
                    items = listOf("Choose Plan")
                )

                Spacer(modifier = Modifier.height(28.dp))

                // 4. LOG OUT BUTTON with 3D Solid Shadow & Hover effect
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
                ) {
                    // Solid dark shadow layer
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .offset(x = 4.dp, y = 4.dp)
                            .background(Color(0xFFC54636), RoundedCornerShape(20.dp))
                    )
                    // Main upper pill button (darker when hovered/pressed)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                if (isLogoutPressed || isLogoutHovered) Color(0xFFFF8576) else Color(0xFFFF9E90),
                                RoundedCornerShape(20.dp)
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
                            color = Color(0xFF7F1D1D),
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = Inter
                        )
                    }
                }

                Spacer(modifier = Modifier.height(48.dp))

                // 5. FOOTER LINKS
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "Terms Of Service",
                        color = PrimaryPurple,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = Inter,
                        modifier = Modifier.clickable { }
                    )
                    Text(
                        text = "Security Policy",
                        color = PrimaryPurple,
                        fontSize = 14.sp,
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
    items: List<String>,
    onItemClick: (String) -> Unit = {}
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White, RoundedCornerShape(16.dp))
            .border(1.5.dp, Color(0xFF94A3B8), RoundedCornerShape(16.dp))
    ) {
        Column {
            items.forEachIndexed { index, item ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onItemClick(item) }
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = item,
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
                if (index < items.size - 1) {
                    HorizontalDivider(color = Color(0xFFE2E8F0), thickness = 1.dp)
                }
            }
        }
    }
}
