package com.eous.mentor.features.friends

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Person
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import com.eous.mentor.R
import com.eous.mentor.core.navigation.navigateSafe
import com.eous.mentor.core.ui.theme.Inter
import com.eous.mentor.domain.model.DashboardStats
import com.eous.mentor.domain.model.Profile

private val PrimaryPurple = Color(0xFF5B29A2)
private val CardBannerPurple = Color(0xFF7F43D4)
private val DarkBorderColor = Color(0xFF1E293B)
private val LightBlueAvatar = Color(0xFFCFD1FF)
private val LightPurpleBg = Color(0xFFD6C7FF)
private val TextDarkGray = Color(0xFF64748B)

@Composable
fun FriendProfileScreen(
    currentUserId: String,
    targetUserId: String,
    navController: NavController,
    viewModel: FriendProfileViewModel = remember(currentUserId, targetUserId) {
        FriendProfileViewModel(currentUserId, targetUserId)
    }
) {
    val state by viewModel.state.collectAsState()
    val scrollState = rememberScrollState()

    if (state.isLoading) {
        com.eous.mentor.core.ui.components.PreparingLoadingScreen()
    } else {
        Box(modifier = Modifier.fillMaxSize()) {
            // Background Image
            Image(
                painter = painterResource(id = R.drawable.profile_background),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.FillBounds
            )

            Box(modifier = Modifier.fillMaxSize()) {
                // Scrollable Content Sheet
                Column(modifier = Modifier.fillMaxSize().verticalScroll(scrollState)) {
                    val profile = state.profile
                    val displayName = profile?.display_name ?: profile?.email?.substringBefore("@") ?: "User"
                    val email = profile?.email ?: ""
                    val initials = displayName.split(" ")
                        .filter { it.isNotEmpty() }
                        .map { it.first().uppercase() }
                        .joinToString("")
                        .take(2)
                        .ifEmpty { "U" }

                    val streak = state.stats?.streak ?: profile?.current_streak ?: 0
                    val totalQueries = state.stats?.totalQueries ?: 0
                    val libraryItems = state.stats?.libraryItems ?: 0
                    val xp = totalQueries * 10 + libraryItems * 20

                    Box(modifier = Modifier.fillMaxWidth()) {
                        // Main White Container Card
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
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
                                text = email,
                                color = Color(0xFF64748B),
                                fontSize = 14.sp,
                                fontFamily = Inter
                            )

                            Spacer(modifier = Modifier.height(20.dp))

                            // Stats Banner Card
                            StatsBannerCard(
                                streak = streak,
                                friendsCount = state.friendsCount
                            )

                            Spacer(modifier = Modifier.height(24.dp))

                            // Friendship Actions Button
                            if (state.actionLoading) {
                                Box(
                                    modifier = Modifier.fillMaxWidth().height(48.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator(color = PrimaryPurple)
                                }
                            } else {
                                FriendshipActionContainer(
                                    currentUserId = currentUserId,
                                    friendship = state.friendship,
                                    onSendRequest = { viewModel.sendFriendRequest() },
                                    onAcceptRequest = { viewModel.acceptFriendRequest() },
                                    onDeclineOrCancel = { viewModel.declineOrRemoveFriendship() }
                                )
                            }

                            Spacer(modifier = Modifier.height(24.dp))

                            // Overview Section
                            OverviewSection(
                                streak = streak,
                                xp = xp
                            )

                            Spacer(modifier = Modifier.height(22.dp))

                            // Badges Section
                            val quizzes = state.stats?.quizzes ?: emptyList()
                            val todayStr = java.time.LocalDate.now().toString()
                            val todayQuizzesCompleted = quizzes.count { it.created_at.startsWith(todayStr) }
                            BadgesSection(streak = streak, todayQuizzesCompleted = todayQuizzesCompleted)

                            Spacer(modifier = Modifier.height(30.dp))
                        }

                        // Avatar Circle
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopCenter)
                                .padding(top = 80.dp)
                                .size(90.dp)
                        ) {
                            val currentAvatarUrl = profile?.avatar_url
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(LightBlueAvatar, CircleShape)
                                    .border(3.dp, PrimaryPurple, CircleShape)
                                    .clip(CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                if (!currentAvatarUrl.isNullOrEmpty()) {
                                    AsyncImage(
                                        model = currentAvatarUrl,
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
                        }
                    }

                    Spacer(modifier = Modifier.height(100.dp))
                }

                // Top Bar with Back Button
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 12.dp, vertical = 12.dp)
                ) {
                    val backInteractionSource = remember { MutableInteractionSource() }
                    val isBackHovered by backInteractionSource.collectIsHoveredAsState()
                    val isBackPressed by backInteractionSource.collectIsPressedAsState()
                    val backScale by animateFloatAsState(
                        targetValue = if (isBackPressed) 0.90f else if (isBackHovered) 1.10f else 1.0f,
                        animationSpec = spring(stiffness = Spring.StiffnessLow),
                        label = "back_scale"
                    )

                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .align(Alignment.TopStart)
                            .graphicsLayer {
                                scaleX = backScale
                                scaleY = backScale
                            }
                            .background(
                                if (isBackHovered || isBackPressed) Color.White.copy(alpha = 0.6f)
                                else Color.White.copy(alpha = 0.3f),
                                CircleShape
                            )
                            .clickable(
                                interactionSource = backInteractionSource,
                                indication = null
                            ) { navController.popBackStack() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = DarkBorderColor,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FriendshipActionContainer(
    currentUserId: String,
    friendship: com.eous.mentor.domain.model.Friendship?,
    onSendRequest: () -> Unit,
    onAcceptRequest: () -> Unit,
    onDeclineOrCancel: () -> Unit
) {
    if (friendship == null) {
        // Not friends, no request sent
        Button(
            onClick = onSendRequest,
            colors = ButtonDefaults.buttonColors(containerColor = LightPurpleBg),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth().height(48.dp)
        ) {
            Text(
                text = "ADD FRIEND",
                color = Color(0xFF4C1D95),
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = Inter
            )
        }
    } else if (friendship.status == "pending") {
        if (friendship.sender_id == currentUserId) {
            // We sent the request, pending recipient response
            Button(
                onClick = onDeclineOrCancel, // Click to cancel request
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF1F5F9)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth().height(48.dp)
            ) {
                Text(
                    text = "PENDING (CANCEL)",
                    color = Color(0xFF475569),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = Inter
                )
            }
        } else {
            // Received request, we can Accept or Decline
            Row(
                modifier = Modifier.fillMaxWidth().height(48.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = onAcceptRequest,
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f).fillMaxHeight()
                ) {
                    Text(
                        text = "ACCEPT",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = Inter
                    )
                }
                OutlinedButton(
                    onClick = onDeclineOrCancel,
                    border = BorderStroke(1.5.dp, Color(0xFFEF4444)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f).fillMaxHeight(),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFEF4444))
                ) {
                    Text(
                        text = "DECLINE",
                        color = Color(0xFFEF4444),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = Inter
                    )
                }
            }
        }
    } else if (friendship.status == "accepted") {
        // Already friends, show Unfriend option
        Button(
            onClick = onDeclineOrCancel, // Unfriend
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFEE2E2)),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth().height(48.dp)
        ) {
            Text(
                text = "UNFRIEND",
                color = Color(0xFFEF4444),
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = Inter
            )
        }
    }
}

@Composable
private fun StatsBannerCard(
    streak: Int,
    friendsCount: Int
) {
    Box(modifier = Modifier.fillMaxWidth()) {
        // Ear accents
        Box(
            modifier = Modifier
                .offset(x = 34.dp, y = (-18).dp)
                .size(24.dp, 26.dp)
                .background(PrimaryPurple, RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
        )
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = (-34).dp, y = (-18).dp)
                .size(24.dp, 26.dp)
                .background(PrimaryPurple, RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
        )

        // Main Card
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
                        modifier = Modifier
                            .weight(1f)
                            .background(Color(0xFF5E27B6), RoundedCornerShape(16.dp))
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
                            label = "ACTIVE DAYS",
                            value = streak.toString()
                        )
                    }

                    // RANKS
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .background(Color(0xFF5E27B6), RoundedCornerShape(16.dp))
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
                            label = "RANKS",
                            value = "#10"
                        )
                    }

                    // FRIENDS
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .background(Color(0xFF5E27B6), RoundedCornerShape(16.dp))
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
                            label = "FRIENDS",
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
            fontSize = 9.sp,
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
private fun OverviewSection(streak: Int, xp: Int) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = "Overview",
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

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Streak Card
            Row(
                modifier = Modifier
                    .weight(1f)
                    .background(Color.White)
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_streak),
                    contentDescription = null,
                    modifier = Modifier.size(36.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "$streak days",
                    color = Color.Black,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = Inter
                )
            }

            // XP Card
            Row(
                modifier = Modifier
                    .weight(1f)
                    .background(Color.White)
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_xp),
                    contentDescription = null,
                    modifier = Modifier.size(36.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "$xp XP",
                    color = Color.Black,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = Inter
                )
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
                contentDescription = null,
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
            colorFilter = if (isUnlocked) null else ColorFilter.colorMatrix(ColorMatrix().apply { setToSaturation(0f) }),
            alpha = if (isUnlocked) 1.0f else 0.45f
        )
    }
}
