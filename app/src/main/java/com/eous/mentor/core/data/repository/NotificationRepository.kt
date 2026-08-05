package com.eous.mentor.core.data.repository

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import androidx.core.app.NotificationCompat
import com.eous.mentor.MainActivity
import com.eous.mentor.R
import com.eous.mentor.domain.model.NotificationItem
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

object NotificationRepository {
    private const val KEY_NOTIFICATIONS = "notifications_json"
    private const val KEY_SHOWN_FRIEND_REQUESTS = "shown_friend_requests_json"
    private const val CHANNEL_ID = "eous_alerts_channel"

    private val json = Json { ignoreUnknownKeys = true }
    private val welcomeSentUsers = mutableSetOf<String>()

    private fun getPrefs(context: Context, userId: String): SharedPreferences {
        val safeId = if (userId.isBlank()) "default" else userId
        return context.getSharedPreferences("eous_notifications_$safeId", Context.MODE_PRIVATE)
    }

    fun getNotifications(context: Context, userId: String): List<NotificationItem> {
        val jsonStr = getPrefs(context, userId).getString(KEY_NOTIFICATIONS, null) ?: return emptyList()
        return try {
            json.decodeFromString<List<NotificationItem>>(jsonStr)
        } catch (e: Throwable) {
            emptyList()
        }
    }

    private fun saveNotifications(context: Context, userId: String, list: List<NotificationItem>) {
        try {
            getPrefs(context, userId).edit().putString(KEY_NOTIFICATIONS, json.encodeToString(list)).apply()
        } catch (e: Throwable) {
            e.printStackTrace()
        }
    }

    fun addNotification(context: Context, userId: String, item: NotificationItem) {
        val current = getNotifications(context, userId).toMutableList()
        // Remove old same ID if exists
        current.removeAll { it.id == item.id }
        current.add(0, item)
        
        // GIỚI HẠN TỐI ĐA 50 THÔNG BÁO GẦN NHẤT (Phương án dọn dẹp để không ảnh hưởng bộ nhớ)
        val limit = 50
        val final = if (current.size > limit) current.take(limit) else current
        
        saveNotifications(context, userId, final)
    }

    fun formatRelativeTime(timestamp: Long): String {
        val diff = System.currentTimeMillis() - timestamp
        val seconds = diff / 1000
        val minutes = seconds / 60
        val hours = minutes / 60
        val days = hours / 24

        return when {
            diff < 0 -> "Just now"
            seconds < 60 -> "Just now"
            minutes < 60 -> "${minutes}m ago"
            hours < 24 -> "${hours}h ago"
            days == 1L -> "Yesterday"
            days < 30 -> "${days}d ago"
            else -> {
                val sdf = java.text.SimpleDateFormat("dd MMM yyyy", java.util.Locale.ENGLISH)
                sdf.format(java.util.Date(timestamp))
            }
        }
    }

    fun markAsRead(context: Context, userId: String, id: String) {
        val current = getNotifications(context, userId).map {
            if (it.id == id) it.copy(isRead = true) else it
        }
        saveNotifications(context, userId, current)
    }

    fun clearAll(context: Context, userId: String) {
        getPrefs(context, userId).edit().remove(KEY_NOTIFICATIONS).apply()
    }

    // --- Welcome Message Logic ---
    fun checkAndSendWelcomeNotification(context: Context, userId: String) {
        if (userId.isBlank()) return
        if (!welcomeSentUsers.contains(userId)) {
            welcomeSentUsers.add(userId)
            val currentList = getNotifications(context, userId)
            val welcomeId = "welcome_msg"
            val hasWelcome = currentList.any { it.id == welcomeId }

            if (!hasWelcome) {
                val welcomeItem = NotificationItem(
                    id = welcomeId,
                    title = "Welcome to Eous AI Mentor!",
                    description = "Get started by asking questions to your AI mentor and explore tools to boost your learning.",
                    time = "Just now",
                    iconRes = R.drawable.ic_welcome,
                    isFriendRequest = false,
                    section = "Today"
                )
                addNotification(context, userId, welcomeItem)
            }
            // Always show system notification when starting app
            showSystemNotification(
                context = context,
                title = "Welcome to Eous AI Mentor!",
                message = "Get started by asking questions to your AI mentor and explore tools to boost your learning.",
                navigateToRoute = "alert" // Click will navigate to alert tab
            )
        }
    }

    // --- Friend Request Already Shown Check ---
    private fun getShownFriendRequests(context: Context, userId: String): Set<String> {
        val jsonStr = getPrefs(context, userId).getString(KEY_SHOWN_FRIEND_REQUESTS, null) ?: return emptySet()
        return try {
            json.decodeFromString<Set<String>>(jsonStr)
        } catch (e: Throwable) {
            emptySet()
        }
    }

    fun markFriendRequestAsShown(context: Context, userId: String, friendshipId: String) {
        val current = getShownFriendRequests(context, userId).toMutableSet()
        current.add(friendshipId)
        try {
            getPrefs(context, userId).edit().putString(KEY_SHOWN_FRIEND_REQUESTS, json.encodeToString(current)).apply()
        } catch (e: Throwable) {
            e.printStackTrace()
        }
    }

    fun isFriendRequestShown(context: Context, userId: String, friendshipId: String): Boolean {
        return getShownFriendRequests(context, userId).contains(friendshipId)
    }

    // --- System Push Notification Generator ---
    fun showSystemNotification(context: Context, title: String, message: String, navigateToRoute: String? = null) {
        try {
            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotificationChannel(
                    CHANNEL_ID,
                    "Eous Mentor Notifications",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "Notifications from Eous AI Mentor"
                    enableVibration(true)
                }
                notificationManager.createNotificationChannel(channel)
            }

            // Create navigation intent
            val intent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
                if (navigateToRoute != null) {
                    putExtra("navigate_to", navigateToRoute)
                }
            }

            val pendingIntent = PendingIntent.getActivity(
                context,
                System.currentTimeMillis().toInt(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val builder = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)

            notificationManager.notify(System.currentTimeMillis().toInt(), builder.build())
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
