package com.eous.mentor.features.chat

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eous.mentor.di.RepositoryProvider
import com.eous.mentor.domain.model.*
import com.eous.mentor.domain.repository.ChatRepository
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
        private val chatRepository: ChatRepository = RepositoryProvider.chatRepository,
        private val userRepository: com.eous.mentor.domain.repository.UserRepository =
                RepositoryProvider.userRepository
) : ViewModel() {
    private val _state = MutableStateFlow(ChatState())
    val state: StateFlow<ChatState> = _state.asStateFlow()

    private var aiResponseJob: Job? = null
    private val sessionMessagesCache =
            java.util.concurrent.ConcurrentHashMap<String, List<ChatMessage>>()
    private var userProfile: Profile? = null

    init {
        loadSessions(initialQuestion)
        loadUserProfile()
    }

    private fun loadUserProfile() {
        viewModelScope.launch {
            userRepository.getProfile(userId).onSuccess { profile ->
                userProfile = profile
                profile?.explanation_style?.let { style ->
                    _state.update { it.copy(explanationStyle = style) }
                }
            }
        }
    }

    // ---- Session Management ----

    fun loadSessions(initialQuestion: String = "") {
        viewModelScope.launch {
            val startTime = System.currentTimeMillis()
            _state.update { it.copy(isLoadingSessions = true) }
            chatRepository
                    .getSessions(userId)
                    .onSuccess { sessions ->
                        val elapsedTime = System.currentTimeMillis() - startTime
                        val minDurationMs = 2600L
                        if (elapsedTime < minDurationMs) {
                            kotlinx.coroutines.delay(minDurationMs - elapsedTime)
                        }
                        _state.update {
                            it.copy(
                                    sessions = sessions,
                                    activeSession = null,
                                    messages = emptyList(),
                                    isLoadingSessions = false
                            )
                        }

                        if (sessions.isNotEmpty()) {
                            preloadAllSessionsMessages(sessions)
                        }

                        if (initialQuestion.isNotEmpty()) {
                            _state.update { it.copy(inputText = initialQuestion) }
                        }
                    }
                    .onFailure { e ->
                        _state.update {
                            it.copy(isLoadingSessions = false, errorMessage = e.message)
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
            chatRepository
                    .createSession(userId)
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

    fun selectSession(session: ChatSession) {
        if (session.id == _state.value.activeSession?.id) {
            _state.update { it.copy(isSessionDrawerOpen = false) }
            return
        }
        val cachedMessages = sessionMessagesCache[session.id]
        if (cachedMessages != null) {
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
        loadMessages(session.id!!)
    }

    fun deleteSession(sessionId: String) {
        viewModelScope.launch {
            chatRepository
                    .deleteSession(sessionId)
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
            chatRepository
                    .updateSessionTitle(sessionId, newTitle)
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
            chatRepository
                    .deleteAllSessions(userId)
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

    private fun loadMessages(sessionId: String) {
        viewModelScope.launch {
            chatRepository
                    .getMessages(sessionId)
                    .onSuccess { messages ->
                        sessionMessagesCache[sessionId] = messages
                        if (_state.value.activeSession?.id == sessionId) {
                            _state.update {
                                it.copy(messages = messages, isLoadingMessages = false)
                            }
                        }
                    }
                    .onFailure { e ->
                        if (_state.value.activeSession?.id == sessionId) {
                            _state.update {
                                it.copy(isLoadingMessages = false, errorMessage = e.message)
                            }
                        }
                    }
        }
    }

    private fun preloadAllSessionsMessages(sessions: List<ChatSession>) {
        viewModelScope.launch {
            sessions.forEach { session ->
                val id = session.id ?: return@forEach
                if (!sessionMessagesCache.containsKey(id)) {
                    chatRepository.getMessages(id).onSuccess { messages ->
                        sessionMessagesCache[id] = messages
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
                chatRepository.createSession(userId)
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
            val userMsg =
                    ChatMessage(
                            user_id = userId,
                            session_id = session.id,
                            role = "user",
                            content = currentInput,
                            image = imageUrl
                    )
            chatRepository
                    .insertMessage(userMsg)
                    .onSuccess { savedUserMsg ->
                        _state.update { state ->
                            val updated = state.messages + savedUserMsg
                            sessionMessagesCache[session.id!!] = updated
                            state.copy(messages = updated)
                        }

                        // If this is the first message in the session, update the session title
                        if (session.title == "New Chat" && _state.value.messages.size == 1) {
                            val rawTitle =
                                    if (currentInput.isNotEmpty()) currentInput
                                    else "Image Attachment"
                            val newTitle =
                                    if (rawTitle.length > 40) rawTitle.take(40) + "..."
                                    else rawTitle
                            chatRepository.updateSessionTitle(session.id!!, newTitle).onSuccess {
                                val updatedSession = session.copy(title = newTitle)
                                _state.update { state ->
                                    val updatedSessions =
                                            state.sessions.map { s ->
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

                        aiResponseJob =
                                viewModelScope.launch {
                                    val historyBeforeLast = _state.value.messages.dropLast(1)
                                    val context =
                                            UserContext(
                                                    education_level =
                                                            userProfile?.education_level
                                                                    ?: "high_school",
                                                    explanation_style =
                                                            _state.value.explanationStyle,
                                                    subjects =
                                                            userProfile?.subjects ?: emptyList()
                                            )
                                    chatRepository
                                            .getAiResponse(
                                                    message = currentInput,
                                                    history = historyBeforeLast,
                                                    imageUrl = imageUrl,
                                                    userContext = context
                                            )
                                            .onSuccess { aiResponse ->
                                                if (!aiResponse.error.isNullOrBlank()) {
                                                    _state.update {
                                                        it.copy(
                                                                isSending = false,
                                                                isAiResponding = false,
                                                                errorMessage = aiResponse.error
                                                        )
                                                    }
                                                    return@onSuccess
                                                }

                                                // Detect Refusal
                                                val parsed = AnswerParser.parse(aiResponse.reply, aiResponse.subject)
                                                if (parsed.type == AnswerType.REFUSAL) {
                                                    val isOnlyMessage = _state.value.messages.size <= 1
                                                    if (isOnlyMessage) {
                                                        chatRepository.deleteSession(session.id!!)
                                                        _state.update { state ->
                                                            val updatedSessions = state.sessions.filter { it.id != session.id }
                                                            sessionMessagesCache.remove(session.id)
                                                            state.copy(
                                                                    sessions = updatedSessions,
                                                                    activeSession = null,
                                                                    messages = emptyList(),
                                                                    isSending = false,
                                                                    isAiResponding = false,
                                                                    errorMessage = "Cannot assist with this request. I can only assist with academic and study-related queries."
                                                            )
                                                        }
                                                    } else {
                                                        savedUserMsg.id?.let { msgId ->
                                                            chatRepository.deleteMessage(msgId)
                                                        }
                                                        _state.update { state ->
                                                            val filtered = state.messages.filter { it.id != savedUserMsg.id }
                                                            sessionMessagesCache[session.id!!] = filtered
                                                            state.copy(
                                                                    messages = filtered,
                                                                    isSending = false,
                                                                    isAiResponding = false,
                                                                    errorMessage = "Cannot assist with this request. I can only assist with academic and study-related queries."
                                                            )
                                                        }
                                                    }
                                                    return@onSuccess
                                                }

                                                // 3. Check if AI generated a Quiz
                                                var createdQuizId: String? = null
                                                val q = aiResponse.quiz
                                                if (q != null && q.questions.isNotEmpty()) {
                                                    val topicName =
                                                            aiResponse.subject
                                                                    ?: q.topic.ifBlank { "General" }
                                                    val quizTitle =
                                                            q.title.ifBlank { "Bài tập $topicName" }
                                                    val createRes =
                                                            userRepository.createQuiz(
                                                                    userId = userId,
                                                                    topic = topicName,
                                                                    title = quizTitle,
                                                                    totalQuestions =
                                                                            q.questions.size,
                                                                    questions = q.questions,
                                                                    difficulty = q.difficulty
                                                            )
                                                    if (createRes.isSuccess) {
                                                        createdQuizId = createRes.getOrNull()?.id
                                                    }
                                                }

                                                // 4. Insert AI response into DB
                                                val replyContent = if (createdQuizId != null) {
                                                    "I have designed the questions above to help you review your knowledge. Don't hesitate to try your best, as every mistake is an opportunity to learn even more deeply. Happy learning, and keep up your eager spirit!"
                                                } else {
                                                    aiResponse.reply
                                                }
                                                val aiMsg =
                                                        ChatMessage(
                                                                user_id = userId,
                                                                session_id = session.id,
                                                                role = "ai",
                                                                content = replyContent,
                                                                subject = aiResponse.subject,
                                                                quiz_id = createdQuizId
                                                        )
                                                chatRepository
                                                        .insertMessage(aiMsg)
                                                        .onSuccess { savedAiMsg ->
                                                            val recognizedSubject = aiResponse.subject ?: "General"
                                                            if (session.subject != recognizedSubject) {
                                                                chatRepository.updateSessionSubject(session.id!!, recognizedSubject)
                                                            }
                                                            _state.update { state ->
                                                                val updated =
                                                                        state.messages + savedAiMsg
                                                                sessionMessagesCache[session.id!!] =
                                                                        updated
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
                                                        }
                                                        .onFailure { e ->
                                                            _state.update {
                                                                it.copy(
                                                                        isSending = false,
                                                                        isAiResponding = false,
                                                                        errorMessage = e.message
                                                                )
                                                            }
                                                        }
                                            }
                                            .onFailure { e ->
                                                _state.update {
                                                    it.copy(
                                                            isSending = false,
                                                            isAiResponding = false,
                                                            errorMessage = e.message
                                                    )
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
            val updatedMessages =
                    state.messages.map {
                        if (it.id == msgId)
                                it.copy(
                                        is_bookmarked = newBookmarked,
                                        bookmark_folder = if (newBookmarked) folder else null
                                )
                        else it
                    }
            if (message.session_id != null) {
                sessionMessagesCache[message.session_id] = updatedMessages
            }
            state.copy(messages = updatedMessages)
        }

        viewModelScope.launch {
            chatRepository.toggleBookmark(
                            messageId = msgId,
                            userId = userId,
                            isBookmarked = newBookmarked,
                            folder = folder
                    )
                    .onFailure {
                        // Revert on failure
                        _state.update { state ->
                            val revertedMessages =
                                    state.messages.map {
                                        if (it.id == msgId)
                                                it.copy(
                                                        is_bookmarked = !newBookmarked,
                                                        bookmark_folder =
                                                                if (!newBookmarked) folder else null
                                                )
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
                chatRepository
                        .uploadImage(userId, fileName, bytes)
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
        viewModelScope.launch { userRepository.updateExplanationStyle(userId, style) }
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
