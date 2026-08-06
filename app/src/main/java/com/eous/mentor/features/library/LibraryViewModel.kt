package com.eous.mentor.features.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eous.mentor.di.RepositoryProvider
import com.eous.mentor.domain.model.ChatMessage
import com.eous.mentor.domain.model.ChatSession
import com.eous.mentor.domain.usecase.library.GetLibraryContentUseCase
import com.eous.mentor.domain.usecase.bookmark.ToggleSessionBookmarkUseCase
import com.eous.mentor.di.UseCaseProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class LibraryViewModel(
    private val userId: String = "",
    private val getLibraryContentUseCase: GetLibraryContentUseCase = UseCaseProvider.getLibraryContent,
    private val toggleSessionBookmarkUseCase: ToggleSessionBookmarkUseCase = UseCaseProvider.toggleSessionBookmark
) : ViewModel() {
    private val _state = MutableStateFlow(LibraryState())
    val state: StateFlow<LibraryState> = _state.asStateFlow()

    init {
        if (userId.isNotEmpty()) loadLibraryData(userId)
    }

    fun loadLibraryData(userId: String) {
        _state.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            getLibraryContentUseCase(userId)
                .onSuccess { content ->
                    _state.update {
                        it.copy(
                            subjects = content.subjects,
                            bookmarkedMessages = content.bookmarkedMessages,
                            sessions = content.sessions,
                            practiceSubject = content.practiceSubject,
                            practiceQuestionCount = content.practiceQuestionCount,
                            hasPracticedToday = content.hasPracticedToday,
                            isLoading = false
                        )
                    }
                }
                .onFailure { error ->
                    _state.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = error.message
                        )
                    }
                }
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
        viewModelScope.launch {
            toggleSessionBookmarkUseCase(session, isCurrentlyBookmarked, bookmarkedMsg, userId)
                .onSuccess {
                    loadLibraryData(userId)
                }
                .onFailure { error ->
                    _state.update { it.copy(errorMessage = error.message) }
                }
        }
    }

    fun dismissSuggestion() {
        _state.update { it.copy(isSuggestionDismissed = true) }
    }

    fun clearError() {
        _state.update { it.copy(errorMessage = null) }
    }
}
