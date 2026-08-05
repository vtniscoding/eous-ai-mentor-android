package com.eous.mentor.core.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Data class for bottom nav items
data class MainNavItem(
        val route: String,
        val label: String,
        val selectedIcon: ImageVector,
        val unselectedIcon: ImageVector
)

val MainNavItems =
        listOf(
                MainNavItem("dashboard", "Home", Icons.Filled.Home, Icons.Outlined.Home),
                MainNavItem("library", "Library", Icons.Filled.Book, Icons.Outlined.Book),
                MainNavItem("chat", "Chat", Icons.Filled.ChatBubble, Icons.Outlined.ChatBubble),
                MainNavItem("alert", "Alert", Icons.Filled.Notifications, Icons.Outlined.Notifications),
                MainNavItem("personal", "Personal", Icons.Filled.Person, Icons.Outlined.Person)
        )

@Composable
fun MainNavigationBar(
        userId: String,
        currentScreen: String,
        onNavigate: (String) -> Unit,
        modifier: Modifier = Modifier
) {
        val context = LocalContext.current
        val hasUnreadAlerts = remember(currentScreen, userId) {
                com.eous.mentor.core.data.repository.NotificationRepository.getNotifications(context, userId)
                        .any { !it.isRead }
        }

        Box(
                modifier = modifier
                        .fillMaxWidth()
                        .shadow(
                                elevation = 16.dp,
                                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                                clip = false
                        )
                        .background(
                                color = Color.White,
                                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
                        )
                        .pointerInput(Unit) {
                                detectTapGestures { }
                        }
                        .navigationBarsPadding(),
                contentAlignment = Alignment.Center
        ) {
                Row(
                        modifier = Modifier
                                .fillMaxWidth()
                                .height(76.dp)
                                .padding(horizontal = 24.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                ) {
                        MainNavItems.forEach { item ->
                                val isSelected =
                                        currentScreen == item.route ||
                                                (item.route == "chat" &&
                                                        currentScreen == "search")
                                MainNavItemView(
                                        item = item,
                                        isSelected = isSelected,
                                        hasUnreadAlerts = hasUnreadAlerts,
                                        onClick = { onNavigate(item.route) }
                                )
                        }
                }
        }
}

@Composable
private fun RowScope.MainNavItemView(
        item: MainNavItem,
        isSelected: Boolean,
        hasUnreadAlerts: Boolean,
        onClick: () -> Unit
) {
        val purpleColor = Color(0xFF5B21B6)

        val capsuleColor by animateColorAsState(
                targetValue = if (isSelected) purpleColor else Color.Transparent,
                animationSpec = tween(durationMillis = 200),
                label = "nav_item_capsule"
        )
        val contentColor by animateColorAsState(
                targetValue = if (isSelected) Color.White else purpleColor.copy(alpha = 0.8f),
                animationSpec = tween(durationMillis = 200),
                label = "nav_item_content"
        )

        Box(
                modifier = Modifier
                        .clickable(
                                indication = null,
                                interactionSource = remember { MutableInteractionSource() },
                                onClick = onClick
                        )
                        .padding(vertical = 4.dp),
                contentAlignment = Alignment.Center
        ) {
                if (isSelected) {
                        Row(
                                modifier = Modifier
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(capsuleColor)
                                        .padding(horizontal = 14.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                        ) {
                                Box(contentAlignment = Alignment.TopEnd) {
                                        Icon(
                                                imageVector = item.selectedIcon,
                                                contentDescription = item.label,
                                                tint = contentColor,
                                                modifier = Modifier.size(22.dp)
                                        )
                                        if (item.route == "alert" && hasUnreadAlerts) {
                                                Box(
                                                        modifier = Modifier
                                                                .size(8.dp)
                                                                .offset(x = 2.dp, y = (-2).dp)
                                                                .background(Color.Red, CircleShape)
                                                                .border(1.dp, Color.White, CircleShape)
                                                )
                                        }
                                }
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                        text = item.label,
                                        color = contentColor,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1
                                )
                        }
                } else {
                        Box(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                contentAlignment = Alignment.Center
                        ) {
                                Box(contentAlignment = Alignment.TopEnd) {
                                        Icon(
                                                imageVector = item.unselectedIcon,
                                                contentDescription = item.label,
                                                tint = contentColor,
                                                modifier = Modifier.size(24.dp)
                                        )
                                        if (item.route == "alert" && hasUnreadAlerts) {
                                                Box(
                                                        modifier = Modifier
                                                                .size(8.dp)
                                                                .offset(x = 2.dp, y = (-2).dp)
                                                                .background(Color.Red, CircleShape)
                                                                .border(1.dp, Color.White, CircleShape)
                                                )
                                        }
                                }
                        }
                }
        }
}
