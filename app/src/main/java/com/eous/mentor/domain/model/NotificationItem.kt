package com.eous.mentor.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class NotificationItem(
    val id: String,
    val title: String,
    val description: String,
    val time: String,
    val isRead: Boolean = false,
    val iconRes: Int? = null,
    val isFriendRequest: Boolean = false,
    val avatarLetter: String? = null,
    val section: String = "Today",
    val timestamp: Long = System.currentTimeMillis()
)
