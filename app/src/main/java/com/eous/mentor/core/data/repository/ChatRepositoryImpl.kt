package com.eous.mentor.core.data.repository

import com.eous.mentor.di.supabase
import com.eous.mentor.domain.model.*
import com.eous.mentor.domain.repository.ChatRepository
import io.github.jan.supabase.functions.functions
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.Order
import io.github.jan.supabase.storage.storage
import io.ktor.client.call.body
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import kotlinx.serialization.json.Json

class ChatRepositoryImpl : ChatRepository {
    private val json = Json { ignoreUnknownKeys = true }

    // ---- Sessions ----

    override suspend fun getSessions(userId: String): Result<List<ChatSession>> {
        return try {
            val sessions =
                    supabase.from("sessions")
                            .select {
                                filter { eq("user_id", userId) }
                                order("created_at", Order.DESCENDING)
                            }
                            .decodeList<ChatSession>()
            Result.success(sessions)
        } catch (e: Throwable) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    override suspend fun createSession(userId: String, title: String): Result<ChatSession> {
        return try {
            val session =
                    supabase.from("sessions")
                            .insert(ChatSession(user_id = userId, title = title)) { select() }
                            .decodeSingle<ChatSession>()
            Result.success(session)
        } catch (e: Throwable) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    override suspend fun updateSessionTitle(sessionId: String, title: String): Result<Unit> {
        return try {
            supabase.from("sessions").update({ set("title", title) }) {
                filter { eq("id", sessionId) }
            }
            Result.success(Unit)
        } catch (e: Throwable) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    override suspend fun updateSessionSubject(sessionId: String, subject: String): Result<Unit> {
        return try {
            supabase.from("sessions").update({ set("subject", subject) }) {
                filter { eq("id", sessionId) }
            }
            Result.success(Unit)
        } catch (e: Throwable) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    override suspend fun deleteSession(sessionId: String): Result<Unit> {
        return try {
            // Messages are cascade-deleted via FK or we delete explicitly
            supabase.from("messages").delete { filter { eq("session_id", sessionId) } }
            supabase.from("sessions").delete { filter { eq("id", sessionId) } }
            Result.success(Unit)
        } catch (e: Throwable) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    override suspend fun deleteAllSessions(userId: String): Result<Unit> {
        return try {
            supabase.from("messages").delete { filter { eq("user_id", userId) } }
            supabase.from("sessions").delete { filter { eq("user_id", userId) } }
            Result.success(Unit)
        } catch (e: Throwable) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    // ---- Messages ----

    override suspend fun getMessages(sessionId: String): Result<List<ChatMessage>> {
        return try {
            val messages =
                    supabase.from("messages")
                            .select {
                                filter { eq("session_id", sessionId) }
                                order("created_at", Order.ASCENDING)
                            }
                            .decodeList<ChatMessage>()
            Result.success(messages)
        } catch (e: Throwable) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    override suspend fun insertMessage(message: ChatMessage): Result<ChatMessage> {
        return try {
            val saved =
                    supabase.from("messages")
                            .insert(message) { select() }
                            .decodeSingle<ChatMessage>()

            Result.success(saved)
        } catch (e: Throwable) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    override suspend fun deleteMessage(messageId: String): Result<Unit> {
        return try {
            supabase.from("messages").delete { filter { eq("id", messageId) } }
            Result.success(Unit)
        } catch (e: Throwable) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    // ---- AI ----

    override suspend fun getAiResponse(
            message: String,
            history: List<ChatMessage>,
            imageUrl: String?,
            userContext: UserContext?
    ): Result<AiChatResponse> {
        return try {
            val historyItems =
                    history.map { msg ->
                        AiChatHistoryItem(
                                role = if (msg.role == "user") "user" else "model",
                                content = msg.content
                        )
                    }
            val request =
                    AiChatRequest(
                            message = message,
                            history = historyItems,
                            imageUrl = imageUrl,
                            userContext = userContext
                    )

            val response =
                    supabase.functions.invoke(
                            function = "ai-chat",
                            body = request,
                            headers =
                                    Headers.build {
                                        append(HttpHeaders.ContentType, "application/json")
                                    }
                    )
            val responseText = response.body<String>()
            val aiResponse = json.decodeFromString<AiChatResponse>(responseText)
            Result.success(aiResponse)
        } catch (e: Throwable) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    // ---- Bookmarks ----

    override suspend fun toggleBookmark(
            messageId: String,
            userId: String,
            isBookmarked: Boolean,
            folder: String
    ): Result<Unit> {
        return try {
            // Update the message's bookmark status
            supabase.from("messages").update({
                        set("is_bookmarked", isBookmarked)
                        set("bookmark_folder", if (isBookmarked) folder else null)
                    }) { filter { eq("id", messageId) } }

            if (isBookmarked) {
                // Insert into bookmarks table
                supabase.from("bookmarks")
                        .insert(Bookmark(user_id = userId, message_id = messageId, folder = folder))
            } else {
                // Remove from bookmarks table
                supabase.from("bookmarks").delete { filter { eq("message_id", messageId) } }
            }
            Result.success(Unit)
        } catch (e: Throwable) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    override suspend fun getBookmarkedMessages(userId: String): Result<List<ChatMessage>> {
        return try {
            val bookmarks =
                    supabase.from("bookmarks")
                            .select(columns = Columns.raw("*, message:messages(*)")) {
                                filter { eq("user_id", userId) }
                            }
                            .decodeList<BookmarkWithMessage>()

            val messages =
                    bookmarks.mapNotNull { b ->
                        b.message?.copy(is_bookmarked = true, bookmark_folder = b.folder)
                    }
            Result.success(messages)
        } catch (e: Throwable) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    // ---- Image Upload ----

    override suspend fun uploadImage(
            userId: String,
            fileName: String,
            imageBytes: ByteArray
    ): Result<String> {
        return try {
            val path = "chat-images/$userId/$fileName"
            val bucket = supabase.storage.from("chat-images")
            bucket.upload(path, imageBytes) { upsert = true }
            val publicUrl = bucket.publicUrl(path)
            Result.success(publicUrl)
        } catch (e: Throwable) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    // ---- Legacy (dashboard stats still uses these) ----

    override suspend fun sendMessage(message: String): Result<Unit> {
        return Result.success(Unit)
    }

    override suspend fun getLegacyMessages(userId: String): Result<List<Message>> {
        return try {
            val messages =
                    supabase.from("messages")
                            .select {
                                filter {
                                    if (userId.isNotEmpty()) {
                                        eq("user_id", userId)
                                    }
                                }
                            }
                            .decodeList<Message>()
            Result.success(messages)
        } catch (e: Throwable) {
            Result.failure(e)
        }
    }
}
