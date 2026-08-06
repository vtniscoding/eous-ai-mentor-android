package com.eous.mentor.features.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eous.mentor.di.UseCaseProvider
import com.eous.mentor.domain.usecase.profile.GetProfileUseCase
import com.eous.mentor.domain.usecase.profile.SaveOnboardingProfileUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ProfileViewModel(
    private val getProfileUseCase: GetProfileUseCase = UseCaseProvider.getProfile,
    private val saveOnboardingProfileUseCase: SaveOnboardingProfileUseCase = UseCaseProvider.saveOnboardingProfile
) : ViewModel() {
    private val _state = MutableStateFlow(ProfileState())
    val state: StateFlow<ProfileState> = _state.asStateFlow()

    fun loadProfile(userId: String) {
        if (userId.isEmpty()) return
        _state.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            getProfileUseCase(userId).onSuccess { profile ->
                _state.update {
                    it.copy(
                        profile = profile,
                        selectedLevel = profile?.education_level ?: it.selectedLevel,
                        selectedStyle = profile?.explanation_style ?: it.selectedStyle,
                        selectedSubjects = profile?.subjects ?: it.selectedSubjects
                    )
                }
            }
            _state.update { it.copy(isLoading = false) }
        }
    }

    fun setSelectedLevel(level: String) {
        _state.update { it.copy(selectedLevel = level) }
    }

    fun setSelectedStyle(style: String) {
        _state.update { it.copy(selectedStyle = style) }
    }

    fun setSelectedSubjects(subjects: List<String>) {
        _state.update { it.copy(selectedSubjects = subjects) }
    }

    fun saveProfile(userId: String, onSuccess: () -> Unit, onError: () -> Unit) {
        if (_state.value.isSaving) return
        _state.update { it.copy(isSaving = true) }
        viewModelScope.launch {
            saveOnboardingProfileUseCase(
                userId = userId,
                educationLevel = _state.value.selectedLevel,
                explanationStyle = _state.value.selectedStyle,
                subjects = _state.value.selectedSubjects
            ).onSuccess {
                _state.update { it.copy(isSaving = false) }
                onSuccess()
            }.onFailure {
                _state.update { it.copy(isSaving = false) }
                onError()
            }
        }
    }
}
