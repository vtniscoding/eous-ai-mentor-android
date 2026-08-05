package com.eous.mentor.core.navigation

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.NavController
import androidx.lifecycle.Lifecycle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.eous.mentor.core.data.repository.SavedAccountsRepository
import com.eous.mentor.core.ui.theme.*
import com.eous.mentor.di.RepositoryProvider
import com.eous.mentor.domain.repository.SessionState
import com.eous.mentor.features.auth.intro.AuthIntroScreen
import com.eous.mentor.features.auth.login.LoginFormScreen
import com.eous.mentor.features.auth.register.RegisterFormScreen
import com.eous.mentor.features.auth.relogin.ReLoginScreen
import com.eous.mentor.features.auth.splash.SplashScreen
import com.eous.mentor.features.home.HomeViewModel
import com.eous.mentor.features.chat.ChatViewModel
import com.eous.mentor.features.main.MainScreen
import com.eous.mentor.features.auth.mfa.MfaVerifyScreen
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.mfa.AuthenticatorAssuranceLevel

// --- Navigation Host ---
@Composable
fun AuthRouter() {
    val sessionRepository = RepositoryProvider.sessionRepository
    val navController = rememberNavController()
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp
    val isTablet = screenWidth >= 600

    var isInitialized by remember { mutableStateOf(false) }
    var targetDest by remember { mutableStateOf("intro") }
    var activeUserId by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        sessionRepository.observeSessionStatus().collect { status ->
            if (status != SessionState.INITIALIZING) {
                val currentUid = sessionRepository.getCurrentUserId() ?: ""
                activeUserId = currentUid
                val savedAccounts = withContext(Dispatchers.IO) { SavedAccountsRepository.getSavedAccounts(context) }
                if (status == SessionState.AUTHENTICATED) {
                    try {
                        val (current, next) = com.eous.mentor.di.supabase.auth.mfa.getAuthenticatorAssuranceLevel()
                        if (current == AuthenticatorAssuranceLevel.AAL1 &&
                            next == AuthenticatorAssuranceLevel.AAL2) {
                            targetDest = "mfa_verify"
                        } else {
                            targetDest = "dashboard"
                        }
                    } catch (e: Throwable) {
                        targetDest = "dashboard"
                    }
                } else {
                    targetDest = if (savedAccounts.isNotEmpty()) "relogin" else if (isTablet) "login" else "intro"
                }
                isInitialized = true
            }
        }
    }

    val homeViewModel =
            remember(activeUserId) {
                if (activeUserId.isNotEmpty()) HomeViewModel(activeUserId) else null
            }
    val chatViewModel =
            remember(activeUserId) {
                if (activeUserId.isNotEmpty()) ChatViewModel(userId = activeUserId) else null
            }

    val homeState = homeViewModel?.state?.collectAsState()?.value
    val chatState = chatViewModel?.state?.collectAsState()?.value
    val isTargetReady =
            if (targetDest == "dashboard" || targetDest == "mfa_verify") {
                isInitialized && (targetDest == "mfa_verify" || (homeState != null && !homeState.isLoading && chatState != null && !chatState.isLoadingSessions))
            } else {
                isInitialized
            }

    // Auth navigation host
        NavHost(
                navController = navController,
                startDestination = "splash",
                enterTransition = { EnterTransition.None },
                exitTransition = { ExitTransition.None },
                popEnterTransition = { EnterTransition.None },
                popExitTransition = { ExitTransition.None }
        ) {
            composable(
                route = "splash",
                exitTransition = { fadeOut(animationSpec = tween(500)) }
            ) {
                SplashScreen(
                    navController = navController,
                    targetDestination = targetDest,
                    isInitialized = isTargetReady
                )
            }
            composable(
                route = "intro",
                    exitTransition = {
                        if (targetState.destination.route == "login") {
                            slideOutVertically(
                                targetOffsetY = { -it },
                                animationSpec = tween(450, easing = FastOutSlowInEasing)
                            ) + fadeOut(animationSpec = tween(450))
                        } else if (targetState.destination.route == "register") {
                            slideOutVertically(
                                targetOffsetY = { it },
                                animationSpec = tween(450, easing = FastOutSlowInEasing)
                            ) + fadeOut(animationSpec = tween(450))
                        } else {
                            fadeOut(animationSpec = tween(450))
                        }
                    },
                    popEnterTransition = {
                        if (initialState.destination.route == "login") {
                            slideInVertically(
                                initialOffsetY = { -it },
                                animationSpec = tween(450, easing = FastOutSlowInEasing)
                            ) + fadeIn(animationSpec = tween(450))
                        } else if (initialState.destination.route == "register") {
                            slideInVertically(
                                initialOffsetY = { it },
                                animationSpec = tween(450, easing = FastOutSlowInEasing)
                            ) + fadeIn(animationSpec = tween(450))
                        } else {
                            fadeIn(animationSpec = tween(450))
                        }
                    }
                ) {
                    AuthIntroScreen(navController = navController)
                }
                composable(
                    route = "login",
                    enterTransition = {
                        slideInVertically(
                            initialOffsetY = { it },
                            animationSpec = tween(450, easing = FastOutSlowInEasing)
                        ) + fadeIn(animationSpec = tween(450))
                    },
                    exitTransition = {
                        fadeOut(animationSpec = tween(450))
                    },
                    popEnterTransition = {
                        fadeIn(animationSpec = tween(450))
                    },
                    popExitTransition = {
                        slideOutVertically(
                            targetOffsetY = { it },
                            animationSpec = tween(450, easing = FastOutSlowInEasing)
                        ) + fadeOut(animationSpec = tween(450))
                    }
                ) {
                    LoginFormScreen(navController = navController, isTablet = isTablet)
                }
                composable(
                    route = "register",
                    enterTransition = {
                        slideInVertically(
                            initialOffsetY = { -it },
                            animationSpec = tween(450, easing = FastOutSlowInEasing)
                        ) + fadeIn(animationSpec = tween(450))
                    },
                    exitTransition = {
                        fadeOut(animationSpec = tween(450))
                    },
                    popEnterTransition = {
                        fadeIn(animationSpec = tween(450))
                    },
                    popExitTransition = {
                        slideOutVertically(
                            targetOffsetY = { -it },
                            animationSpec = tween(450, easing = FastOutSlowInEasing)
                        ) + fadeOut(animationSpec = tween(450))
                    }
                ) {
                    RegisterFormScreen(navController = navController, isTablet = isTablet)
                }
                composable(
                    route = "relogin",
                    enterTransition = { fadeIn(animationSpec = tween(450)) },
                    exitTransition = { fadeOut(animationSpec = tween(450)) }
                ) {
                    ReLoginScreen(navController = navController)
                }
                composable("mfa_verify") {
                    MfaVerifyScreen(navController = navController)
                }
                composable("dashboard") {
                    MainScreen(
                            navController = navController,
                            userId = activeUserId,
                            homeViewModel = homeViewModel!!,
                            chatViewModel = chatViewModel!!
                    )
                }
                composable("leaderboards") {
                    com.eous.mentor.features.leaderboard.LeaderboardsScreen(
                        userId = activeUserId,
                        navController = navController
                    )
                }
                composable("friends?tab={tab}") { backStackEntry ->
                    val tab = backStackEntry.arguments?.getString("tab")?.toIntOrNull() ?: 0
                    com.eous.mentor.features.friends.FriendsScreen(
                        userId = activeUserId,
                        navController = navController,
                        initialTab = tab
                    )
                }
                composable("friend_profile/{targetUserId}") { backStackEntry ->
                    val targetUserId = backStackEntry.arguments?.getString("targetUserId") ?: ""
                    com.eous.mentor.features.friends.FriendProfileScreen(
                        currentUserId = activeUserId,
                        targetUserId = targetUserId,
                        navController = navController
                    )
                }
            }
        }

object GlobalNavigationHelper {
    var pendingRoute: String? = null
}



// --- Double-click Prevention Navigation Helpers ---
fun NavController.navigateSafe(route: String) {
    if (currentBackStackEntry?.lifecycle?.currentState == Lifecycle.State.RESUMED) {
        navigate(route)
    }
}

fun NavController.navigateSafe(route: String, builder: androidx.navigation.NavOptionsBuilder.() -> Unit) {
    if (currentBackStackEntry?.lifecycle?.currentState == Lifecycle.State.RESUMED) {
        navigate(route, builder)
    }
}
