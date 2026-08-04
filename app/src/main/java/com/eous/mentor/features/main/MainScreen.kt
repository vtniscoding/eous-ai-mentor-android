package com.eous.mentor.features.main

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.navigation.NavController
import androidx.compose.animation.animateColorAsState
import androidx.lifecycle.viewmodel.compose.viewModel
import com.eous.mentor.core.ui.components.MainNavigationBar
import com.eous.mentor.core.ui.theme.*
import com.eous.mentor.features.chat.Chat
import com.eous.mentor.features.chat.ChatViewModel
import com.eous.mentor.features.home.HomeScreen
import com.eous.mentor.features.home.HomeViewModel
import com.eous.mentor.features.pro.Pro
import com.eous.mentor.features.search.Search
import com.eous.mentor.features.library.Library
import com.eous.mentor.features.library.LibraryViewModel
import com.eous.mentor.features.alert.AlertScreen
import com.eous.mentor.features.timer.TimerSection
import com.eous.mentor.features.quizzes.QuizzesSection
import com.eous.mentor.features.quizzes.QuizzesScreen
import com.eous.mentor.features.quizzes.QuizzesViewModel
import com.eous.mentor.features.flashcards.FlashcardsSection
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext
import com.eous.mentor.core.data.repository.SavedAccountsRepository
import com.eous.mentor.features.personal.PersonalScreen
import com.eous.mentor.features.settings.SettingsScreen
import com.eous.mentor.features.profile.ProfileScreen
import com.eous.mentor.features.progress.ProgressScreen
import com.eous.mentor.features.progress.ProgressViewModel
import kotlinx.coroutines.launch

private fun getScreenIndex(route: String): Int {
    return when (route) {
        "dashboard" -> 0
        "library" -> 1
        "chat" -> 2
        "search" -> 2
        "alert" -> 3
        "personal" -> 4
        "settings" -> 5
        "profile" -> 6
        "pro" -> 7
        "progress" -> 8
        else -> 0
    }
}

@Composable
fun MainScreen(
    navController: NavController,
    userId: String,
    viewModel: MainScreenViewModel = viewModel(),
    homeViewModel: HomeViewModel = remember(userId) { HomeViewModel(userId) },
    chatViewModel: ChatViewModel = remember(userId) { ChatViewModel(userId = userId) }
) {
    val state by viewModel.state.collectAsState()

    val libraryViewModel = remember(userId) { LibraryViewModel(userId = userId) }
    val personalViewModel = remember(userId) { com.eous.mentor.features.personal.PersonalViewModel(userId = userId) }
    val progressViewModel = remember(userId) { ProgressViewModel(userId = userId) }
    val quizzesViewModel = remember(userId) { QuizzesViewModel() }

    val homeState by homeViewModel.state.collectAsState()
    val chatState by chatViewModel.state.collectAsState()
    val personalState by personalViewModel.state.collectAsState()
    val progressState by progressViewModel.state.collectAsState()

    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    LaunchedEffect(personalState.profile) {
        val profile = personalState.profile
        if (profile != null) {
            if (!profile.onboarding_completed) {
                viewModel.navigateTo("profile")
            } else {
                val localSessionId = com.eous.mentor.di.RepositoryProvider.sessionRepository.getLocalSessionId(context)
                val remoteSessionId = profile.current_session_id
                if (!remoteSessionId.isNullOrEmpty()) {
                    if (localSessionId.isEmpty() || localSessionId != remoteSessionId) {
                        val currentAvatarUrl = profile.avatar_url
                        val currentEmail = profile.email ?: com.eous.mentor.di.RepositoryProvider.sessionRepository.getCurrentUserEmail()
                        com.eous.mentor.di.RepositoryProvider.sessionRepository.clearLocalSessionId(context)
                        
                        homeViewModel.logout(
                            onSuccess = {
                                if (!currentEmail.isNullOrBlank()) {
                                    SavedAccountsRepository.saveAccount(
                                        context,
                                        com.eous.mentor.domain.model.SavedAccount(
                                            email = currentEmail,
                                            avatarUrl = currentAvatarUrl
                                        )
                                    )
                                }
                                Toast.makeText(
                                    context,
                                    "Session expired: Account logged in on another device!",
                                    Toast.LENGTH_LONG
                                ).show()
                                val savedAccounts = SavedAccountsRepository.getSavedAccounts(context)
                                val targetRoute = if (savedAccounts.isNotEmpty()) "relogin" else "login"
                                navController.navigate(targetRoute) {
                                    popUpTo(0) { inclusive = true }
                                }
                            },
                            onError = {
                                Toast.makeText(
                                    context,
                                    "Session expired. Logged out!",
                                    Toast.LENGTH_LONG
                                ).show()
                                val savedAccounts = SavedAccountsRepository.getSavedAccounts(context)
                                val targetRoute = if (savedAccounts.isNotEmpty()) "relogin" else "login"
                                navController.navigate(targetRoute) {
                                    popUpTo(0) { inclusive = true }
                                }
                            }
                        )
                    }
                } else {
                    val newSessionId = java.util.UUID.randomUUID().toString()
                    com.eous.mentor.di.RepositoryProvider.sessionRepository.saveLocalSessionId(context, newSessionId)
                    scope.launch {
                        com.eous.mentor.di.RepositoryProvider.userRepository.updateSessionId(userId, newSessionId)
                    }
                }
            }
        }
    }

    LaunchedEffect(userId) {
        if (userId.isNotEmpty()) {
            while (true) {
                kotlinx.coroutines.delay(8000L)
                try {
                    val remoteSessionId = com.eous.mentor.di.RepositoryProvider.userRepository.getRemoteSessionId(userId).getOrNull()
                    val localSessionId = com.eous.mentor.di.RepositoryProvider.sessionRepository.getLocalSessionId(context)
                    if (!remoteSessionId.isNullOrEmpty() && localSessionId.isNotEmpty() && localSessionId != remoteSessionId) {
                        val profile = personalState.profile
                        val currentAvatarUrl = profile?.avatar_url
                        val currentEmail = profile?.email ?: com.eous.mentor.di.RepositoryProvider.sessionRepository.getCurrentUserEmail()
                        com.eous.mentor.di.RepositoryProvider.sessionRepository.clearLocalSessionId(context)
                        
                        homeViewModel.logout(
                            onSuccess = {
                                if (!currentEmail.isNullOrBlank()) {
                                    SavedAccountsRepository.saveAccount(
                                        context,
                                        com.eous.mentor.domain.model.SavedAccount(
                                            email = currentEmail,
                                            avatarUrl = currentAvatarUrl
                                        )
                                    )
                                }
                                Toast.makeText(
                                    context,
                                    "Session expired: Account logged in on another device!",
                                    Toast.LENGTH_LONG
                                ).show()
                                val savedAccounts = SavedAccountsRepository.getSavedAccounts(context)
                                val targetRoute = if (savedAccounts.isNotEmpty()) "relogin" else "login"
                                navController.navigate(targetRoute) {
                                    popUpTo(0) { inclusive = true }
                                }
                            },
                            onError = {
                                Toast.makeText(
                                    context,
                                    "Session expired. Logged out!",
                                    Toast.LENGTH_LONG
                                ).show()
                                val savedAccounts = SavedAccountsRepository.getSavedAccounts(context)
                                val targetRoute = if (savedAccounts.isNotEmpty()) "relogin" else "login"
                                navController.navigate(targetRoute) {
                                    popUpTo(0) { inclusive = true }
                                }
                            }
                        )
                        break
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    val isScreenLoading = when (state.currentScreen) {
        "dashboard" -> homeState.isLoading
        "personal" -> personalState.isLoading
        "progress" -> progressState.isLoading
        "chat" -> chatState.isLoadingSessions || chatState.isLoadingMessages
        else -> false
    }

    val isSubScreen = state.currentScreen in listOf("timer", "quizzes", "flashcards", "progress", "settings", "profile")

    // Back handler: go back to settings from profile, personal from settings, or dashboard from any other non-dashboard tab
    if (state.currentScreen == "profile") {
        val isForce = personalState.profile?.onboarding_completed == false
        BackHandler {
            if (!isForce) {
                viewModel.navigateTo("settings")
            }
        }
    } else if (state.currentScreen == "settings") {
        BackHandler { viewModel.navigateTo("personal") }
    } else if (state.currentScreen != "dashboard") {
        BackHandler { viewModel.navigateTo("dashboard") }
    }


    val bgCol = Color(0xFFA566FE)
    Box(modifier = Modifier.fillMaxSize().background(bgCol)) {
        // --- MAIN CONTENT ---
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            AnimatedContent(
                targetState = state.currentScreen,
                transitionSpec = {
                    if (targetState == "settings" || initialState == "settings") {
                        val initialIndex = getScreenIndex(initialState)
                        val targetIndex = getScreenIndex(targetState)
                        if (targetIndex > initialIndex) {
                            slideInHorizontally(animationSpec = tween(300)) { it } +
                                    fadeIn(animationSpec = tween(300)) togetherWith
                                    slideOutHorizontally(animationSpec = tween(300)) { -it } +
                                    fadeOut(animationSpec = tween(300))
                        } else {
                            slideInHorizontally(animationSpec = tween(300)) { -it } +
                                    fadeIn(animationSpec = tween(300)) togetherWith
                                    slideOutHorizontally(animationSpec = tween(300)) { it } +
                                    fadeOut(animationSpec = tween(300))
                        }
                    } else {
                        EnterTransition.None togetherWith ExitTransition.None
                    }
                },
                modifier = Modifier.fillMaxSize(),
                label = "tab_transition"
            ) { screen ->
                when (screen) {
                    "dashboard" -> {
                        HomeScreen(
                            navController = navController,
                            userId = userId,
                            onNavigateToTab = { tab ->
                                if (tab.startsWith("tools_")) {
                                    val tool = tab.substringAfter("tools_")
                                    viewModel.navigateTo(tool)
                                } else {
                                    viewModel.navigateTo(tab)
                                }
                            },
                            viewModel = homeViewModel
                        )
                    }
                    "progress" -> {
                        ProgressScreen(
                            onBack = { viewModel.navigateTo("dashboard") },
                            viewModel = progressViewModel
                        )
                    }
                    "chat" -> {
                        Chat(
                            userId = userId,
                            onMenuClick = {},
                            initialQuestion = state.chatInitialQuestion,
                            viewModel = chatViewModel,
                            onNavigateToSearch = {
                                viewModel.navigateTo("search")
                            },
                            onNavigateToQuizzes = {
                                viewModel.navigateTo("quizzes")
                            },
                            onBack = state.chatBackDestination?.let { dest ->
                                {
                                    chatViewModel.startNewChat()
                                    viewModel.navigateTo(dest)
                                }
                            }
                        )
                    }
                    "library" -> {
                        Library(
                            userId = userId,
                            onBack = { viewModel.navigateTo("dashboard") },
                            onNavigateToChatSession = { session ->
                                chatViewModel.selectSession(session)
                                viewModel.navigateTo("chat", chatBackDest = "library")
                            },
                            onPracticeClick = { subject ->
                                quizzesViewModel.createQuizWithAi(
                                    userId = userId,
                                    topic = subject,
                                    prompt = "Review $subject"
                                )
                                viewModel.navigateTo("quizzes")
                            },
                            viewModel = libraryViewModel
                        )
                    }
                    "alert" -> {
                        AlertScreen(
                            onMenuClick = {}
                        )
                    }
                    "timer" -> {
                        ToolScreenContainer(
                            title = "Study Timer",
                            onBack = { viewModel.navigateTo("dashboard") }
                        ) {
                            TimerSection()
                        }
                    }
                    "quizzes" -> {
                        QuizzesScreen(
                            userId = userId,
                            onBack = { viewModel.navigateTo("dashboard") },
                            viewModel = quizzesViewModel
                        )
                    }
                    "flashcards" -> {
                        ToolScreenContainer(
                            title = "Recall Flashcards",
                            onBack = { viewModel.navigateTo("dashboard") }
                        ) {
                            FlashcardsSection()
                        }
                    }
                    "pro" -> {
                        Pro(
                            onMenuClick = {
                                viewModel.navigateTo("dashboard")
                            }
                        )
                    }
                    "search" -> {
                        Search(
                            onMenuClick = {
                                viewModel.navigateTo("chat")
                            }
                        )
                    }
                    "personal" -> {
                        val context = LocalContext.current
                        PersonalScreen(
                            userId = userId,
                            navController = navController,
                            onLogout = {
                                val currentAvatarUrl = personalState.profile?.avatar_url
                                val currentEmail = personalState.profile?.email ?: com.eous.mentor.di.RepositoryProvider.sessionRepository.getCurrentUserEmail()
                                homeViewModel.logout(
                                    onSuccess = {
                                        if (!currentEmail.isNullOrBlank()) {
                                            SavedAccountsRepository.saveAccount(
                                                context,
                                                com.eous.mentor.domain.model.SavedAccount(
                                                    email = currentEmail,
                                                    avatarUrl = currentAvatarUrl
                                                )
                                            )
                                        }
                                        Toast.makeText(
                                            context,
                                            "Logged out successfully!",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        val savedAccounts = SavedAccountsRepository.getSavedAccounts(context)
                                        val targetRoute = if (savedAccounts.isNotEmpty()) "relogin" else "login"
                                        navController.navigate(targetRoute) {
                                            popUpTo(0) { inclusive = true }
                                        }
                                    },
                                    onError = {
                                        Toast.makeText(
                                            context,
                                            "Error logging out",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                )
                            },
                            onOpenSettings = {
                                viewModel.navigateTo("settings")
                            },
                            onOpenPro = {
                                viewModel.navigateTo("pro")
                            },
                            onBack = {
                                viewModel.navigateTo("dashboard")
                            },
                            viewModel = personalViewModel
                        )
                    }
                    "settings" -> {
                        val context = LocalContext.current
                        SettingsScreen(
                            onBack = { viewModel.navigateTo("personal") },
                            onOpenProfile = { viewModel.navigateTo("profile") },
                            onLogout = {
                                val currentAvatarUrl = personalState.profile?.avatar_url
                                val currentEmail = personalState.profile?.email ?: com.eous.mentor.di.RepositoryProvider.sessionRepository.getCurrentUserEmail()
                                homeViewModel.logout(
                                    onSuccess = {
                                        if (!currentEmail.isNullOrBlank()) {
                                            SavedAccountsRepository.saveAccount(
                                                context,
                                                com.eous.mentor.domain.model.SavedAccount(
                                                    email = currentEmail,
                                                    avatarUrl = currentAvatarUrl
                                                )
                                            )
                                        }
                                        Toast.makeText(
                                            context,
                                            "Logged out successfully!",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        val savedAccounts = SavedAccountsRepository.getSavedAccounts(context)
                                        val targetRoute = if (savedAccounts.isNotEmpty()) "relogin" else "login"
                                        navController.navigate(targetRoute) {
                                            popUpTo(0) { inclusive = true }
                                        }
                                    },
                                    onError = {
                                        Toast.makeText(
                                            context,
                                            "Error logging out",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                )
                            }
                        )
                    }
                    "profile" -> {
                        val isForce = personalState.profile?.onboarding_completed == false
                        ProfileScreen(
                            userId = userId,
                            isForceOnboarding = isForce,
                            onBack = {
                                if (!isForce) {
                                    viewModel.navigateTo("settings")
                                }
                            },
                            onComplete = {
                                personalViewModel.loadData(isSilentRefresh = true)
                                viewModel.navigateTo("dashboard")
                            }
                        )
                    }
                }
            }
        }

        // --- BOTTOM NAVIGATION BAR ---
        if (!isSubScreen && !isScreenLoading) {
            MainNavigationBar(
                currentScreen = state.currentScreen,
                onNavigate = { route -> viewModel.navigateTo(route) },
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }
    }
}

@Composable
fun ToolScreenContainer(
    title: String,
    onBack: () -> Unit,
    content: @Composable () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        // Background glow
        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .size(300.dp)
                .blur(90.dp)
                .background(Brush.radialGradient(listOf(EousPurple.copy(alpha = 0.08f), Color.Transparent)))
        )
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            // Top bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 15.dp),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = title,
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            
            // Content
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 10.dp)
            ) {
                content()
            }
        }
    }
}
