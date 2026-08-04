package com.eous.mentor.features.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eous.mentor.di.RepositoryProvider
import com.eous.mentor.domain.model.ChatMessage
import com.eous.mentor.domain.model.ChatSession
import com.eous.mentor.domain.repository.ChatRepository
import com.eous.mentor.domain.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

class LibraryViewModel(
    private val userId: String = "",
    private val chatRepository: ChatRepository = RepositoryProvider.chatRepository,
    private val userRepository: UserRepository = RepositoryProvider.userRepository
) : ViewModel() {
    private val _state = MutableStateFlow(LibraryState())
    val state: StateFlow<LibraryState> = _state.asStateFlow()

    init {
        if (userId.isNotEmpty()) loadLibraryData(userId)
    }

    fun loadLibraryData(userId: String) {
        _state.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            // 1. Fetch bookmarked messages
            val bookmarksResult = chatRepository.getBookmarkedMessages(userId)
            val bookmarks = bookmarksResult.getOrDefault(emptyList())

            // 2. Fetch history chat sessions
            chatRepository.getSessions(userId)
                .onSuccess { sessionsList ->
                    // Extract unique subjects from user's sessions to dynamically update the filters
                    val uniqueSubjects = sessionsList
                        .map { it.subject }
                        .filter { it.isNotBlank() }
                        .distinct()

                    // Compute dynamic weekly suggestion stats based on active subjects (questions >= 3)
                    val now = System.currentTimeMillis()
                    val oneWeekAgo = now - 7 * 24 * 60 * 60 * 1000L
                    val weeklySessions = sessionsList.filter { session ->
                        val time = parseCreatedAt(session.created_at) ?: 0L
                        time >= oneWeekAgo
                    }
                    val subjectCounts = weeklySessions.groupBy { it.subject }.mapValues { it.value.size }
                    val mostFrequentSubject = subjectCounts.filterKeys { it.isNotBlank() }.maxByOrNull { it.value }
                    val practiceSubject = mostFrequentSubject?.key ?: "Math"
                    val practiceQuestionCount = mostFrequentSubject?.value ?: 0

                    _state.update {
                        it.copy(
                            subjects = uniqueSubjects,
                            bookmarkedMessages = bookmarks,
                            sessions = sessionsList,
                            practiceSubject = practiceSubject,
                            practiceQuestionCount = practiceQuestionCount,
                            isLoading = false
                        )
                    }
                }
                .onFailure { error ->
                    _state.update {
                        it.copy(
                            bookmarkedMessages = bookmarks,
                            isLoading = false,
                            errorMessage = error.message
                        )
                    }
                }
        }
    }

    private fun parseCreatedAt(createdAt: String?): Long? {
        if (createdAt.isNullOrEmpty()) return null
        return try {
            val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US)
            sdf.timeZone = TimeZone.getTimeZone("UTC")
            sdf.parse(createdAt.take(19))?.time
        } catch (e: Exception) {
            null
        }
    }

    fun setSearchQuery(query: String) {
        _state.update { it.copy(searchQuery = query) }
    }

    fun selectFilter(filter: String) {
        _state.update { it.copy(selectedFilter = filter) }
    }

    fun toggleBookmarkFromLibrary(
        session: ChatSession,
        isCurrentlyBookmarked: Boolean,
        bookmarkedMsg: ChatMessage?,
        userId: String
    ) {
        val sessionId = session.id ?: return
        viewModelScope.launch {
            if (isCurrentlyBookmarked) {
                val msgId = bookmarkedMsg?.id ?: return@launch
                chatRepository.toggleBookmark(
                    messageId = msgId,
                    userId = userId,
                    isBookmarked = false
                ).onSuccess {
                    loadLibraryData(userId)
                }.onFailure { error ->
                    _state.update { it.copy(errorMessage = error.message) }
                }
            } else {
                // Fetch messages for session to find the last AI response
                chatRepository.getMessages(sessionId)
                    .onSuccess { messages ->
                        val latestAiMsg = messages.lastOrNull { it.role == "ai" }
                        if (latestAiMsg != null) {
                            val msgId = latestAiMsg.id ?: return@onSuccess
                            chatRepository.toggleBookmark(
                                messageId = msgId,
                                userId = userId,
                                isBookmarked = true,
                                folder = session.subject
                            ).onSuccess {
                                loadLibraryData(userId)
                            }.onFailure { error ->
                                _state.update { it.copy(errorMessage = error.message) }
                            }
                        } else {
                            _state.update { it.copy(errorMessage = "No AI response found in this session to bookmark.") }
                        }
                    }
                    .onFailure { error ->
                        _state.update { it.copy(errorMessage = error.message) }
                    }
            }
        }
    }

    fun clearError() {
        _state.update { it.copy(errorMessage = null) }
    }
}
