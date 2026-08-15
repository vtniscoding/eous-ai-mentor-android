package com.eous.mentor.features.alert

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import com.eous.mentor.R
import com.eous.mentor.core.data.repository.NotificationRepository
import com.eous.mentor.core.navigation.navigateSafe
import com.eous.mentor.core.ui.theme.Inter
import com.eous.mentor.domain.model.NotificationItem

private val PrimaryPurple = Color(0xFF5B29A2)
private val LightCardBg = Color(0xFFE2E4E8)

@Composable
fun AlertScreen(
    userId: String,
    navController: NavController,
    onMenuClick: () -> Unit
) {
    val context = LocalContext.current

    // Request notification permission launcher for Android 13+ (API 33+)
    val permissionLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestPermission(),
            onResult = { isGranted ->
                if (!isGranted) {
                    Toast.makeText(
                        context,
                        "Please grant Notification permission in Settings to receive push alerts.",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        )

    // Check & request permission on screen entry
    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    var notifications by remember(userId) {
        mutableStateOf(NotificationRepository.getNotifications(context, userId))
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF1F3F5)) // Nền xám nhạt nhẹ nhàng
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            // Header: Cùng kích thước 20.sp và kiểu dáng như các trang khác
            Text(
                text = "Notification",
                color = Color.Black,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = Inter,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 20.dp)
            )

            // Notifications List
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(top = 8.dp, bottom = 140.dp)
            ) {
                if (notifications.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 40.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No notifications yet.",
                                color = Color(0xFF64748B),
                                fontSize = 14.sp,
                                fontFamily = Inter
                            )
                        }
                    }
                } else {
                    items(notifications, key = { it.id }) { item ->
                        NotificationCard(
                            item = item,
                            onClick = {
                                NotificationRepository.markAsRead(context, userId, item.id)
                                notifications = NotificationRepository.getNotifications(context, userId)
                                if (item.isFriendRequest) {
                                    navController.navigateSafe("friends?tab=2")
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun NotificationCard(
    item: NotificationItem,
    onClick: () -> Unit
) {
    val localContext = LocalContext.current
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(LightCardBg, RoundedCornerShape(24.dp)) // Bo góc lớn 24.dp sang trọng
            .clip(RoundedCornerShape(24.dp))
            .clickable { onClick() }
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon / Avatar tròn bên trái
            if (item.isFriendRequest && item.avatarLetter != null) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .background(PrimaryPurple.copy(alpha = 0.1f), CircleShape)
                        .border(1.5.dp, PrimaryPurple, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = item.avatarLetter,
                        color = PrimaryPurple,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = Inter
                    )
                }
            } else if (item.iconRes != null && isResourceValid(localContext, item.iconRes)) {
                Image(
                    painter = painterResource(id = item.iconRes),
                    contentDescription = null,
                    modifier = Modifier.size(46.dp)
                )
            } else {
                val fallbackId = R.drawable.ic_welcome
                if (isResourceValid(localContext, fallbackId)) {
                    Image(
                        painter = painterResource(id = fallbackId),
                        contentDescription = null,
                        modifier = Modifier.size(46.dp)
                    )
                }
            }

            // Text chi tiết bên phải
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = item.title,
                        color = if (item.isRead) Color(0xFF64748B) else Color.Black,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = Inter,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = NotificationRepository.formatRelativeTime(item.timestamp),
                        color = Color(0xFF64748B),
                        fontSize = 11.sp,
                        fontFamily = Inter
                    )
                }
                Text(
                    text = item.description,
                    color = if (item.isRead) Color(0xFF94A3B8) else Color(0xFF334155),
                    fontSize = 12.sp,
                    lineHeight = 16.sp,
                    fontFamily = Inter
                )
            }
        }
    }
}

private fun isResourceValid(context: android.content.Context, resId: Int): Boolean {
    return try {
        val drawable = androidx.core.content.ContextCompat.getDrawable(context, resId) ?: return false
        val className = drawable.javaClass.name
        className.contains("VectorDrawable") || className.contains("BitmapDrawable")
    } catch (e: Throwable) {
        false
    }
}
