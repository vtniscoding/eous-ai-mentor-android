package com.eous.mentor.features.library

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.eous.mentor.R
import com.eous.mentor.core.ui.theme.*
import com.eous.mentor.domain.model.ChatMessage
import com.eous.mentor.domain.model.ChatSession
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

private val SubjectHighlightColor = Color(0xFF5B29A2)

private data class LibraryItem(
    val session: ChatSession,
    val isBookmarked: Boolean,
    val bookmarkedMessage: ChatMessage? = null
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Library(
    userId: String,
    onBack: () -> Unit,
    onNavigateToChatSession: (ChatSession) -> Unit,
    onPracticeClick: (String) -> Unit,
    viewModel: LibraryViewModel = remember { LibraryViewModel() }
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current

    // Load data every time screen enters composition
    LaunchedEffect(Unit) {
        viewModel.loadLibraryData(userId)
    }

    // Show error toast
    LaunchedEffect(state.errorMessage) {
        state.errorMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.clearError()
        }
    }



    // Map sessions and bookmarked messages to library item wrappers
    val libraryItems = remember(state.sessions, state.bookmarkedMessages) {
        state.sessions.map { session ->
            val bookmarkedMsg = state.bookmarkedMessages.firstOrNull { it.session_id == session.id }
            LibraryItem(
                session = session,
                isBookmarked = bookmarkedMsg != null,
                bookmarkedMessage = bookmarkedMsg
            )
        }
    }

    // Filter library items based on selected filter tab and search query
    val filteredItems = remember(libraryItems, state.selectedFilter, state.searchQuery) {
        libraryItems.filter { item ->
            val matchesFilter = when (state.selectedFilter) {
                "All" -> true
                "Marked" -> item.isBookmarked
                else -> item.session.subject.equals(state.selectedFilter, ignoreCase = true)
            }

            val matchesSearch = if (state.searchQuery.isBlank()) {
                true
            } else {
                item.session.title.contains(state.searchQuery, ignoreCase = true)
            }

            matchesFilter && matchesSearch
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            // Header flat bar with ChevronLeft back button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                IconButton(onClick = onBack, modifier = Modifier.size(36.dp)) {
                    Icon(
                        imageVector = Icons.Default.ChevronLeft,
                        contentDescription = "Back",
                        tint = Color.Black,
                        modifier = Modifier.size(28.dp)
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Personal ",
                        color = Color.Black,
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = Inter
                    )
                    Text(
                        text = "Library & History",
                        color = SubjectHighlightColor,
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = Inter
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Scrollable Content
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(bottom = 120.dp)
            ) {
                // Search Bar Question Box
                item {
                    Surface(
                        shape = CircleShape,
                        color = Color.White,
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Search",
                                tint = Color(0xFF94A3B8),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            OutlinedTextField(
                                value = state.searchQuery,
                                onValueChange = { viewModel.setSearchQuery(it) },
                                placeholder = {
                                    Text(
                                        "Search question...",
                                        color = Color(0xFF94A3B8),
                                        fontSize = 14.sp,
                                        fontFamily = Inter
                                    )
                                },
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color.Transparent,
                                    unfocusedBorderColor = Color.Transparent,
                                    focusedTextColor = Color(0xFF0F172A),
                                    unfocusedTextColor = Color(0xFF0F172A)
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }

                // Dynamic Practice Suggestion Card (if count >= 3 and not dismissed)
                if (state.practiceQuestionCount >= 3 && !state.isSuggestionDismissed) {
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFE0D4FF)),
                            shape = RoundedCornerShape(24.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 20.dp, vertical = 18.dp)
                            ) {
                                // Close button at top right
                                IconButton(
                                    onClick = { viewModel.dismissSuggestion() },
                                    modifier = Modifier
                                        .size(24.dp)
                                        .align(Alignment.TopEnd)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Close Gợi ý",
                                        tint = Color(0xFF5B29A2),
                                        modifier = Modifier.size(16.dp)
                                    )
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(
                                        modifier = Modifier.weight(1.3f),
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Text(
                                            text = "Practice suggestion",
                                            color = Color(0xFF5B29A2),
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = Inter
                                        )
                                        Text(
                                            text = buildAnnotatedString {
                                                append("You have asked ")
                                                withStyle(style = SpanStyle(fontWeight = FontWeight.Bold, color = Color(0xFF5B29A2))) {
                                                    append("${state.practiceQuestionCount} questions")
                                                }
                                                append(" about ")
                                                withStyle(style = SpanStyle(fontWeight = FontWeight.Bold, color = Color(0xFF5B29A2))) {
                                                    append(state.practiceSubject)
                                                }
                                                append(" this week. Do a small practice to remember it longer!")
                                            },
                                            color = Color(0xFF475569),
                                            fontSize = 12.sp,
                                            lineHeight = 16.sp,
                                            fontFamily = Inter
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Button(
                                            onClick = { onPracticeClick(state.practiceSubject) },
                                            enabled = !state.hasPracticedToday,
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = SubjectHighlightColor,
                                                disabledContainerColor = Color(0xFFCBD5E1),
                                                disabledContentColor = Color(0xFF94A3B8)
                                            ),
                                            shape = RoundedCornerShape(12.dp),
                                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
                                        ) {
                                            Text(
                                                text = if (state.hasPracticedToday) "You practiced today" else "Practice now (+30xp)",
                                                color = if (state.hasPracticedToday) Color(0xFF94A3B8) else Color.White,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                fontFamily = Inter
                                            )
                                        }
                                    }
                                    Image(
                                        painter = painterResource(id = R.drawable.ic_working_eous),
                                        contentDescription = "Mascot Library",
                                        modifier = Modifier
                                            .weight(0.7f)
                                            .height(95.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // Scrollable Filter Chips row
                item {
                    val filterOptions = remember(state.subjects) {
                        listOf("All", "Marked") + state.subjects
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        filterOptions.forEach { filter ->
                            val isSelected = state.selectedFilter == filter
                            val chipBgColor = if (isSelected) SubjectHighlightColor else Color(0xFFE2E8F0)
                            val chipTxtColor = if (isSelected) Color.White else Color(0xFF1E293B)

                            Surface(
                                onClick = { viewModel.selectFilter(filter) },
                                shape = RoundedCornerShape(20.dp),
                                color = chipBgColor,
                                modifier = Modifier.height(38.dp)
                            ) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier.padding(horizontal = 18.dp)
                                ) {
                                    Text(
                                        text = filter,
                                        color = chipTxtColor,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        fontSize = 13.sp,
                                        fontFamily = Inter
                                    )
                                }
                            }
                        }
                    }
                }

                // List of items
                if (filteredItems.isEmpty()) {
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F5F9)),
                            shape = RoundedCornerShape(20.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(width = 1.dp, color = Color(0xFFE2E8F0), shape = RoundedCornerShape(20.dp))
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.BookmarkBorder,
                                    contentDescription = null,
                                    tint = Color(0xFF94A3B8),
                                    modifier = Modifier.size(36.dp)
                                )
                                Text(
                                    "No questions found matching your filter.",
                                    color = Color(0xFF64748B),
                                    fontSize = 13.sp,
                                    textAlign = TextAlign.Center,
                                    fontFamily = Inter
                                )
                            }
                        }
                    }
                } else {
                    items(filteredItems, key = { it.session.id ?: it.hashCode() }) { item ->
                        LibraryCardItem(
                            item = item,
                            userId = userId,
                            onCardClick = { onNavigateToChatSession(item.session) },
                            onBookmarkToggle = {
                                viewModel.toggleBookmarkFromLibrary(
                                    session = item.session,
                                    isCurrentlyBookmarked = item.isBookmarked,
                                    bookmarkedMsg = item.bookmarkedMessage,
                                    userId = userId
                                )
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LibraryCardItem(
    item: LibraryItem,
    userId: String,
    onCardClick: () -> Unit,
    onBookmarkToggle: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCardClick() }
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Pill tag & timestamp + bookmark toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Subject tag pill
                    Box(
                        modifier = Modifier
                            .background(Color(0xFF1E293B), RoundedCornerShape(8.dp))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = item.session.subject.ifBlank { "General" },
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = Inter
                        )
                    }
                    // Created time label
                    Text(
                        text = formatRelativeTime(item.session.created_at),
                        color = Color(0xFF94A3B8),
                        fontSize = 12.sp,
                        fontFamily = Inter
                    )
                }

                // Bookmark icon button toggler
                IconButton(
                    onClick = onBookmarkToggle,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = if (item.isBookmarked) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                        contentDescription = "Toggle Bookmark",
                        tint = if (item.isBookmarked) SubjectHighlightColor else Color(0xFF94A3B8),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // Question title
            Text(
                text = item.session.title,
                color = Color(0xFF0F172A),
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                fontFamily = Inter,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )

            // View Specs navigation row
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.clickable { onCardClick() }
            ) {
                Text(
                    text = "View specs",
                    color = SubjectHighlightColor,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = Inter
                )
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    tint = SubjectHighlightColor,
                    modifier = Modifier.size(14.dp)
                )
            }
        }
    }
}

private fun formatRelativeTime(createdAt: String?): String {
    if (createdAt.isNullOrEmpty()) return "now"
    try {
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US)
        sdf.timeZone = TimeZone.getTimeZone("UTC")
        val sessionDate: Date = sdf.parse(createdAt.take(19)) ?: return "now"
        val sessionMillis = sessionDate.time
        val nowMillis = System.currentTimeMillis()
        val diffSeconds = ((nowMillis - sessionMillis) / 1000).coerceAtLeast(0)

        return when {
            diffSeconds < 60 -> "Just now"
            diffSeconds < 3600 -> "${diffSeconds / 60}m ago"
            diffSeconds < 86400 -> "${diffSeconds / 3600}h ago"
            diffSeconds < 604800 -> "${diffSeconds / 86400}d ago"
            else -> "${diffSeconds / 604800}w ago"
        }
    } catch (e: Exception) {
        return "now"
    }
}
