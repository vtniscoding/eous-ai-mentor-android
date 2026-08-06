package com.eous.mentor.testutil

import com.eous.mentor.domain.model.*
import com.eous.mentor.domain.repository.ChatRepository

class FakeChatRepository : ChatRepository {

    var getAiResponseResult: Result<AiChatResponse> = Result.failure(NotImplementedError())
    var getAiResponseCallCount = 0; private set
    var lastAiResponsePrompt: String? = null; private set
    var lastAiResponseHistory: List<ChatMessage>? = null; private set

    var insertMessageCallCount = 0; private set
    var lastInsertedMessage: ChatMessage? = null; private set

    var updateSessionSubjectResult: Result<Unit> = Result.success(Unit)
    var updateSessionSubjectCallCount = 0; private set
    var lastUpdatedSessionId: String? = null; private set
    var lastUpdatedSubject: String? = null; private set

    var deleteSessionResult: Result<Unit> = Result.success(Unit)
    var deleteSessionCallCount = 0; private set
    var lastDeletedSessionId: String? = null; private set

    var deleteMessageResult: Result<Unit> = Result.success(Unit)
    var deleteMessageCallCount = 0; private set
    var lastDeletedMessageId: String? = null; private set

    var getSessionsResult: Result<List<ChatSession>> = Result.success(emptyList())
    var getMessagesResult: Result<List<ChatMessage>> = Result.success(emptyList())
    var getLegacyMessagesResult: Result<List<Message>> = Result.success(emptyList())
    var getBookmarkedMessagesResult: Result<List<ChatMessage>> = Result.success(emptyList())

    override suspend fun getSessions(userId: String): Result<List<ChatSession>> {
        val res = getSessionsResult
        return if (res.isSuccess) Result.success(res.getOrNull()!!) else Result.failure(res.exceptionOrNull()!!)
    }

    override suspend fun createSession(userId: String, title: String): Result<ChatSession> =
        Result.success(ChatSession(id = "new-session", user_id = userId, title = title))

    override suspend fun deleteSession(sessionId: String): Result<Unit> {
        deleteSessionCallCount++
        lastDeletedSessionId = sessionId
        val res = deleteSessionResult
        return if (res.isSuccess) Result.success(res.getOrNull()!!) else Result.failure(res.exceptionOrNull()!!)
    }

    override suspend fun deleteAllSessions(userId: String): Result<Unit> = Result.success(Unit)

    override suspend fun updateSessionTitle(sessionId: String, title: String): Result<Unit> = Result.success(Unit)

    override suspend fun updateSessionSubject(sessionId: String, subject: String): Result<Unit> {
        updateSessionSubjectCallCount++
        lastUpdatedSessionId = sessionId
        lastUpdatedSubject = subject
        val res = updateSessionSubjectResult
        return if (res.isSuccess) Result.success(res.getOrNull()!!) else Result.failure(res.exceptionOrNull()!!)
    }

    override suspend fun getMessages(sessionId: String): Result<List<ChatMessage>> {
        val res = getMessagesResult
        return if (res.isSuccess) Result.success(res.getOrNull()!!) else Result.failure(res.exceptionOrNull()!!)
    }

    var insertMessageResult: Result<ChatMessage>? = null

    override suspend fun insertMessage(message: ChatMessage): Result<ChatMessage> {
        insertMessageCallCount++
        lastInsertedMessage = message
        val res = insertMessageResult ?: return Result.success(message)
        return if (res.isSuccess) Result.success(res.getOrNull()!!) else Result.failure(res.exceptionOrNull()!!)
    }

    override suspend fun deleteMessage(messageId: String): Result<Unit> {
        deleteMessageCallCount++
        lastDeletedMessageId = messageId
        val res = deleteMessageResult
        return if (res.isSuccess) Result.success(res.getOrNull()!!) else Result.failure(res.exceptionOrNull()!!)
    }

    override suspend fun getAiResponse(
        message: String,
        history: List<ChatMessage>,
        imageUrl: String?,
        userContext: UserContext?
    ): Result<AiChatResponse> {
        getAiResponseCallCount++
        lastAiResponsePrompt = message
        lastAiResponseHistory = history
        val res = getAiResponseResult
        return if (res.isSuccess) Result.success(res.getOrNull()!!) else Result.failure(res.exceptionOrNull()!!)
    }

    override suspend fun toggleBookmark(
        messageId: String,
        userId: String,
        isBookmarked: Boolean,
        folder: String
    ): Result<Unit> = Result.success(Unit)

    override suspend fun getBookmarkedMessages(userId: String): Result<List<ChatMessage>> {
        val res = getBookmarkedMessagesResult
        return if (res.isSuccess) Result.success(res.getOrNull()!!) else Result.failure(res.exceptionOrNull()!!)
    }

    override suspend fun uploadImage(userId: String, fileName: String, imageBytes: ByteArray): Result<String> =
        Result.success("https://test.com/$fileName")

    override suspend fun sendMessage(message: String): Result<Unit> = Result.success(Unit)

    override suspend fun getLegacyMessages(userId: String): Result<List<Message>> {
        val res = getLegacyMessagesResult
        return if (res.isSuccess) Result.success(res.getOrNull()!!) else Result.failure(res.exceptionOrNull()!!)
    }
}
