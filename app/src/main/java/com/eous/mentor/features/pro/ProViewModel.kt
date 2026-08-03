package com.eous.mentor.features.pro

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class ProViewModel : ViewModel() {
    private val _state = MutableStateFlow(ProState())
    val state: StateFlow<ProState> = _state.asStateFlow()
}
