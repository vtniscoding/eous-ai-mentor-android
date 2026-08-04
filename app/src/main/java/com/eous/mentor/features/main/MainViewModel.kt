package com.eous.mentor.features.main

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class MainScreenState(
        val currentScreen: String = "dashboard",
        val chatInitialQuestion: String = "",
        val toolsInitialTab: String = "",
        val isMainScreenOpen: Boolean = false,
        val chatBackDestination: String? = null
)

class MainScreenViewModel : ViewModel() {
    private val _state = MutableStateFlow(MainScreenState())
    val state: StateFlow<MainScreenState> = _state.asStateFlow()

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
}
