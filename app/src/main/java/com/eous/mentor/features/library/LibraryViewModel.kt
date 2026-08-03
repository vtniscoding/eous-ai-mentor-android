package com.eous.mentor.features.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eous.mentor.di.RepositoryProvider
import com.eous.mentor.domain.model.ChatMessage
import com.eous.mentor.domain.repository.ChatRepository
import com.eous.mentor.domain.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

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
            // 1. Fetch Profile for user subjects
            val profileResult = userRepository.getProfile(userId)
            val userSubjects = profileResult.getOrNull()?.subjects ?: emptyList()
            
            // 2. Fetch bookmarked messages
            chatRepository.getBookmarkedMessages(userId)
                .onSuccess { msgs ->
                    _state.update {
                        it.copy(
                            subjects = userSubjects,
                            bookmarkedMessages = msgs,
                            isLoading = false
                        )
                    }
                }
                .onFailure { error ->
                    _state.update {
                        it.copy(
                            subjects = userSubjects,
                            isLoading = false,
                            errorMessage = error.message
                        )
                    }
                }
        }
    }

    fun selectSubject(subject: String?) {
        _state.update { it.copy(selectedSubject = subject) }
    }

    fun addSubject(userId: String, name: String) {
        val trimmedName = name.trim()
        if (trimmedName.isEmpty()) return
        
        val currentSubjects = _state.value.subjects
        if (currentSubjects.any { it.equals(trimmedName, ignoreCase = true) }) return
        
        val updated = currentSubjects + trimmedName
        viewModelScope.launch {
            userRepository.updateSubjects(userId, updated)
                .onSuccess {
                    _state.update { it.copy(subjects = updated) }
                }
                .onFailure { error ->
                    _state.update { it.copy(errorMessage = error.message) }
                }
        }
    }

    fun renameSubject(userId: String, oldName: String, newName: String) {
        val trimmedNew = newName.trim()
        if (trimmedNew.isEmpty() || trimmedNew == oldName) return
        
        val currentSubjects = _state.value.subjects
        if (currentSubjects.any { it.equals(trimmedNew, ignoreCase = true) && !it.equals(oldName, ignoreCase = true) }) return
        
        val updated = currentSubjects.map { if (it == oldName) trimmedNew else it }
        viewModelScope.launch {
            userRepository.updateSubjects(userId, updated)
                .onSuccess {
                    val newSelected = if (_state.value.selectedSubject == oldName) trimmedNew else _state.value.selectedSubject
                    _state.update { it.copy(subjects = updated, selectedSubject = newSelected) }
                }
                .onFailure { error ->
                    _state.update { it.copy(errorMessage = error.message) }
                }
        }
    }

    fun removeSubject(userId: String, name: String) {
        val currentSubjects = _state.value.subjects
        val updated = currentSubjects.filter { it != name }
        viewModelScope.launch {
            userRepository.updateSubjects(userId, updated)
                .onSuccess {
                    val newSelected = if (_state.value.selectedSubject == name) null else _state.value.selectedSubject
                    _state.update { it.copy(subjects = updated, selectedSubject = newSelected) }
                }
                .onFailure { error ->
                    _state.update { it.copy(errorMessage = error.message) }
                }
        }
    }


    fun removeBookmark(message: ChatMessage, userId: String) {
        val msgId = message.id ?: return
        viewModelScope.launch {
            chatRepository.toggleBookmark(
                messageId = msgId,
                userId = userId,
                isBookmarked = false
            ).onSuccess {
                _state.update { state ->
                    val updatedMsgs = state.bookmarkedMessages.filter { it.id != msgId }
                    state.copy(bookmarkedMessages = updatedMsgs)
                }
            }.onFailure { error ->
                _state.update { it.copy(errorMessage = error.message) }
            }
        }
    }

    fun clearError() {
        _state.update { it.copy(errorMessage = null) }
    }
}
