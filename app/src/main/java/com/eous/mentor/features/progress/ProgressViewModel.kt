package com.eous.mentor.features.progress

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eous.mentor.di.RepositoryProvider
import com.eous.mentor.di.UseCaseProvider
import com.eous.mentor.domain.model.DashboardStats
import com.eous.mentor.domain.usecase.progress.GetProgressStatsUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ProgressState(
    val stats: DashboardStats? = null,
    val isLoading: Boolean = false
)

class ProgressViewModel(
    private val userId: String,
    private val getProgressStatsUseCase: GetProgressStatsUseCase = UseCaseProvider.getProgressStats
) : ViewModel() {

    private val _state = MutableStateFlow(ProgressState())
    val state: StateFlow<ProgressState> = _state.asStateFlow()

    init {
        loadStats()
    }

    fun loadStats() {
        if (userId.isEmpty()) return
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            getProgressStatsUseCase(userId)
                .onSuccess { fetchedStats ->
                    _state.update { it.copy(stats = fetchedStats, isLoading = false) }
                }
                .onFailure { e ->
                    e.printStackTrace()
                    _state.update { it.copy(isLoading = false) }
                }
        }
    }
}
