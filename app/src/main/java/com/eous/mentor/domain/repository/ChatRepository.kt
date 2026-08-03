package com.eous.mentor.domain.repository

import com.eous.mentor.domain.model.AiChatResponse
import com.eous.mentor.domain.model.ChatMessage
import com.eous.mentor.domain.model.ChatSession
import com.eous.mentor.domain.model.Message
import com.eous.mentor.domain.model.UserContext

interface ChatRepository {
    // ---- Sessions ----
    suspend fun getSessions(userId: String): Result<List<ChatSession>>
    suspend fun createSession(userId: String, title: String = "New Chat"): Result<ChatSession>
    suspend fun deleteSession(sessionId: String): Result<Unit>
    suspend fun deleteAllSessions(userId: String): Result<Unit>
    suspend fun updateSessionTitle(sessionId: String, title: String): Result<Unit>
    suspend fun updateSessionSubject(sessionId: String, subject: String): Result<Unit>

    // ---- Messages ----
    suspend fun getMessages(sessionId: String): Result<List<ChatMessage>>
    suspend fun insertMessage(message: ChatMessage): Result<ChatMessage>

    // ---- AI ----
    suspend fun getAiResponse(
            message: String,
            history: List<ChatMessage>,
            imageUrl: String? = null,
            userContext: UserContext? = null
    ): Result<AiChatResponse>

    // ---- Bookmarks ----
    suspend fun toggleBookmark(
            messageId: String,
            userId: String,
            isBookmarked: Boolean,
            folder: String = "General"
    ): Result<Unit>

    suspend fun getBookmarkedMessages(userId: String): Result<List<ChatMessage>>

    // ---- Image Upload ----
    suspend fun uploadImage(userId: String, fileName: String, imageBytes: ByteArray): Result<String>

    // ---- Legacy (used by dashboard stats) ----
    suspend fun sendMessage(message: String): Result<Unit>
    suspend fun getLegacyMessages(userId: String): Result<List<Message>>
}
