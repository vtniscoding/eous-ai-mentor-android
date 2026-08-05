package com.eous.mentor.features.leaderboard

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import com.eous.mentor.R
import com.eous.mentor.core.navigation.navigateSafe
import com.eous.mentor.core.ui.components.PreparingLoadingScreen
import com.eous.mentor.core.ui.theme.Inter

private val PrimaryPurple = Color(0xFF5B29A2)
private val LightBlueAvatar = Color(0xFFCFD1FF)
private val HeaderGray = Color(0xFFECECEC)
private val DividerColor = Color(0xFFE2E8F0)
private val TextDarkGray = Color(0xFF475569)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LeaderboardsScreen(
    userId: String,
    navController: NavController,
    viewModel: LeaderboardsViewModel = remember(userId) { LeaderboardsViewModel(userId) }
) {
    val state by viewModel.state.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        // 1. Background image for the upper portion
        Image(
            painter = painterResource(id = R.drawable.leaderboards_background),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
            alignment = Alignment.TopCenter
        )

        // 2. Main Content Layout
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Top Bar with back button and screen title
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 8.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                        contentDescription = "Back",
                        tint = Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                }
                Text(
                    text = "Leaderboards",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = Inter
                )
            }

            Spacer(modifier = Modifier.weight(1f)) // Push the card down to overlay the background nicely
        }

        // Overlay Card starting from below the trophy, filling the rest of screen
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.68f)
                .align(Alignment.BottomCenter)
        ) {
            if (state.isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Color.White,
                            RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = PrimaryPurple)
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Color.White,
                            RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)
                        )
                ) {
                    if (!state.hasFriends) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 32.dp, vertical = 40.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_badge_chest_hunter),
                                contentDescription = null,
                                tint = PrimaryPurple.copy(alpha = 0.7f),
                                modifier = Modifier.size(80.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "No friends on Leaderboards yet!",
                                color = Color.Black,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = Inter,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Add friends to start competing, comparing XP points, and climbing the ranks together!",
                                color = TextDarkGray,
                                fontSize = 14.sp,
                                fontFamily = Inter,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(24.dp))
                            Button(
                                onClick = { navController.navigateSafe("friends") },
                                colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                            ) {
                                Text(
                                    text = "Add Friends",
                                    color = Color.White,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = Inter
                                )
                            }
                        }
                    } else {
                        // Header Bar (Rankings | XP Points)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    HeaderGray,
                                    RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)
                                )
                                .padding(horizontal = 24.dp, vertical = 16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                        Text(
                            text = "Rankings",
                            color = TextDarkGray,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = Inter
                        )
                        Text(
                            text = "XP Points",
                            color = TextDarkGray,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = Inter
                        )
                    }

                    // Rankings List
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 20.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        itemsIndexed(state.entries) { index, entry ->
                            val rank = index + 1
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // 1. Rank Number
                                Text(
                                    text = rank.toString(),
                                    color = if (rank == 1) PrimaryPurple else TextDarkGray,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = Inter,
                                    modifier = Modifier.width(36.dp),
                                    textAlign = TextAlign.Start
                                )

                                // 2. Avatar
                                Box(
                                    modifier = Modifier
                                        .size(50.dp)
                                        .background(LightBlueAvatar, CircleShape)
                                        .clip(CircleShape)
                                        .clickable(enabled = !entry.isCurrentUser) {
                                            navController.navigateSafe("friend_profile/${entry.id}")
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (!entry.avatarUrl.isNullOrEmpty()) {
                                        AsyncImage(
                                            model = entry.avatarUrl,
                                            contentDescription = "User Avatar",
                                            modifier = Modifier.fillMaxSize(),
                                            contentScale = ContentScale.Crop
                                        )
                                    } else {
                                        Text(
                                            text = entry.initials,
                                            color = PrimaryPurple,
                                            fontSize = 18.sp,
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = Inter
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.width(16.dp))

                                // 3. Display Name
                                Text(
                                    text = entry.name,
                                    color = Color.Black,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Medium,
                                    fontFamily = Inter,
                                    modifier = Modifier.weight(1f)
                                )

                                // 4. XP Points
                                Text(
                                    text = entry.xp.toString(),
                                    color = PrimaryPurple,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = Inter
                                )
                            }

                            if (index < state.entries.lastIndex) {
                                HorizontalDivider(
                                    color = DividerColor,
                                    thickness = 1.dp,
                                    modifier = Modifier.padding(start = 36.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
}
