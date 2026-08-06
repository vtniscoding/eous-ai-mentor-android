package com.eous.mentor.features.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eous.mentor.di.UseCaseProvider
import com.eous.mentor.di.RepositoryProvider
import com.eous.mentor.domain.repository.SessionRepository
import com.eous.mentor.domain.usecase.session.GetRemoteSessionIdUseCase
import com.eous.mentor.domain.usecase.session.IsSessionTakenOverUseCase
import com.eous.mentor.domain.usecase.session.IssueLocalSessionUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SessionExpiredEvent(
    val email: String?,
    val avatarUrl: String?,
    val logoutSucceeded: Boolean
)

data class MainScreenState(
    val currentScreen: String = "dashboard",
    val chatInitialQuestion: String = "",
    val toolsInitialTab: String = "",
    val isMainScreenOpen: Boolean = false,
    val chatBackDestination: String? = null,
    val sessionExpiredEvent: SessionExpiredEvent? = null
)

class MainScreenViewModel(
    private val issueLocalSessionUseCase: IssueLocalSessionUseCase = UseCaseProvider.issueLocalSession,
    private val isSessionTakenOverUseCase: IsSessionTakenOverUseCase = UseCaseProvider.isSessionTakenOver,
    private val getRemoteSessionIdUseCase: GetRemoteSessionIdUseCase = UseCaseProvider.getRemoteSessionId,
    private val sessionRepository: SessionRepository = RepositoryProvider.sessionRepository
) : ViewModel() {
    private val _state = MutableStateFlow(MainScreenState())
    val state: StateFlow<MainScreenState> = _state.asStateFlow()

    private var sessionPollingJob: kotlinx.coroutines.Job? = null

    fun navigateTo(
        screen: String,
        chatQuestion: String = "",
        toolsTab: String = "",
        chatBackDest: String? = null
    ) {
        _state.update {
            it.copy(
                currentScreen = screen,
                chatInitialQuestion = chatQuestion,
                toolsInitialTab = toolsTab,
                chatBackDestination = chatBackDest,
                isMainScreenOpen = false
            )
        }
    }

    fun setMainScreenOpen(open: Boolean) {
        _state.update { it.copy(isMainScreenOpen = open) }
    }

    fun issueNewSession(context: android.content.Context, userId: String) {
        viewModelScope.launch {
            issueLocalSessionUseCase(context, userId)
        }
    }

    fun checkSessionOnProfileLoaded(
        context: android.content.Context,
        remoteSessionId: String?,
        email: String?,
        avatarUrl: String?,
        userId: String,
        onLogout: (onSuccess: () -> Unit, onError: () -> Unit) -> Unit
    ) {
        if (remoteSessionId.isNullOrEmpty()) {
            issueNewSession(context, userId)
        } else if (isSessionTakenOverUseCase(context, remoteSessionId, treatMissingLocalAsTakenOver = true)) {
            val currentEmail = email ?: sessionRepository.getCurrentUserEmail()
            sessionRepository.clearLocalSessionId(context)
            onLogout(
                {
                    _state.update {
                        it.copy(
                            sessionExpiredEvent = SessionExpiredEvent(
                                email = currentEmail,
                                avatarUrl = avatarUrl,
                                logoutSucceeded = true
                            )
                        )
                    }
                },
                {
                    _state.update {
                        it.copy(
                            sessionExpiredEvent = SessionExpiredEvent(
                                email = currentEmail,
                                avatarUrl = avatarUrl,
                                logoutSucceeded = false
                            )
                        )
                    }
                }
            )
        }
    }

    fun startSessionPolling(
        context: android.content.Context,
        userId: String,
        currentEmail: () -> String?,
        currentAvatarUrl: () -> String?,
        onLogout: (onSuccess: () -> Unit, onError: () -> Unit) -> Unit
    ) {
        sessionPollingJob?.cancel()
        sessionPollingJob = viewModelScope.launch {
            while (true) {
                kotlinx.coroutines.delay(8000L)
                try {
                    val remoteSessionId = getRemoteSessionIdUseCase(userId).getOrNull()
                    if (isSessionTakenOverUseCase(context, remoteSessionId, treatMissingLocalAsTakenOver = false)) {
                        val emailVal = currentEmail() ?: sessionRepository.getCurrentUserEmail()
                        val avatarVal = currentAvatarUrl()
                        sessionRepository.clearLocalSessionId(context)
                        onLogout(
                            {
                                _state.update {
                                    it.copy(
                                        sessionExpiredEvent = SessionExpiredEvent(
                                            email = emailVal,
                                            avatarUrl = avatarVal,
                                            logoutSucceeded = true
                                        )
                                    )
                                }
                            }
                        ,
                            {
                                _state.update {
                                    it.copy(
                                        sessionExpiredEvent = SessionExpiredEvent(
                                            email = emailVal,
                                            avatarUrl = avatarVal,
                                            logoutSucceeded = false
                                        )
                                    )
                                }
                            }
                        )
                        break
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    fun consumeSessionExpiredEvent() {
        _state.update { it.copy(sessionExpiredEvent = null) }
    }
}
