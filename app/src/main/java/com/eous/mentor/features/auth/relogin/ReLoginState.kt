package com.eous.mentor.features.auth.relogin

import com.eous.mentor.domain.model.SavedAccount

data class ReLoginState(
    val savedAccounts: List<SavedAccount> = emptyList(),
    val isManageMode: Boolean = false,
    val loggingInEmail: String? = null,
    val accountToRemove: SavedAccount? = null
)
