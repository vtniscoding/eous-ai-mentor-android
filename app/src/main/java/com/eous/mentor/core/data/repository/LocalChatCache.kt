package com.eous.mentor.core.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.eous.mentor.domain.model.ChatMessage
import com.eous.mentor.domain.model.ChatSession
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

object LocalChatCache {
    private const val PREFS_NAME = "eous_chat_disk_cache"
    private val json = Json { ignoreUnknownKeys = true }

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun saveSessions(context: Context, userId: String, sessions: List<ChatSession>) {
        if (userId.isBlank()) return
        try {
            getPrefs(context).edit().putString("sessions_$userId", json.encodeToString(sessions)).apply()
        } catch (e: Throwable) {
            e.printStackTrace()
        }
    }

    fun getSessions(context: Context, userId: String): List<ChatSession> {
        if (userId.isBlank()) return emptyList()
        val jsonStr = getPrefs(context).getString("sessions_$userId", null) ?: return emptyList()
        return try {
            json.decodeFromString<List<ChatSession>>(jsonStr)
        } catch (e: Throwable) {
            emptyList()
        }
    }

    fun saveMessages(context: Context, sessionId: String, messages: List<ChatMessage>) {
        if (sessionId.isBlank()) return
        try {
            getPrefs(context).edit().putString("messages_$sessionId", json.encodeToString(messages)).apply()
        } catch (e: Throwable) {
            e.printStackTrace()
        }
    }

    fun getMessages(context: Context, sessionId: String): List<ChatMessage> {
        if (sessionId.isBlank()) return emptyList()
        val jsonStr = getPrefs(context).getString("messages_$sessionId", null) ?: return emptyList()
        return try {
            json.decodeFromString<List<ChatMessage>>(jsonStr)
        } catch (e: Throwable) {
            emptyList()
        }
    }
}
