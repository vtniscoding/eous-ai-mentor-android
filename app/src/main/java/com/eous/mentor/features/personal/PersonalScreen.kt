package com.eous.mentor.features.personal

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.eous.mentor.core.navigation.navigateSafe
import com.eous.mentor.domain.model.Profile
import coil3.compose.AsyncImage
import com.eous.mentor.R
import com.eous.mentor.core.ui.theme.Inter

private val PrimaryPurple = Color(0xFF5B29A2)
private val CardBannerPurple = Color(0xFF7F43D4)
private val DarkBorderColor = Color(0xFF1E293B)

@Composable
fun PersonalScreen(
        userId: String,
        navController: NavController,
        onLogout: () -> Unit,
        onOpenSettings: () -> Unit = {},
        onOpenPro: () -> Unit = {},
        onBack: () -> Unit = {},
        viewModel: PersonalViewModel = remember(userId) { PersonalViewModel(userId) }
) {
    val context = LocalContext.current
    val state by viewModel.state.collectAsState()

    var selectedAvatarUri by remember { mutableStateOf<Uri?>(null) }
    var pendingAvatarUri by remember { mutableStateOf<Uri?>(null) }
    var showPreviewDialog by remember { mutableStateOf(false) }
    var showOptionsDialog by remember { mutableStateOf(false) }
    var isSavingAvatar by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()

    val imagePickerLauncher =
            rememberLauncherForActivityResult(contract = ActivityResultContracts.GetContent()) {
                    uri: Uri? ->
                if (uri != null) {
                    pendingAvatarUri = uri
                    showPreviewDialog = true
                }
            }

    if (state.isLoading) {
        com.eous.mentor.core.ui.components.PreparingLoadingScreen()
    } else {
        Box(modifier = Modifier.fillMaxSize()) {
            // Full screen profile_background.xml background image
            Image(
                    painter = painterResource(id = R.drawable.profile_background),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.FillBounds
            )

            Box(modifier = Modifier.fillMaxSize()) {
                // Scrollable Content Sheet (fills whole screen from top)
                Column(modifier = Modifier.fillMaxSize().verticalScroll(scrollState)) {
                    val displayName = state.displayName
                    val displayEmail = state.displayEmail
                    val initials = state.initials
                    val streak = state.dashboardStats?.streak ?: 0

                    Box(modifier = Modifier.fillMaxWidth()) {
                        // Main White Container Card
                        Column(
                                modifier =
                                        Modifier.fillMaxWidth()
                                                .padding(start = 16.dp, end = 16.dp, top = 125.dp)
                                                .background(Color.White, RoundedCornerShape(24.dp))
                                                .padding(horizontal = 20.dp, vertical = 20.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Spacer(modifier = Modifier.height(35.dp))

                            Text(
                                    text = displayName,
                                    color = Color.Black,
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = Inter
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                    text = displayEmail,
                                    color = Color(0xFF64748B),
                                    fontSize = 14.sp,
                                    fontFamily = Inter
                            )

                            Spacer(modifier = Modifier.height(20.dp))

                            // Stats Banner Card with Drop Shadow
                             StatsBannerCard(
                                 streak = streak,
                                 friendsCount = state.friends.size,
                                 onFriendsClick = { navController.navigateSafe("friends") }
                             )

                             Spacer(modifier = Modifier.height(24.dp))

                             // 0. MY FRIENDS SECTION
                             MyFriendsSection(
                                 navController = navController,
                                 friends = state.friends
                             )

                             Spacer(modifier = Modifier.height(22.dp))

                             // 1. BADGES SECTION
                             val todayQuizzesCompleted = remember(state.dashboardStats?.quizzes) {
                                 val todayStr = java.time.LocalDate.now().toString()
                                 state.dashboardStats?.quizzes?.count { it.created_at.startsWith(todayStr) } ?: 0
                             }
                             BadgesSection(streak = streak, todayQuizzesCompleted = todayQuizzesCompleted)

                             Spacer(modifier = Modifier.height(22.dp))

                             // 2. LEADERBOARDS SECTION
                             LeaderboardsSection(
                                 navController = navController,
                                 displayName = state.displayName,
                                 totalQueries = state.dashboardStats?.totalQueries ?: 0,
                                 libraryItems = state.dashboardStats?.libraryItems ?: 0
                             )

                             Spacer(modifier = Modifier.height(24.dp))

                            // 3. PRO UPGRADE CARD with Drop Shadow & compact text spacing
                            ProUpgradeCard(onClaimPro = onOpenPro)

                            Spacer(modifier = Modifier.height(30.dp))
                        }

                        // Avatar Circle aligned at top center (overlapping white sheet)
                        Box(
                                modifier =
                                        Modifier.align(Alignment.TopCenter)
                                                .padding(top = 80.dp)
                                                .size(90.dp)
                        ) {
                            val currentAvatarUrl = state.profile?.avatar_url
                            val displayAvatarModel: Any? = pendingAvatarUri ?: currentAvatarUrl

                            Box(
                                    modifier =
                                            Modifier.fillMaxSize()
                                                    .background(Color(0xFFCFD1FF), CircleShape)
                                                    .border(3.dp, PrimaryPurple, CircleShape)
                                                    .clip(CircleShape)
                                                    .clickable {
                                                        if (!currentAvatarUrl.isNullOrEmpty() || pendingAvatarUri != null) {
                                                            showOptionsDialog = true
                                                        } else {
                                                            imagePickerLauncher.launch("image/*")
                                                        }
                                                    },
                                    contentAlignment = Alignment.Center
                            ) {
                                if (displayAvatarModel != null) {
                                    AsyncImage(
                                            model = displayAvatarModel,
                                            contentDescription = "Avatar Image",
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier.fillMaxSize().clip(CircleShape)
                                    )
                                } else {
                                    Text(
                                            text = initials,
                                            color = PrimaryPurple,
                                            fontSize = 32.sp,
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = Inter
                                    )
                                }
                            }

                            // Pencil Edit Badge (launches options or image picker)
                            Box(
                                    modifier =
                                            Modifier.size(26.dp)
                                                    .background(Color.White, CircleShape)
                                                    .border(1.5.dp, PrimaryPurple, CircleShape)
                                                    .align(Alignment.BottomEnd)
                                                    .clickable {
                                                        if (!currentAvatarUrl.isNullOrEmpty() || pendingAvatarUri != null) {
                                                            showOptionsDialog = true
                                                        } else {
                                                            imagePickerLauncher.launch("image/*")
                                                        }
                                                    },
                                    contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                        imageVector = Icons.Default.Edit,
                                        contentDescription = "Edit Profile Picture",
                                        tint = PrimaryPurple,
                                        modifier = Modifier.size(13.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(100.dp))
                }

                // Avatar Options Modal Dialog
                if (showOptionsDialog) {
                    AlertDialog(
                        onDismissRequest = { showOptionsDialog = false },
                        shape = RoundedCornerShape(24.dp),
                        containerColor = Color.White,
                        title = {
                            Text(
                                text = "Profile Picture",
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp,
                                fontFamily = Inter,
                                color = Color(0xFF1E293B)
                            )
                        },
                        text = {
                            Column(
                                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Surface(
                                    onClick = {
                                        showOptionsDialog = false
                                        showPreviewDialog = true
                                    },
                                    shape = RoundedCornerShape(14.dp),
                                    color = Color(0xFFF8FAFC),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Person,
                                            contentDescription = null,
                                            tint = PrimaryPurple,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(14.dp))
                                        Text(
                                            text = "View Profile Picture",
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            fontFamily = Inter,
                                            color = Color(0xFF1E293B)
                                        )
                                    }
                                }

                                Surface(
                                    onClick = {
                                        showOptionsDialog = false
                                        imagePickerLauncher.launch("image/*")
                                    },
                                    shape = RoundedCornerShape(14.dp),
                                    color = Color(0xFFF8FAFC),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Edit,
                                            contentDescription = null,
                                            tint = PrimaryPurple,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(14.dp))
                                        Text(
                                            text = "Upload New Photo",
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            fontFamily = Inter,
                                            color = Color(0xFF1E293B)
                                        )
                                    }
                                }

                                Surface(
                                    onClick = {
                                        showOptionsDialog = false
                                        pendingAvatarUri = null
                                        viewModel.deleteAvatar()
                                    },
                                    shape = RoundedCornerShape(14.dp),
                                    color = Color(0xFFFEF2F2),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = null,
                                            tint = Color(0xFFEF4444),
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(14.dp))
                                        Text(
                                            text = "Remove Photo",
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            fontFamily = Inter,
                                            color = Color(0xFFEF4444)
                                        )
                                    }
                                }
                            }
                        },
                        confirmButton = {},
                        dismissButton = {
                            TextButton(onClick = { showOptionsDialog = false }) {
                                Text("Cancel", color = Color(0xFF64748B), fontFamily = Inter, fontWeight = FontWeight.Medium)
                            }
                        }
                    )
                }

                // Avatar Preview Modal Dialog
                if (showPreviewDialog) {
                    val previewModel: Any? = pendingAvatarUri ?: state.profile?.avatar_url
                    AlertDialog(
                        onDismissRequest = {
                            if (!isSavingAvatar) {
                                showPreviewDialog = false
                            }
                        },
                        shape = RoundedCornerShape(24.dp),
                        containerColor = Color.White,
                        title = {
                            Text(
                                text = if (pendingAvatarUri != null) "Preview Photo" else "Profile Picture",
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp,
                                fontFamily = Inter,
                                color = Color(0xFF1E293B)
                            )
                        },
                        text = {
                            Column(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(210.dp)
                                        .background(Color(0xFFCFD1FF), CircleShape)
                                        .border(4.dp, PrimaryPurple, CircleShape)
                                        .clip(CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (previewModel != null) {
                                        AsyncImage(
                                            model = previewModel,
                                            contentDescription = "Avatar Preview",
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier.fillMaxSize().clip(CircleShape)
                                        )
                                    } else {
                                        Text(
                                            text = state.initials,
                                            color = PrimaryPurple,
                                            fontSize = 64.sp,
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = Inter
                                        )
                                    }
                                }

                                if (isSavingAvatar) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.5.dp, color = PrimaryPurple)
                                        Text("Uploading photo...", fontSize = 14.sp, color = Color(0xFF64748B), fontFamily = Inter)
                                    }
                                }
                            }
                        },
                        confirmButton = {
                            if (pendingAvatarUri != null) {
                                Button(
                                    onClick = {
                                        pendingAvatarUri?.let { uri ->
                                            isSavingAvatar = true
                                            try {
                                                val inputStream = context.contentResolver.openInputStream(uri)
                                                val bytes = inputStream?.readBytes()
                                                if (bytes != null) {
                                                    viewModel.uploadAvatar(bytes) {
                                                        isSavingAvatar = false
                                                        showPreviewDialog = false
                                                        pendingAvatarUri = null
                                                    }
                                                } else {
                                                    isSavingAvatar = false
                                                }
                                            } catch (e: Exception) {
                                                e.printStackTrace()
                                                isSavingAvatar = false
                                            }
                                        }
                                    },
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple),
                                    enabled = !isSavingAvatar
                                ) {
                                    Text("Save Photo", color = Color.White, fontFamily = Inter, fontWeight = FontWeight.SemiBold)
                                }
                            } else if (state.profile?.avatar_url != null) {
                                Button(
                                    onClick = {
                                        showPreviewDialog = false
                                        viewModel.deleteAvatar()
                                    },
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
                                ) {
                                    Text("Remove Photo", color = Color.White, fontFamily = Inter, fontWeight = FontWeight.SemiBold)
                                }
                            }
                        },
                        dismissButton = {
                            TextButton(
                                onClick = {
                                    showPreviewDialog = false
                                    pendingAvatarUri = null
                                },
                                enabled = !isSavingAvatar
                            ) {
                                Text(if (pendingAvatarUri != null) "Cancel" else "Close", color = Color(0xFF64748B), fontFamily = Inter, fontWeight = FontWeight.Medium)
                            }
                        }
                    )
                }

                // Floating Overlay Button (Settings on Top Right)
                Box(
                        modifier =
                                Modifier.fillMaxWidth()
                                        .statusBarsPadding()
                                        .padding(horizontal = 20.dp, vertical = 12.dp)
                ) {
                    val settingsInteractionSource = remember { MutableInteractionSource() }
                    val isSettingsHovered by settingsInteractionSource.collectIsHoveredAsState()
                    val isSettingsPressed by settingsInteractionSource.collectIsPressedAsState()
                    val settingsScale by
                            animateFloatAsState(
                                    targetValue =
                                            if (isSettingsPressed) 0.90f
                                            else if (isSettingsHovered) 1.10f else 1.0f,
                                    animationSpec = spring(stiffness = Spring.StiffnessLow),
                                    label = "settings_scale"
                            )

                    Box(
                            modifier =
                                    Modifier.size(40.dp)
                                            .align(Alignment.TopEnd)
                                            .graphicsLayer {
                                                scaleX = settingsScale
                                                scaleY = settingsScale
                                            }
                                            .background(
                                                    if (isSettingsHovered || isSettingsPressed)
                                                            Color.White.copy(alpha = 0.6f)
                                                    else Color.White.copy(alpha = 0.3f),
                                                    CircleShape
                                            )
                                            .clickable(
                                                    interactionSource = settingsInteractionSource,
                                                    indication = null
                                            ) { onOpenSettings() },
                            contentAlignment = Alignment.Center
                    ) {
                        Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "Settings",
                                tint = DarkBorderColor,
                                modifier = Modifier.size(26.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StatsBannerCard(
    streak: Int,
    friendsCount: Int,
    onFriendsClick: () -> Unit
) {
    Box(modifier = Modifier.fillMaxWidth()) {
        // Ear accents matching design mockup
        Box(
                modifier =
                        Modifier.offset(x = 34.dp, y = (-18).dp)
                                .size(24.dp, 26.dp)
                                .background(
                                        PrimaryPurple,
                                        RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp)
                                )
        )
        Box(
                modifier =
                        Modifier.align(Alignment.TopEnd)
                                .offset(x = (-34).dp, y = (-18).dp)
                                .size(24.dp, 26.dp)
                                .background(
                                        PrimaryPurple,
                                        RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp)
                                )
        )

        // Main Banner Card with elevation shadow
        Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = CardBannerPurple),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                modifier = Modifier.fillMaxWidth()
        ) {
            Box(modifier = Modifier.padding(all = 8.dp)) {
                Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                ) {
                    // ACTIVE DAYS
                    Box(
                            modifier =
                                    Modifier.weight(1f)
                                            .background(
                                                    Color(0xFF5E27B6),
                                                    RoundedCornerShape(16.dp)
                                            )
                                            .padding(vertical = 12.dp, horizontal = 4.dp),
                            contentAlignment = Alignment.Center
                    ) {
                        StatColumn(
                                icon = {
                                    Icon(
                                            imageVector = Icons.Default.FlashOn,
                                            contentDescription = null,
                                            tint = Color(0xFF4ADE80),
                                            modifier = Modifier.size(22.dp)
                                    )
                                },
                                label = "Active Days",
                                value = streak.toString()
                        )
                    }

                    // RANKS
                    Box(
                            modifier =
                                    Modifier.weight(1f)
                                            .background(
                                                    Color(0xFF5E27B6),
                                                    RoundedCornerShape(16.dp)
                                            )
                                            .padding(vertical = 12.dp, horizontal = 4.dp),
                            contentAlignment = Alignment.Center
                    ) {
                        StatColumn(
                                icon = {
                                    Icon(
                                            imageVector = Icons.Default.EmojiEvents,
                                            contentDescription = null,
                                            tint = Color(0xFFFACC15),
                                            modifier = Modifier.size(22.dp)
                                    )
                                },
                                label = "Ranks",
                                value = "#10"
                        )
                    }

                    // FRIENDS
                    Box(
                            modifier =
                                    Modifier.weight(1f)
                                            .background(
                                                    Color(0xFF5E27B6),
                                                    RoundedCornerShape(16.dp)
                                            )
                                            .clip(RoundedCornerShape(16.dp))
                                            .clickable { onFriendsClick() }
                                            .padding(vertical = 12.dp, horizontal = 4.dp),
                            contentAlignment = Alignment.Center
                    ) {
                        StatColumn(
                                icon = {
                                    Icon(
                                            imageVector = Icons.Default.Person,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(22.dp)
                                    )
                                },
                                label = "Friends",
                                value = friendsCount.toString()
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StatColumn(icon: @Composable () -> Unit, label: String, value: String) {
    Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        icon()
        Text(
                text = label,
                color = Color.White.copy(alpha = 0.8f),
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                fontFamily = Inter
        )
        Text(
                text = value,
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = Inter
        )
    }
}

@Composable
private fun MyFriendsSection(
    navController: NavController,
    friends: List<Profile>
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { navController.navigateSafe("friends") },
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = "My Friends",
                    color = Color.Black,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = Inter
                )
                Image(
                    painter = painterResource(id = R.drawable.ic_heart),
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
            }
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "View all friends",
                tint = Color.Black,
                modifier = Modifier.size(22.dp)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (friends.isEmpty()) {
            Text(
                text = "No friends yet. Add friends to study together!",
                color = Color(0xFF64748B),
                fontSize = 13.sp,
                fontFamily = Inter,
                modifier = Modifier.padding(vertical = 4.dp)
            )
        } else {
            Row(
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                friends.take(4).forEach { friend ->
                    val friendName = friend.display_name ?: friend.email?.substringBefore("@") ?: "User"
                    val friendInitial = friendName.trim().split("\\s+".toRegex()).lastOrNull()?.firstOrNull()?.uppercase() ?: "U"
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .background(Color(0xFFDDE0FF), CircleShape)
                            .clip(CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        if (!friend.avatar_url.isNullOrEmpty()) {
                            AsyncImage(
                                model = friend.avatar_url,
                                contentDescription = "Friend Avatar",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Text(
                                text = friendInitial,
                                color = PrimaryPurple,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = Inter
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BadgesSection(streak: Int, todayQuizzesCompleted: Int) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                        text = "Badges",
                        color = Color.Black,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = Inter
                )
                Image(
                        painter = painterResource(id = R.drawable.ic_heart),
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                )
            }
            Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = "View all badges",
                    tint = Color.Black,
                    modifier = Modifier.size(22.dp)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalAlignment = Alignment.CenterVertically
        ) {
            BadgeItem(drawableRes = R.drawable.ic_badge_streak_3, isUnlocked = streak >= 3)
            BadgeItem(drawableRes = R.drawable.ic_badge_streak_5, isUnlocked = streak >= 5)
            BadgeItem(drawableRes = R.drawable.ic_badge_streak_10, isUnlocked = streak >= 10)
            BadgeItem(drawableRes = R.drawable.ic_chest, isUnlocked = todayQuizzesCompleted >= 5)
        }
    }
}

@Composable
private fun BadgeItem(drawableRes: Int, isUnlocked: Boolean) {
    Box(modifier = Modifier.size(76.dp), contentAlignment = Alignment.Center) {
        Image(
                painter = painterResource(id = drawableRes),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                colorFilter =
                        if (isUnlocked) null
                        else ColorFilter.colorMatrix(ColorMatrix().apply { setToSaturation(0f) }),
                alpha = if (isUnlocked) 1.0f else 0.45f
        )
    }
}

@Composable
private fun LeaderboardsSection(
    navController: NavController,
    displayName: String,
    totalQueries: Int,
    libraryItems: Int
) {
    val userXp = totalQueries * 10 + libraryItems * 20
    val userInitial = displayName.trim().split("\\s+".toRegex()).lastOrNull()?.firstOrNull()?.uppercase() ?: "U"

    val leaderboardList = remember(displayName, userXp) {
        val list = mutableListOf(
            com.eous.mentor.features.leaderboard.LeaderboardEntry(name = displayName, xp = userXp, initials = userInitial, isCurrentUser = true),
            com.eous.mentor.features.leaderboard.LeaderboardEntry(name = "Truong Nguyen", xp = 170, initials = "N", isCurrentUser = false),
            com.eous.mentor.features.leaderboard.LeaderboardEntry(name = "Trieu Hoang", xp = 150, initials = "T", isCurrentUser = false)
        )
        list.sortByDescending { it.xp }
        list
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { navController.navigateSafe("leaderboards") },
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = "Leaderboards",
                    color = Color.Black,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = Inter
                )
                Image(
                    painter = painterResource(id = R.drawable.ic_heart),
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
            }
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "View Leaderboards",
                tint = Color.Black,
                modifier = Modifier.size(22.dp)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            leaderboardList.take(3).forEachIndexed { index, entry ->
                LeaderboardCompactItem(entry = entry, rank = index + 1)
            }
        }
    }
}

@Composable
private fun LeaderboardCompactItem(entry: com.eous.mentor.features.leaderboard.LeaderboardEntry, rank: Int) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Box(
            modifier = Modifier
                .size(60.dp)
                .background(Color(0xFFDDE0FF), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = entry.initials,
                color = Color(0xFF5B29A2),
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = Inter
            )
        }

        Text(
            text = if (rank == 1) "Top 1" else rank.toString(),
            color = if (rank == 1) Color(0xFF5B29A2) else Color(0xFF64748B),
            fontSize = 14.sp,
            fontWeight = if (rank == 1) FontWeight.Bold else FontWeight.Medium,
            fontFamily = Inter
        )
    }
}

@Composable
private fun ProUpgradeCard(onClaimPro: () -> Unit) {
    Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFDDE0FF)),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            modifier = Modifier.fillMaxWidth()
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            // Mascot Image positioned on Bottom-Right
            Image(
                    painter = painterResource(id = R.drawable.ic_pro),
                    contentDescription = null,
                    modifier =
                            Modifier.align(Alignment.BottomEnd)
                                    .offset(x = 12.dp, y = 32.dp)
                                    .size(165.dp),
                    contentScale = ContentScale.Fit
            )

            // Left Side Content
            Column(
                    modifier =
                            Modifier.fillMaxWidth(0.70f)
                                    .padding(start = 20.dp, top = 16.dp, bottom = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                            text = "Learn without\nlimits with PRO",
                            color = Color(0xFF1E152A),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = Inter,
                            lineHeight = 22.sp
                    )

                    Text(
                            text =
                                    "• Disable ad\n• Access to more special function\n• Ask unlimited question, every day.",
                            color = Color(0xFF475569),
                            fontSize = 10.5.sp,
                            fontFamily = Inter,
                            lineHeight = 14.sp
                    )
                }

                Box(
                        modifier =
                                Modifier.background(Color(0xFF1E152A), RoundedCornerShape(12.dp))
                                        .clickable { onClaimPro() }
                                        .padding(horizontal = 16.dp, vertical = 8.dp),
                        contentAlignment = Alignment.Center
                ) {
                    Text(
                            text = "Claim 14 days free",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = Inter
                    )
                }
            }
        }
    }
}
