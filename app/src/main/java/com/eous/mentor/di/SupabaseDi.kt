package com.eous.mentor.di

import com.eous.mentor.BuildConfig
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.functions.Functions
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.storage.Storage
import io.github.jan.supabase.compose.auth.ComposeAuth
import io.github.jan.supabase.compose.auth.googleNativeLogin
import kotlin.time.Duration.Companion.seconds

val supabase = createSupabaseClient(
    supabaseUrl = BuildConfig.SUPABASE_URL,
    supabaseKey = BuildConfig.SUPABASE_KEY
) {
    requestTimeout = 60.seconds
    install(Postgrest)
    install(Auth)
    install(Storage)
    install(Functions)
    install(ComposeAuth) {
        googleNativeLogin(
            serverClientId = BuildConfig.GOOGLE_WEB_CLIENT_ID
        )
    }
}

