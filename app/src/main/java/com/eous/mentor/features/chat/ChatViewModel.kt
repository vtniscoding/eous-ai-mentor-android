package com.eous.mentor.features.chat

import android.content.Context
import android.net.Uri
import com.eous.mentor.core.data.repository.LocalChatCache
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eous.mentor.di.UseCaseProvider
import com.eous.mentor.domain.model.*
import com.eous.mentor.domain.usecase.chat.*
import com.eous.mentor.domain.usecase.bookmark.ToggleBookmarkUseCase
import com.eous.mentor.domain.usecase.profile.GetProfileUseCase
import com.eous.mentor.domain.usecase.profile.UpdateExplanationStyleUseCase
import java.util.UUID
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ChatViewModel(
    private val userId: String,
    initialQuestion: String = "",
    private val getProfileUseCase: GetProfileUseCase = UseCaseProvider.getProfile,
    private val getSessionsUseCase: GetSessionsUseCase = UseCaseProvider.getSessions,
    private val createSessionUseCase: CreateSessionUseCase = UseCaseProvider.createSession,
    private val deleteSessionUseCase: DeleteSessionUseCase = UseCaseProvider.deleteSession,
    private val deleteAllSessionsUseCase: DeleteAllSessionsUseCase = UseCaseProvider.deleteAllSessions,
    private val renameSessionUseCase: RenameSessionUseCase = UseCaseProvider.renameSession,
    private val getSessionMessagesUseCase: GetSessionMessagesUseCase = UseCaseProvider.getSessionMessages,
    private val insertMessageUseCase: InsertMessageUseCase = UseCaseProvider.insertMessage,
    private val requestAiReplyUseCase: RequestAiReplyUseCase = UseCaseProvider.requestAiReply,
    private val toggleBookmarkUseCase: ToggleBookmarkUseCase = UseCaseProvider.toggleBookmark,
    private val uploadChatImageUseCase: UploadChatImageUseCase = UseCaseProvider.uploadChatImage,
    private val updateExplanationStyleUseCase: UpdateExplanationStyleUseCase = UseCaseProvider.updateExplanationStyle
) : ViewModel() {
    private val _state = MutableStateFlow(ChatState())
    val state: StateFlow<ChatState> = _state.asStateFlow()

    private var aiResponseJob: Job? = null
    private val sessionMessagesCache =
            java.util.concurrent.ConcurrentHashMap<String, List<ChatMessage>>()
    private var userProfile: Profile? = null

    init {
        loadSessions(initialQuestion = initialQuestion)
        loadUserProfile()
    }

    private fun loadUserProfile() {
        viewModelScope.launch {
            getProfileUseCase(userId).onSuccess { profile ->
                userProfile = profile
                profile?.explanation_style?.let { style ->
                    _state.update { it.copy(explanationStyle = style) }
                }
            }
        }
    }

    // ---- Session Management ----

    fun loadSessions(context: android.content.Context? = null, initialQuestion: String = "") {
        viewModelScope.launch {
            if (context != null) {
                val cachedDiskSessions = LocalChatCache.getSessions(context, userId)
                if (cachedDiskSessions.isNotEmpty()) {
                    _state.update {
                        it.copy(
                            sessions = cachedDiskSessions,
                            isLoadingSessions = false
                        )
                    }
                } else {
                    _state.update { it.copy(isLoadingSessions = true) }
                }
            } else if (_state.value.sessions.isEmpty()) {
                _state.update { it.copy(isLoadingSessions = true) }
            }

            getSessionsUseCase(userId)
                    .onSuccess { sessions ->
                        if (context != null) {
                            LocalChatCache.saveSessions(context, userId, sessions)
                        }
                        _state.update {
                            it.copy(
                                    sessions = sessions,
                                    isLoadingSessions = false
                            )
                        }

                        if (sessions.isNotEmpty()) {
                            preloadAllSessionsMessages(context, sessions)
                        }

                        if (initialQuestion.isNotEmpty()) {
                            _state.update { it.copy(inputText = initialQuestion) }
                        }
                    }
                    .onFailure { e ->
                        val fallback = if (context != null) LocalChatCache.getSessions(context, userId) else _state.value.sessions
                        _state.update {
                            it.copy(
                                sessions = fallback,
                                isLoadingSessions = false
                            )
                        }
                    }
        }
    }

    fun startNewChat() {
        _state.update {
            it.copy(
                    activeSession = null,
                    messages = emptyList(),
                    inputText = "",
                    pendingImageUri = null,
                    pendingImageUrl = null
            )
        }
    }

    fun createNewSession(initialQuestion: String = "") {
        viewModelScope.launch {
            createSessionUseCase(userId)
                    .onSuccess { session ->
                        sessionMessagesCache[session.id!!] = emptyList()
                        _state.update { state ->
                            state.copy(
                                    sessions = listOf(session) + state.sessions,
                                    activeSession = session,
                                    messages = emptyList(),
                                    isLoadingSessions = false,
                                    isSessionDrawerOpen = false
                            )
                        }
                        if (initialQuestion.isNotEmpty()) {
                            _state.update { it.copy(inputText = initialQuestion) }
                        }
                    }
                    .onFailure { e -> _state.update { it.copy(errorMessage = e.message) } }
        }
    }

    fun selectSession(session: ChatSession, context: android.content.Context? = null) {
        if (session.id == _state.value.activeSession?.id) {
            _state.update { it.copy(isSessionDrawerOpen = false) }
            return
        }
        val cachedMessages = sessionMessagesCache[session.id] ?: (if (context != null) LocalChatCache.getMessages(context, session.id!!) else null)
        if (cachedMessages != null && cachedMessages.isNotEmpty()) {
            sessionMessagesCache[session.id!!] = cachedMessages
            _state.update {
                it.copy(
                        activeSession = session,
                        messages = cachedMessages,
                        isLoadingMessages = false,
                        isSessionDrawerOpen = false
                )
            }
        } else {
            _state.update {
                it.copy(
                        activeSession = session,
                        messages = emptyList(),
                        isLoadingMessages = true,
                        isSessionDrawerOpen = false
                )
            }
        }
        loadMessages(context, session.id!!)
    }

    fun refresh(context: android.content.Context) {
        val currentSession = _state.value.activeSession
        loadSessions(context)
        if (currentSession?.id != null) {
            loadMessages(context, currentSession.id)
        }
    }

    fun deleteSession(sessionId: String) {
        viewModelScope.launch {
            deleteSessionUseCase(sessionId)
                    .onSuccess {
                        sessionMessagesCache.remove(sessionId)
                        _state.update { state ->
                            val remaining = state.sessions.filter { it.id != sessionId }
                            state.copy(sessions = remaining)
                        }
                        // If we deleted the active session, switch to another or create new
                        if (_state.value.activeSession?.id == sessionId) {
                            val remaining = _state.value.sessions
                            if (remaining.isNotEmpty()) {
                                selectSession(remaining.first())
                            } else {
                                createNewSession()
                            }
                        }
                    }
                    .onFailure { e -> _state.update { it.copy(errorMessage = e.message) } }
        }
    }

    fun renameSession(sessionId: String, newTitle: String) {
        viewModelScope.launch {
            renameSessionUseCase(sessionId, newTitle)
                    .onSuccess {
                        _state.update { state ->
                            val updatedSessions =
                                    state.sessions.map { s ->
                                        if (s.id == sessionId) s.copy(title = newTitle) else s
                                    }
                            val updatedActiveSession =
                                    if (state.activeSession?.id == sessionId) {
                                        state.activeSession.copy(title = newTitle)
                                    } else {
                                        state.activeSession
                                    }
                            state.copy(
                                    sessions = updatedSessions,
                                    activeSession = updatedActiveSession
                            )
                        }
                    }
                    .onFailure { e -> _state.update { it.copy(errorMessage = e.message) } }
        }
    }

    fun deleteAllSessions() {
        viewModelScope.launch {
            deleteAllSessionsUseCase(userId)
                    .onSuccess {
                        sessionMessagesCache.clear()
                        _state.update {
                            it.copy(
                                    sessions = emptyList(),
                                    messages = emptyList(),
                                    activeSession = null
                            )
                        }
                        createNewSession()
                    }
                    .onFailure { e -> _state.update { it.copy(errorMessage = e.message) } }
        }
    }

    fun toggleSessionDrawer() {
        _state.update { it.copy(isSessionDrawerOpen = !it.isSessionDrawerOpen) }
    }

    // ---- Messages ----

    fun loadMessages(context: android.content.Context? = null, sessionId: String) {
        viewModelScope.launch {
            if (context != null) {
                val cachedDisk = LocalChatCache.getMessages(context, sessionId)
                if (cachedDisk.isNotEmpty()) {
                    sessionMessagesCache[sessionId] = cachedDisk
                    if (_state.value.activeSession?.id == sessionId) {
                        _state.update { it.copy(messages = cachedDisk, isLoadingMessages = false) }
                    }
                }
            }

            getSessionMessagesUseCase(sessionId)
                    .onSuccess { messages ->
                        sessionMessagesCache[sessionId] = messages
                        if (context != null) {
                            LocalChatCache.saveMessages(context, sessionId, messages)
                        }
                        if (_state.value.activeSession?.id == sessionId) {
                            _state.update {
                                it.copy(messages = messages, isLoadingMessages = false)
                            }
                        }
                    }
                    .onFailure { e ->
                        if (_state.value.activeSession?.id == sessionId) {
                            val fallback = if (context != null) LocalChatCache.getMessages(context, sessionId) else (_state.value.messages)
                            _state.update {
                                it.copy(messages = fallback, isLoadingMessages = false)
                            }
                        }
                    }
        }
    }

    private fun preloadAllSessionsMessages(context: android.content.Context? = null, sessions: List<ChatSession>) {
        viewModelScope.launch {
            sessions.forEach { session ->
                val id = session.id ?: return@forEach
                if (context != null) {
                    val cachedDisk = LocalChatCache.getMessages(context, id)
                    if (cachedDisk.isNotEmpty()) {
                        sessionMessagesCache[id] = cachedDisk
                        if (_state.value.activeSession?.id == id) {
                            _state.update { it.copy(messages = cachedDisk, isLoadingMessages = false) }
                        }
                    }
                }
                if (!sessionMessagesCache.containsKey(id)) {
                    getSessionMessagesUseCase(id).onSuccess { messages ->
                        sessionMessagesCache[id] = messages
                        if (context != null) {
                            LocalChatCache.saveMessages(context, id, messages)
                        }
                        if (_state.value.activeSession?.id == id) {
                            _state.update {
                                it.copy(messages = messages, isLoadingMessages = false)
                            }
                        }
                    }
                }
            }
        }
    }

    fun onInputTextChanged(text: String) {
        _state.update { it.copy(inputText = text) }
    }

    fun sendMessage() {
        val currentInput = _state.value.inputText.trim()
        val imageUrl = _state.value.pendingImageUrl
        val pendingImageUri = _state.value.pendingImageUri

        // Safeguard: if an image is selected but not yet uploaded, do not send
        if (pendingImageUri != null && imageUrl == null) return

        if (currentInput.isEmpty() && imageUrl == null) return
        if (_state.value.isSending) return

        _state.update {
            it.copy(
                    inputText = "",
                    isSending = true,
                    pendingImageUri = null,
                    pendingImageUrl = null
            )
        }

        viewModelScope.launch {
            val session = _state.value.activeSession ?: run {
                var createdSession: ChatSession? = null
                createSessionUseCase(userId)
                    .onSuccess { newSession ->
                        sessionMessagesCache[newSession.id!!] = emptyList()
                        _state.update { state ->
                            state.copy(
                                    sessions = listOf(newSession) + state.sessions,
                                    activeSession = newSession,
                                    messages = emptyList(),
                                    isLoadingSessions = false,
                                    isSessionDrawerOpen = false
                            )
                        }
                        createdSession = newSession
                    }
                    .onFailure { e ->
                        _state.update { it.copy(isSending = false, errorMessage = e.message) }
                    }
                createdSession
            } ?: return@launch

            // 1. Insert user message into DB
            val userMsg = ChatMessage(
                user_id = userId,
                session_id = session.id,
                role = "user",
                content = currentInput,
                image = imageUrl
            )
            insertMessageUseCase(userMsg)
                    .onSuccess { savedUserMsg ->
                        _state.update { state ->
                            val updated = state.messages + savedUserMsg
                            sessionMessagesCache[session.id!!] = updated
                            state.copy(messages = updated)
                        }

                        // If this is the first message in the session, update the session title
                        if (session.title == "New Chat" && _state.value.messages.size == 1) {
                            val rawTitle = if (currentInput.isNotEmpty()) currentInput else "Image Attachment"
                            val newTitle = if (rawTitle.length > 40) rawTitle.take(40) + "..." else rawTitle
                            renameSessionUseCase(session.id!!, newTitle).onSuccess {
                                val updatedSession = session.copy(title = newTitle)
                                _state.update { state ->
                                    val updatedSessions = state.sessions.map { s ->
                                        if (s.id == session.id) updatedSession else s
                                    }
                                    state.copy(
                                            sessions = updatedSessions,
                                            activeSession = updatedSession
                                    )
                                }
                            }
                        }

                        // 2. Show thinking indicator and call AI
                        _state.update { it.copy(isAiResponding = true) }

                        aiResponseJob = viewModelScope.launch {
                            val historyBeforeLast = _state.value.messages
                                .dropLast(1)
                                .filter { msg ->
                                    !(msg.role == "ai" && !msg.quiz_id.isNullOrBlank()) &&
                                    !(msg.role == "user" && msg.content == "Generate a practice quiz on this topic")
                                }
                            val context = UserContext(
                                education_level = userProfile?.education_level ?: "high_school",
                                explanation_style = _state.value.explanationStyle,
                                subjects = userProfile?.subjects ?: emptyList()
                            )
                            
                            requestAiReplyUseCase(
                                userId = userId,
                                sessionId = session.id!!,
                                currentInput = currentInput,
                                imageUrl = imageUrl,
                                historyBeforeLast = historyBeforeLast,
                                userContext = context,
                                isOnlyMessage = _state.value.messages.size <= 1,
                                savedUserMsgId = savedUserMsg.id,
                                sessionSubject = session.subject
                            ).onSuccess { aiReplyResult ->
                                val savedAiMsg = aiReplyResult.message
                                _state.update { state ->
                                    val updated = state.messages + savedAiMsg
                                    sessionMessagesCache[session.id!!] = updated
                                    val recognizedSubject = aiReplyResult.updatedSubject ?: session.subject
                                    val updatedSessions = state.sessions.map { s ->
                                        if (s.id == session.id) s.copy(subject = recognizedSubject) else s
                                    }
                                    val updatedActiveSession = state.activeSession?.let {
                                        if (it.id == session.id) it.copy(subject = recognizedSubject) else it
                                    }
                                    state.copy(
                                        messages = updated,
                                        sessions = updatedSessions,
                                        activeSession = updatedActiveSession,
                                        isSending = false,
                                        isAiResponding = false
                                    )
                                }
                            }.onFailure { e ->
                                if (e.message?.contains("Cannot assist") == true) {
                                    if (_state.value.messages.size <= 1) {
                                        _state.update { state ->
                                            val updatedSessions = state.sessions.filter { it.id != session.id }
                                            sessionMessagesCache.remove(session.id)
                                            state.copy(
                                                sessions = updatedSessions,
                                                activeSession = null,
                                                messages = emptyList(),
                                                isSending = false,
                                                isAiResponding = false,
                                                errorMessage = e.message
                                            )
                                        }
                                    } else {
                                        _state.update { state ->
                                            val filtered = state.messages.filter { it.id != savedUserMsg.id }
                                            sessionMessagesCache[session.id!!] = filtered
                                            state.copy(
                                                messages = filtered,
                                                isSending = false,
                                                isAiResponding = false,
                                                errorMessage = e.message
                                            )
                                        }
                                    }
                                } else {
                                    _state.update {
                                        it.copy(
                                            isSending = false,
                                            isAiResponding = false,
                                            errorMessage = com.eous.mentor.features.auth.friendlyAuthError(e)
                                        )
                                    }
                                }
                            }
                        }
                    }
                    .onFailure { e ->
                        _state.update { it.copy(isSending = false, errorMessage = e.message) }
                    }
        }
    }

    // ---- Stop Responding ----

    fun stopResponding() {
        aiResponseJob?.cancel()
        aiResponseJob = null
        _state.update { it.copy(isSending = false, isAiResponding = false) }
    }

    // ---- Bookmark ----

    fun toggleBookmark(message: ChatMessage, folder: String = "General") {
        val msgId = message.id ?: return
        val newBookmarked = !message.is_bookmarked

        // Optimistic UI update
        _state.update { state ->
            val updatedMessages = state.messages.map {
                if (it.id == msgId) it.copy(is_bookmarked = newBookmarked, bookmark_folder = if (newBookmarked) folder else null)
                else it
            }
            if (message.session_id != null) {
                sessionMessagesCache[message.session_id] = updatedMessages
            }
            state.copy(messages = updatedMessages)
        }

        viewModelScope.launch {
            toggleBookmarkUseCase(
                    messageId = msgId,
                    userId = userId,
                    isBookmarked = newBookmarked,
                    folder = folder
            ).onFailure {
                // Revert on failure
                _state.update { state ->
                    val revertedMessages = state.messages.map {
                        if (it.id == msgId) it.copy(is_bookmarked = !newBookmarked, bookmark_folder = if (!newBookmarked) folder else null)
                        else it
                    }
                    if (message.session_id != null) {
                        sessionMessagesCache[message.session_id] = revertedMessages
                    }
                    state.copy(messages = revertedMessages, errorMessage = it.message)
                }
            }
        }
    }

    // ---- Image Upload ----

    fun onImagePicked(uri: Uri, context: Context) {
        _state.update { it.copy(pendingImageUri = uri.toString()) }

        viewModelScope.launch {
            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                val bytes = inputStream?.readBytes() ?: throw Exception("Could not read image")
                inputStream.close()

                val fileName = "${UUID.randomUUID()}.jpg"
                uploadChatImageUseCase(userId, fileName, bytes)
                        .onSuccess { url -> _state.update { it.copy(pendingImageUrl = url) } }
                        .onFailure { e ->
                            _state.update {
                                it.copy(pendingImageUri = null, errorMessage = e.message)
                            }
                        }
            } catch (e: Throwable) {
                _state.update { it.copy(pendingImageUri = null, errorMessage = e.message) }
            }
        }
    }

    fun onExplanationStyleChanged(style: String) {
        _state.update { it.copy(explanationStyle = style, isStyleMenuExpanded = false) }
        userProfile = userProfile?.copy(explanation_style = style)
        viewModelScope.launch { updateExplanationStyleUseCase(userId, style) }
    }

    fun toggleStyleMenu() {
        _state.update { it.copy(isStyleMenuExpanded = !it.isStyleMenuExpanded) }
    }

    fun clearPendingImage() {
        _state.update { it.copy(pendingImageUri = null, pendingImageUrl = null) }
    }

    fun clearError() {
        _state.update { it.copy(errorMessage = null) }
    }
}
