package com.eous.mentor.features.profile

import com.eous.mentor.domain.model.Profile

data class ProfileState(
    val profile: Profile? = null,
    val selectedLevel: String = "high_school",
    val selectedStyle: String = "detailed",
    val selectedSubjects: List<String> = emptyList(),
    val isLoading: Boolean = true,
    val isSaving: Boolean = false
)
