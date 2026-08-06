package com.eous.mentor.features.auth.relogin

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eous.mentor.di.RepositoryProvider
import com.eous.mentor.di.UseCaseProvider
import com.eous.mentor.domain.model.SavedAccount
import com.eous.mentor.domain.repository.SessionRepository
import com.eous.mentor.domain.usecase.auth.LoginUseCase
import com.eous.mentor.domain.usecase.session.IssueLocalSessionUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ReLoginViewModel(
    private val loginUseCase: LoginUseCase = UseCaseProvider.login,
    private val issueLocalSessionUseCase: IssueLocalSessionUseCase = UseCaseProvider.issueLocalSession,
    private val sessionRepository: SessionRepository = RepositoryProvider.sessionRepository
) : ViewModel() {
    private val _state = MutableStateFlow(ReLoginState())
    val state: StateFlow<ReLoginState> = _state.asStateFlow()

    fun updateSavedAccounts(accounts: List<SavedAccount>) {
        _state.update { it.copy(savedAccounts = accounts) }
    }

    fun setManageMode(manage: Boolean) {
        _state.update { it.copy(isManageMode = manage) }
    }

    fun setAccountToRemove(account: SavedAccount?) {
        _state.update { it.copy(accountToRemove = account) }
    }

    fun loginAccount(context: Context, account: SavedAccount, onResult: (Result<Unit>) -> Unit) {
        if (_state.value.loggingInEmail != null) return
        _state.update { it.copy(loggingInEmail = account.email) }
        viewModelScope.launch {
            val res = loginUseCase(account.email, account.password)
            if (res.isSuccess) {
                sessionRepository.getCurrentUserId()?.takeIf { it.isNotEmpty() }?.let { uid ->
                    issueLocalSessionUseCase(context, uid)
                }
                _state.update { it.copy(loggingInEmail = null) }
                onResult(Result.success(Unit))
            } else {
                _state.update { it.copy(loggingInEmail = null) }
                onResult(Result.failure(res.exceptionOrNull() ?: Exception("Unknown error")))
            }
        }
    }
}
