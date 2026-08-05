package com.eous.mentor.features.alert

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.eous.mentor.R
import com.eous.mentor.core.ui.theme.Inter

private val PrimaryPurple = Color(0xFF5B29A2)
private val LightCardBg = Color(0xFFE2E4E8)

data class NotificationItem(
        val id: String,
        val title: String,
        val description: String,
        val time: String,
        val isRead: Boolean = false,
        val iconRes: Int? = null,
        val isFriendRequest: Boolean = false,
        val avatarLetter: String? = null,
        val section: String // "Today" or "Recent"
)

data class SuggestedUser(
        val id: String,
        val name: String,
        val avatarLetter: String,
        var isAdded: Boolean = false
)

@Composable
fun AlertScreen(onMenuClick: () -> Unit) {
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
                                    )
                                    .show()
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
        // Auto system push notification for Welcome
        showSystemNotification(
                context = context,
                title = "Welcome to Eous AI Mentor!",
                message =
                        "Get started by asking questions to your AI mentor and explore tools to boost your learning."
        )
    }

    // Initial notification list state using user-specified icons:
    // Welcome -> ic_welcome
    var notifications by remember {
        mutableStateOf(
                listOf(
                        NotificationItem(
                                id = "1",
                                title = "Welcome to Eous AI Mentor!",
                                description =
                                        "Get started by asking questions to your AI mentor and explore tools to boost your learning.",
                                time = "Just now",
                                iconRes = R.drawable.ic_welcome,
                                section = "Today"
                        )
                )
        )
    }

    // Suggested users list state
    var suggestedUsers by remember {
        mutableStateOf(
                listOf(
                        SuggestedUser("1", "Le Huu Dan", "D"),
                        SuggestedUser("2", "Truong Nguyen", "N"),
                        SuggestedUser("3", "Dinh Trieu", "T")
                )
        )
    }

    // Test notification pool to cycle when clicking "Test Push Notification":
    // Lost streak -> ic_unhappy_eous
    // You're on fire -> ic_fire_eous
    val testPool = remember {
        listOf(
                NotificationItem(
                        id = "req_dan",
                        title = "Le Huu Dan",
                        description = "Le Huu Dan send you friend request.",
                        time = "Yesterday",
                        isFriendRequest = true,
                        avatarLetter = "D",
                        section = "Recent"
                ),
                NotificationItem(
                        id = "lost_streak",
                        title = "You lost your streak...",
                        description =
                                "Am I not good enough or getting troubles with your motivations.",
                        time = "1 day ago",
                        iconRes = R.drawable.ic_unhappy_eous,
                        section = "Recent"
                ),
                NotificationItem(
                        id = "on_fire",
                        title = "You're on fire! Keep going!",
                        description =
                                "Congratulations on starting your learning streak! Check-in daily to build consistency.",
                        time = "3 days ago",
                        iconRes = R.drawable.ic_fire_eous,
                        section = "Recent"
                )
        )
    }

    var testIndex by remember { mutableIntStateOf(0) }

    Box(modifier = Modifier.fillMaxSize().background(Color(0xFFF1F3F5))) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header Bar
            Box(
                    modifier =
                            Modifier.fillMaxWidth()
                                    .shadow(elevation = 3.dp)
                                    .zIndex(1f)
                                    .background(Color.White)
                                    .statusBarsPadding()
                                    .padding(horizontal = 20.dp, vertical = 18.dp)
            ) {
                Text(
                        text = "Notifications",
                        color = Color.Black,
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = Inter
                )
            }

            // Notifications List & Content
            LazyColumn(
                    modifier = Modifier.fillMaxWidth().weight(1f).padding(horizontal = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(top = 16.dp, bottom = 140.dp)
            ) {
                // TODAY SECTION
                val todayItems = notifications.filter { it.section == "Today" }
                if (todayItems.isNotEmpty()) {
                    item { SectionHeader(title = "Today") }
                    items(todayItems, key = { it.id }) { item -> NotificationCard(item = item) }
                }

                // RECENT SECTION
                val recentItems = notifications.filter { it.section == "Recent" }
                if (recentItems.isNotEmpty()) {
                    item { SectionHeader(title = "Recent") }
                    items(recentItems, key = { it.id }) { item -> NotificationCard(item = item) }
                }

                // SOMEONE YOU MAY KNOW SECTION
                if (suggestedUsers.isNotEmpty()) {
                    item { SectionHeader(title = "Someone you may know") }
                    item {
                        LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                modifier = Modifier.fillMaxWidth()
                        ) {
                            items(suggestedUsers, key = { it.id }) { user ->
                                SuggestedUserCard(
                                        user = user,
                                        onAddFriend = {
                                            suggestedUsers =
                                                    suggestedUsers.map { u ->
                                                        if (u.id == user.id) u.copy(isAdded = true)
                                                        else u
                                                    }
                                            Toast.makeText(
                                                            context,
                                                            "Friend request sent to ${user.name}",
                                                            Toast.LENGTH_SHORT
                                                    )
                                                    .show()
                                        },
                                        onDismiss = {
                                            suggestedUsers =
                                                    suggestedUsers.filter { u -> u.id != user.id }
                                        }
                                )
                            }
                        }
                    }
                }

                // TEST PUSH NOTIFICATION BUTTON
                item {
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                            onClick = {
                                // Check permission for Android 13+
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                                                ContextCompat.checkSelfPermission(
                                                        context,
                                                        Manifest.permission.POST_NOTIFICATIONS
                                                ) != PackageManager.PERMISSION_GRANTED
                                ) {
                                    permissionLauncher.launch(
                                            Manifest.permission.POST_NOTIFICATIONS
                                    )
                                }

                                val nextItem = testPool[testIndex % testPool.size]
                                testIndex++

                                val newItem =
                                        nextItem.copy(
                                                id = "${nextItem.id}_${System.currentTimeMillis()}"
                                        )
                                notifications = notifications + newItem

                                // Send real Android system push notification
                                showSystemNotification(
                                        context = context,
                                        title = newItem.title,
                                        message = newItem.description
                                )
                            },
                            modifier = Modifier.fillMaxWidth().height(52.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple),
                            shape = RoundedCornerShape(16.dp)
                    ) {
                        Icon(
                                imageVector = Icons.Default.NotificationsActive,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                                text = "Test Push Notification 🔔",
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = Inter
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.padding(vertical = 4.dp)
    ) {
        Text(
                text = title,
                color = Color.Black,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = Inter
        )
        Image(
                painter = painterResource(id = R.drawable.ic_heart),
                contentDescription = "Heart Icon",
                modifier = Modifier.size(18.dp)
        )
    }
}

@Composable
private fun NotificationCard(item: NotificationItem) {
    Box(
            modifier =
                    Modifier.fillMaxWidth()
                            .background(LightCardBg, RoundedCornerShape(20.dp))
                            .padding(14.dp)
    ) {
        Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon / Avatar
            if (item.isFriendRequest && item.avatarLetter != null) {
                Box(
                        modifier =
                                Modifier.size(46.dp)
                                        .background(Color(0xFFD8B4FE), CircleShape)
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
            } else if (item.iconRes != null) {
                Image(
                        painter = painterResource(id = item.iconRes),
                        contentDescription = null,
                        modifier = Modifier.size(46.dp)
                )
            }

            // Text details
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
                            color = Color.Black,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = Inter,
                            modifier = Modifier.weight(1f)
                    )
                    Text(
                            text = item.time,
                            color = Color(0xFF64748B),
                            fontSize = 11.sp,
                            fontFamily = Inter
                    )
                }
                Text(
                        text = item.description,
                        color = Color(0xFF334155),
                        fontSize = 12.sp,
                        lineHeight = 16.sp,
                        fontFamily = Inter
                )
            }
        }
    }
}

@Composable
private fun SuggestedUserCard(user: SuggestedUser, onAddFriend: () -> Unit, onDismiss: () -> Unit) {
    Box(
            modifier =
                    Modifier.width(115.dp)
                            .background(LightCardBg, RoundedCornerShape(20.dp))
                            .padding(vertical = 12.dp, horizontal = 8.dp)
    ) {
        // Dismiss button top right
        Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Dismiss",
                tint = Color(0xFF64748B),
                modifier = Modifier.size(16.dp).align(Alignment.TopEnd).clickable { onDismiss() }
        )

        Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
        ) {
            Spacer(modifier = Modifier.height(4.dp))

            // Avatar
            Box(
                    modifier =
                            Modifier.size(46.dp)
                                    .background(Color(0xFFD8B4FE), CircleShape)
                                    .border(1.5.dp, PrimaryPurple, CircleShape),
                    contentAlignment = Alignment.Center
            ) {
                Text(
                        text = user.avatarLetter,
                        color = PrimaryPurple,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = Inter
                )
            }

            // Name
            Text(
                    text = user.name,
                    color = Color.Black,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = Inter,
                    maxLines = 1
            )

            // Add friend button
            Button(
                    onClick = onAddFriend,
                    enabled = !user.isAdded,
                    modifier = Modifier.fillMaxWidth().height(30.dp),
                    contentPadding = PaddingValues(0.dp),
                    colors =
                            ButtonDefaults.buttonColors(
                                    containerColor =
                                            if (user.isAdded) Color(0xFF94A3B8) else PrimaryPurple,
                                    disabledContainerColor = Color(0xFF94A3B8)
                            ),
                    shape = RoundedCornerShape(10.dp)
            ) {
                Text(
                        text = if (user.isAdded) "Requested" else "Add friend",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = Inter
                )
            }
        }
    }
}

private fun showSystemNotification(context: Context, title: String, message: String) {
    try {
        val channelId = "eous_alerts_channel"
        val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val channel =
                NotificationChannel(
                                channelId,
                                "Eous Mentor Notifications",
                                NotificationManager.IMPORTANCE_HIGH
                        )
                        .apply {
                            description = "Notifications from Eous AI Mentor"
                            enableVibration(true)
                        }
        notificationManager.createNotificationChannel(channel)

        val builder =
                NotificationCompat.Builder(context, channelId)
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setContentTitle(title)
                        .setContentText(message)
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .setDefaults(NotificationCompat.DEFAULT_ALL)
                        .setAutoCancel(true)

        notificationManager.notify(System.currentTimeMillis().toInt(), builder.build())
    } catch (e: Exception) {
        e.printStackTrace()
    }
}
