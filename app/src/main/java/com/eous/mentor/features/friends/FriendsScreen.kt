package com.eous.mentor.features.friends

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import com.eous.mentor.core.ui.theme.Inter
import com.eous.mentor.domain.model.Profile
import com.eous.mentor.domain.model.Friendship
import com.eous.mentor.domain.model.FriendshipWithProfile
import com.eous.mentor.core.navigation.navigateSafe

private val PrimaryPurple = Color(0xFF5B29A2)
private val LightBlueAvatar = Color(0xFFCFD1FF)
private val DividerColor = Color(0xFFE2E8F0)
private val TextDarkGray = Color(0xFF475569)
private val LightPurpleBg = Color(0xFFF1F0FF)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FriendsScreen(
    userId: String,
    navController: NavController,
    viewModel: FriendsViewModel = remember(userId) { FriendsViewModel(userId) },
    initialTab: Int = 0
) {
    val state by viewModel.state.collectAsState()
    val context = androidx.compose.ui.platform.LocalContext.current
    var selectedTabIndex by remember { mutableStateOf(initialTab) }
    val tabs = listOf("My Friends", "Add Friends", "Requests")

    LaunchedEffect(selectedTabIndex) {
        viewModel.loadData(context)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // 1. Top Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(PrimaryPurple)
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
                    text = "Friends",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = Inter
                )
            }

            // 2. Tab Bar
            TabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = Color.White,
                contentColor = PrimaryPurple,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                        color = PrimaryPurple
                    )
                }
            ) {
                tabs.forEachIndexed { index, title ->
                    val badgeCount = when (index) {
                        2 -> state.pendingRequests.size
                        else -> 0
                    }
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = title,
                                    fontFamily = Inter,
                                    fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Normal,
                                    fontSize = 14.sp
                                )
                                if (badgeCount > 0) {
                                    Box(
                                        modifier = Modifier
                                            .background(Color.Red, CircleShape)
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = badgeCount.toString(),
                                            color = Color.White,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    )
                }
            }

            // 3. Tab Content
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                if (state.isLoading && selectedTabIndex != 1) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = PrimaryPurple)
                    }
                } else {
                    when (selectedTabIndex) {
                        0 -> MyFriendsTab(
                            friends = state.friends,
                            navController = navController,
                            onUnfriend = { viewModel.declineOrRemoveFriendship(it.id) }
                        )
                        1 -> AddFriendsTab(
                            searchResults = state.searchResults,
                            searchQuery = state.searchQuery,
                            allFriendships = state.allFriendships,
                            userId = userId,
                            isSearching = state.isSearching,
                            navController = navController,
                            suggestedUsers = state.suggestedUsers,
                            onSearchQueryChange = { viewModel.searchUsers(it) },
                            onAddFriend = { viewModel.sendFriendRequest(it) },
                            onAcceptFriend = { viewModel.acceptFriendRequest(it) },
                            onDismissSuggested = { viewModel.dismissSuggested(it) },
                            onAddSuggestedFriend = { viewModel.addSuggestedFriend(it) }
                        )
                        2 -> RequestsTab(
                            requests = state.pendingRequests,
                            navController = navController,
                            onAccept = { viewModel.acceptFriendRequest(it) },
                            onDecline = { viewModel.declineOrRemoveFriendship(it) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MyFriendsTab(
    friends: List<Profile>,
    navController: NavController,
    onUnfriend: (Profile) -> Unit
) {
    if (friends.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                text = "No friends yet.\nGo to 'Add Friends' to connect!",
                textAlign = TextAlign.Center,
                color = TextDarkGray,
                fontSize = 15.sp,
                fontFamily = Inter
            )
        }
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(friends) { friend ->
                val name = friend.display_name ?: friend.email?.substringBefore("@") ?: "User"
                val initial = name.trim().split("\\s+".toRegex()).lastOrNull()?.firstOrNull()?.uppercase() ?: "U"

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .background(LightBlueAvatar, CircleShape)
                            .clip(CircleShape)
                            .clickable { navController.navigateSafe("friend_profile/${friend.id}") },
                        contentAlignment = Alignment.Center
                    ) {
                        if (!friend.avatar_url.isNullOrEmpty()) {
                            AsyncImage(
                                model = friend.avatar_url,
                                contentDescription = "Avatar",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Text(
                                text = initial,
                                color = PrimaryPurple,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = Inter
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = name,
                            color = Color.Black,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = Inter
                        )
                        Text(
                            text = friend.email ?: "",
                            color = TextDarkGray,
                            fontSize = 12.sp,
                            fontFamily = Inter
                        )
                    }

                    TextButton(
                        onClick = { onUnfriend(friend) },
                        colors = ButtonDefaults.textButtonColors(contentColor = Color.Red)
                    ) {
                        Text("Unfriend", fontSize = 13.sp, fontWeight = FontWeight.Bold, fontFamily = Inter)
                    }
                }

                HorizontalDivider(color = DividerColor, thickness = 1.dp)
            }
        }
    }
}

@Composable
private fun AddFriendsTab(
    searchResults: List<Profile>,
    searchQuery: String,
    allFriendships: List<Friendship>,
    userId: String,
    isSearching: Boolean,
    navController: NavController,
    suggestedUsers: List<SuggestedUser>,
    onSearchQueryChange: (String) -> Unit,
    onAddFriend: (String) -> Unit,
    onAcceptFriend: (String) -> Unit,
    onDismissSuggested: (String) -> Unit,
    onAddSuggestedFriend: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchQueryChange,
            placeholder = { Text("Search by email or name...", color = TextDarkGray, fontSize = 14.sp) },
            leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = null, tint = TextDarkGray) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = PrimaryPurple,
                unfocusedBorderColor = DividerColor
            ),
            singleLine = true
        )

        if (isSearching) {
            Box(modifier = Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = PrimaryPurple)
            }
        } else if (searchQuery.trim().isNotEmpty()) {
            if (searchResults.isEmpty()) {
                Box(modifier = Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                    Text(
                        text = "No users found.",
                        color = TextDarkGray,
                        fontSize = 14.sp,
                        fontFamily = Inter
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(searchResults) { user ->
                        val name = user.display_name ?: user.email?.substringBefore("@") ?: "User"
                        val initial = name.trim().split("\\s+".toRegex()).lastOrNull()?.firstOrNull()?.uppercase() ?: "U"

                        // Xác định trạng thái kết bạn
                        val relation = allFriendships.find {
                            (it.sender_id == userId && it.receiver_id == user.id) ||
                            (it.sender_id == user.id && it.receiver_id == userId)
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(50.dp)
                                    .background(LightBlueAvatar, CircleShape)
                                    .clip(CircleShape)
                                    .clickable { navController.navigateSafe("friend_profile/${user.id}") },
                                contentAlignment = Alignment.Center
                            ) {
                                if (!user.avatar_url.isNullOrEmpty()) {
                                    AsyncImage(
                                        model = user.avatar_url,
                                        contentDescription = "Avatar",
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                } else {
                                    Text(
                                        text = initial,
                                        color = PrimaryPurple,
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = Inter
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(16.dp))

                            Column(
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = name,
                                    color = Color.Black,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = Inter
                                )
                                Text(
                                    text = user.email ?: "",
                                    color = TextDarkGray,
                                    fontSize = 12.sp,
                                    fontFamily = Inter
                                )
                            }

                            if (relation == null) {
                                Button(
                                    onClick = { onAddFriend(user.id) },
                                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text("Add Friend", fontSize = 12.sp, fontWeight = FontWeight.Bold, fontFamily = Inter)
                                }
                            } else if (relation.status == "accepted") {
                                Text(
                                    text = "Friend",
                                    color = TextDarkGray,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = Inter,
                                    modifier = Modifier.padding(horizontal = 8.dp)
                                )
                            } else if (relation.status == "pending") {
                                if (relation.sender_id == userId) {
                                    Button(
                                        onClick = {},
                                        enabled = false,
                                        colors = ButtonDefaults.buttonColors(
                                            disabledContainerColor = DividerColor,
                                            disabledContentColor = TextDarkGray
                                        ),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text("Pending", fontSize = 12.sp, fontWeight = FontWeight.Bold, fontFamily = Inter)
                                    }
                                } else {
                                    Button(
                                        onClick = { onAcceptFriend(user.id) },
                                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text("Accept", fontSize = 12.sp, fontWeight = FontWeight.Bold, fontFamily = Inter)
                                    }
                                }
                            }
                        }

                        HorizontalDivider(color = DividerColor, thickness = 1.dp)
                    }
                }
            }
        } else {
            // Hiển thị Someone you may know dạng danh sách kiểu mạng xã hội
            Column(modifier = Modifier.fillMaxWidth().weight(1f)) {
                Text(
                    text = "Someone you may know",
                    color = Color.Black,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = Inter,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                if (suggestedUsers.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No suggestions available", color = TextDarkGray, fontSize = 14.sp, fontFamily = Inter)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth().weight(1f),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(suggestedUsers, key = { it.id }) { user ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color(0xFFF8FAFC), RoundedCornerShape(16.dp))
                                    .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(16.dp))
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(46.dp)
                                        .background(LightBlueAvatar, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = user.avatarLetter,
                                        color = PrimaryPurple,
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = Inter
                                    )
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = user.name,
                                        color = Color.Black,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = Inter
                                    )
                                    Text(
                                        text = user.email,
                                        color = TextDarkGray,
                                        fontSize = 11.sp,
                                        fontFamily = Inter
                                    )
                                }

                                Button(
                                    onClick = { onAddSuggestedFriend(user.id) },
                                    enabled = !user.isAdded,
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (user.isAdded) Color(0xFF94A3B8) else PrimaryPurple,
                                        disabledContainerColor = Color(0xFF94A3B8)
                                    ),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.height(32.dp),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp)
                                ) {
                                    Text(
                                        text = if (user.isAdded) "Requested" else "Add",
                                        color = Color.White,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = Inter
                                    )
                                }

                                Spacer(modifier = Modifier.width(8.dp))

                                IconButton(
                                    onClick = { onDismissSuggested(user.id) },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Dismiss",
                                        tint = TextDarkGray,
                                        modifier = Modifier.size(16.dp)
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

@Composable
private fun RequestsTab(
    requests: List<FriendshipWithProfile>,
    navController: NavController,
    onAccept: (String) -> Unit,
    onDecline: (String) -> Unit
) {
    if (requests.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                text = "No pending requests.",
                color = TextDarkGray,
                fontSize = 15.sp,
                fontFamily = Inter
            )
        }
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(requests) { request ->
                val sender = request.sender
                val name = sender?.display_name ?: sender?.email?.substringBefore("@") ?: "User"
                val initial = name.trim().split("\\s+".toRegex()).lastOrNull()?.firstOrNull()?.uppercase() ?: "U"

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .background(LightBlueAvatar, CircleShape)
                            .clip(CircleShape)
                            .clickable {
                                if (sender != null) {
                                    navController.navigateSafe("friend_profile/${sender.id}")
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        if (!sender?.avatar_url.isNullOrEmpty()) {
                            AsyncImage(
                                model = sender.avatar_url,
                                contentDescription = "Avatar",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Text(
                                text = initial,
                                color = PrimaryPurple,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = Inter
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = name,
                            color = Color.Black,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = Inter
                        )
                        Text(
                            text = sender?.email ?: "",
                            color = TextDarkGray,
                            fontSize = 12.sp,
                            fontFamily = Inter
                        )
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Button(
                            onClick = { onAccept(request.sender_id) },
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                            modifier = Modifier.height(32.dp)
                        ) {
                            Text("Accept", fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = Inter)
                        }

                        Button(
                            onClick = { onDecline(request.sender_id) },
                            colors = ButtonDefaults.buttonColors(containerColor = DividerColor, contentColor = Color.Red),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                            modifier = Modifier.height(32.dp)
                        ) {
                            Text("Decline", fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = Inter)
                        }
                    }
                }

                HorizontalDivider(color = DividerColor, thickness = 1.dp)
            }
        }
    }
}
