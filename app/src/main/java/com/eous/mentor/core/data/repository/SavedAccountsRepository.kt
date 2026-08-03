package com.eous.mentor.core.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.eous.mentor.domain.model.SavedAccount
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

object SavedAccountsRepository {
    private const val PREFS_NAME = "eous_saved_accounts"
    private const val KEY_SAVED_ACCOUNTS = "saved_accounts_json"

    private val json = Json { ignoreUnknownKeys = true }

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun getSavedAccounts(context: Context): List<SavedAccount> {
        val jsonStr = getPrefs(context).getString(KEY_SAVED_ACCOUNTS, null) ?: return emptyList()
        return try {
            json.decodeFromString<List<SavedAccount>>(jsonStr)
        } catch (e: Throwable) {
            emptyList()
        }
    }

    /** Read → transform → write pattern shared by all mutating operations. Returns the updated list. */
    private fun updateAccounts(
            context: Context,
            transform: MutableList<SavedAccount>.() -> Unit
    ): List<SavedAccount> {
        val updated = getSavedAccounts(context).toMutableList().also(transform)
        getPrefs(context).edit().putString(KEY_SAVED_ACCOUNTS, json.encodeToString(updated)).apply()
        return updated
    }

    fun saveAccount(context: Context, account: SavedAccount) {
        if (account.email.isBlank()) return
        updateAccounts(context) {
            val existing = find { it.email.equals(account.email, ignoreCase = true) }
            val finalPassword = if (account.password.isNotEmpty()) account.password else (existing?.password ?: "")
            val finalAvatar = if (!account.avatarUrl.isNullOrEmpty()) account.avatarUrl else existing?.avatarUrl
            val finalDisplayName = if (account.displayName.isNotEmpty()) account.displayName else (existing?.displayName ?: "")

            removeAll { it.email.equals(account.email, ignoreCase = true) }
            add(
                0,
                SavedAccount(
                    email = account.email,
                    password = finalPassword,
                    displayName = finalDisplayName,
                    avatarUrl = finalAvatar
                )
            )
        }
    }

    /** Removes the account and returns the updated list so callers avoid a redundant re-read. */
    fun removeAccount(context: Context, email: String): List<SavedAccount> =
            updateAccounts(context) {
                removeAll { it.email.equals(email, ignoreCase = true) }
            }

    fun clearAll(context: Context) {
        getPrefs(context).edit().remove(KEY_SAVED_ACCOUNTS).apply()
    }
}
