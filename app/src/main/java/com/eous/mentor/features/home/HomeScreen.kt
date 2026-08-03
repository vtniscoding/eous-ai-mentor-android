package com.eous.mentor.features.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.eous.mentor.R
import com.eous.mentor.core.ui.theme.*
import java.time.LocalDate

@Composable
fun HomeScreen(
        userId: String,
        navController: NavController? = null,
        onNavigateToTab: ((String) -> Unit)? = null,
        viewModel: HomeViewModel = remember(userId) { HomeViewModel(userId) }
) {
        val context = LocalContext.current
        val state by viewModel.state.collectAsState()
        val stats = state.stats

        LaunchedEffect(Unit) { viewModel.loadDashboardStats() }

        val cardShadowColor = Color(0xFF1E293B)
        val purpleContainerBg = Color(0xFF5E27B6)

        Surface(modifier = Modifier.fillMaxSize(), color = Color(0xFFFAFAFA)) {
                if (state.isLoading) {
                        com.eous.mentor.core.ui.components.PreparingLoadingScreen()
                } else {
                        Box(modifier = Modifier.fillMaxSize()) {
                                Column(
                                        modifier =
                                                Modifier.fillMaxSize()
                                                        .verticalScroll(rememberScrollState())
                                ) {
                                        // 1. TOP HEADER (Seamless transparent background, matching
                                        // Hero card alignment)
                                        Row(
                                                modifier =
                                                        Modifier.fillMaxWidth()
                                                                .statusBarsPadding()
                                                                .padding(
                                                                        start = 16.dp,
                                                                        end = 16.dp,
                                                                        top = 12.dp,
                                                                        bottom = 12.dp
                                                                ),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                        ) {
                                                // Left: User Avatar + Name + Level
                                                Row(
                                                        verticalAlignment =
                                                                Alignment.CenterVertically,
                                                        horizontalArrangement =
                                                                Arrangement.spacedBy(10.dp)
                                                ) {
                                                        val initials =
                                                                stats.displayName
                                                                        .split(" ")
                                                                        .filter { it.isNotEmpty() }
                                                                        .map {
                                                                                it.first()
                                                                                        .uppercase()
                                                                        }
                                                                        .joinToString("")
                                                                        .take(2)
                                                                        .ifEmpty { "A" }

                                                        // Avatar without border
                                                        Box(
                                                                modifier =
                                                                        Modifier.size(44.dp)
                                                                                .background(
                                                                                        Color(
                                                                                                0xFFCFD1FF
                                                                                        ),
                                                                                        CircleShape
                                                                                )
                                                                                .clip(CircleShape),
                                                                contentAlignment = Alignment.Center
                                                        ) {
                                                                if (!stats.avatarUrl.isNullOrEmpty()
                                                                ) {
                                                                        coil3.compose.AsyncImage(
                                                                                model =
                                                                                        stats.avatarUrl,
                                                                                contentDescription =
                                                                                        "Avatar Image",
                                                                                contentScale =
                                                                                        ContentScale
                                                                                                .Crop,
                                                                                modifier =
                                                                                        Modifier.fillMaxSize()
                                                                                                .clip(
                                                                                                        CircleShape
                                                                                                )
                                                                        )
                                                                } else {
                                                                        Text(
                                                                                text = initials,
                                                                                color =
                                                                                        Color(
                                                                                                0xFF5B29A2
                                                                                        ),
                                                                                fontSize = 17.sp,
                                                                                fontWeight =
                                                                                        FontWeight
                                                                                                .Bold,
                                                                                fontFamily = Inter
                                                                        )
                                                                }
                                                        }

                                                        Column(
                                                                verticalArrangement =
                                                                        Arrangement.spacedBy(2.dp)
                                                        ) {
                                                                Text(
                                                                        text = stats.displayName,
                                                                        color = Color.Black,
                                                                        fontSize = 16.5.sp,
                                                                        fontFamily = Inter,
                                                                        fontWeight = FontWeight.Bold,
                                                                        lineHeight = 20.sp
                                                                )
                                                                Text(
                                                                        text =
                                                                                "Level: ${stats.educationLevel}",
                                                                        color = Color(0xFF64748B),
                                                                        fontSize = 12.sp,
                                                                        fontFamily = Inter,
                                                                        fontWeight =
                                                                                FontWeight.Normal,
                                                                        lineHeight = 16.sp
                                                                )
                                                        }
                                                }

                                                // Right: Dynamic Streak & XP Indicators (Optimized spacing for 3-4 digit numbers)
                                                Row(
                                                        verticalAlignment =
                                                                Alignment.CenterVertically,
                                                        horizontalArrangement =
                                                                Arrangement.spacedBy(10.dp)
                                                ) {
                                                        // Dynamic Streak
                                                        Row(
                                                                verticalAlignment =
                                                                        Alignment.CenterVertically,
                                                                horizontalArrangement =
                                                                        Arrangement.spacedBy(3.dp)
                                                        ) {
                                                                Image(
                                                                        painter =
                                                                                painterResource(
                                                                                        id =
                                                                                                R.drawable
                                                                                                        .ic_streak
                                                                                ),
                                                                        contentDescription =
                                                                                "Streak Icon",
                                                                        modifier =
                                                                                Modifier.size(22.dp)
                                                                )
                                                                Text(
                                                                        text = "${stats.streak}",
                                                                        color = Color(0xFFFA6938),
                                                                        fontSize = 15.5.sp,
                                                                        fontWeight =
                                                                                FontWeight.Bold,
                                                                        fontFamily = Inter
                                                                )
                                                        }

                                                        // Dynamic XP
                                                        Row(
                                                                verticalAlignment =
                                                                        Alignment.CenterVertically,
                                                                horizontalArrangement =
                                                                        Arrangement.spacedBy(3.dp)
                                                        ) {
                                                                Image(
                                                                        painter =
                                                                                painterResource(
                                                                                        id =
                                                                                                R.drawable
                                                                                                        .ic_xp
                                                                                ),
                                                                        contentDescription =
                                                                                "XP Icon",
                                                                        modifier =
                                                                                Modifier.size(24.dp)
                                                                )
                                                                Text(
                                                                        text = "${stats.xp}",
                                                                        color = Color(0xFF6C3FEA),
                                                                        fontSize = 16.sp,
                                                                        fontWeight =
                                                                                FontWeight.Bold,
                                                                        fontFamily = Inter
                                                                )
                                                        }
                                                }
                                        }

                                        Spacer(modifier = Modifier.height(12.dp))

                                        // 2. HERO BANNER CARD (#D2CAFF background, purple title,
                                        // drop shadow)
                                        Box(
                                                modifier =
                                                        Modifier.fillMaxWidth()
                                                                .padding(horizontal = 16.dp)
                                                                .shadow(
                                                                        elevation = 6.dp,
                                                                        shape =
                                                                                RoundedCornerShape(
                                                                                        24.dp
                                                                                )
                                                                )
                                                                .background(
                                                                        Color(0xFFD2CAFF),
                                                                        RoundedCornerShape(24.dp)
                                                                )
                                                                .clip(RoundedCornerShape(24.dp))
                                        ) {
                                                Row(
                                                        modifier =
                                                                Modifier.fillMaxWidth()
                                                                        .padding(
                                                                                start = 18.dp,
                                                                                top = 16.dp,
                                                                                bottom = 16.dp,
                                                                                end = 120.dp
                                                                        ),
                                                        verticalAlignment =
                                                                Alignment.CenterVertically
                                                ) {
                                                        Column(
                                                                modifier = Modifier.fillMaxWidth(),
                                                                verticalArrangement =
                                                                        Arrangement.spacedBy(2.dp)
                                                        ) {
                                                                Text(
                                                                        text =
                                                                                "Having trouble with\nyour studies?",
                                                                        color = Color(0xFF3A1078),
                                                                        fontSize = 18.sp,
                                                                        fontFamily = Inter,
                                                                        fontWeight =
                                                                                FontWeight.Bold,
                                                                        lineHeight = 22.sp
                                                                )
                                                                Text(
                                                                        text =
                                                                                "Don't worry, Eous can help you with any subject. Feel free to ask me anytime!",
                                                                        color = Color(0xFF4A4A4A),
                                                                        fontSize = 12.5.sp,
                                                                        fontFamily = Inter,
                                                                        fontWeight =
                                                                                FontWeight.Normal,
                                                                        lineHeight = 17.sp
                                                                )

                                                                Spacer(
                                                                        modifier =
                                                                                Modifier.height(
                                                                                        4.dp
                                                                                )
                                                                )

                                                                // "Get Started" button
                                                                Box(
                                                                        modifier =
                                                                                Modifier.background(
                                                                                                Color(
                                                                                                        0xFF222222
                                                                                                ),
                                                                                                CircleShape
                                                                                        )
                                                                                        .clickable {
                                                                                                onNavigateToTab
                                                                                                        ?.invoke(
                                                                                                                "chat"
                                                                                                        )
                                                                                        }
                                                                                        .padding(
                                                                                                horizontal =
                                                                                                        16.dp,
                                                                                                vertical =
                                                                                                        8.dp
                                                                                        )
                                                                ) {
                                                                        Text(
                                                                                text =
                                                                                        "Get Started",
                                                                                color = Color.White,
                                                                                fontSize = 13.sp,
                                                                                fontWeight =
                                                                                        FontWeight
                                                                                                .Bold,
                                                                                fontFamily = Inter
                                                                        )
                                                                }
                                                        }
                                                }

                                                // Right Mascot Illustration
                                                Image(
                                                        painter =
                                                                painterResource(
                                                                        id = R.drawable.ic_home_1
                                                                ),
                                                        contentDescription = "Eous Mascot",
                                                        contentScale = ContentScale.Fit,
                                                        modifier =
                                                                Modifier.height(150.dp)
                                                                        .align(Alignment.BottomEnd)
                                                                        .offset(
                                                                                x = 16.dp,
                                                                                y = 20.dp
                                                                        )
                                                )
                                        }

                                        Spacer(modifier = Modifier.height(20.dp))

                                        // 3. QUICK ACCESS ACTION GRID (#D2CAFF background, no
                                        // shadow, medium font
                                        // weight)
                                        Row(
                                                modifier =
                                                        Modifier.fillMaxWidth()
                                                                .padding(horizontal = 16.dp),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                        ) {
                                                QuickAccessItem(
                                                        title = "Practice",
                                                        icon = Icons.Outlined.Extension,
                                                        onClick = {
                                                                onNavigateToTab?.invoke(
                                                                        "tools_quizzes"
                                                                )
                                                        },
                                                        modifier = Modifier.weight(1f)
                                                )
                                                QuickAccessItem(
                                                        title = "Progress",
                                                        icon = Icons.Outlined.BarChart,
                                                        onClick = {
                                                                onNavigateToTab?.invoke("progress")
                                                        },
                                                        modifier = Modifier.weight(1f)
                                                )
                                                QuickAccessItem(
                                                        title = "Flashcard",
                                                        icon = Icons.Outlined.Style,
                                                        onClick = {
                                                                onNavigateToTab?.invoke(
                                                                        "tools_flashcards"
                                                                )
                                                        },
                                                        modifier = Modifier.weight(1f)
                                                )
                                                QuickAccessItem(
                                                        title = "Focus",
                                                        icon = Icons.Outlined.Timer,
                                                        onClick = {
                                                                onNavigateToTab?.invoke(
                                                                        "tools_timer"
                                                                )
                                                        },
                                                        modifier = Modifier.weight(1f)
                                                )
                                        }

                                        Spacer(modifier = Modifier.height(20.dp))

                                        // 4. PURPLE CONTAINER (InsetBounds matching Hero card
                                        // width)
                                        Box(
                                                modifier =
                                                        Modifier.fillMaxWidth()
                                                                .padding(horizontal = 16.dp)
                                                                .shadow(
                                                                        elevation = 4.dp,
                                                                        shape =
                                                                                RoundedCornerShape(
                                                                                        28.dp
                                                                                )
                                                                )
                                                                .background(
                                                                        color = purpleContainerBg,
                                                                        shape =
                                                                                RoundedCornerShape(
                                                                                        28.dp
                                                                                )
                                                                )
                                                                .clip(RoundedCornerShape(28.dp))
                                                                .padding(
                                                                        horizontal = 16.dp,
                                                                        vertical = 20.dp
                                                                )
                                        ) {
                                                Column(
                                                        verticalArrangement =
                                                                Arrangement.spacedBy(22.dp),
                                                        modifier = Modifier.fillMaxWidth()
                                                ) {
                                                        // BUILD YOUR STREAK SECTION
                                                        Column(
                                                                verticalArrangement =
                                                                        Arrangement.spacedBy(0.dp),
                                                                modifier = Modifier.fillMaxWidth()
                                                        ) {
                                                                Text(
                                                                        text = "Build Your Streak!",
                                                                        color = Color.White,
                                                                        fontSize = 18.sp,
                                                                        fontWeight =
                                                                                FontWeight.Bold,
                                                                        fontFamily = Inter,
                                                                        modifier =
                                                                                Modifier.padding(
                                                                                        horizontal =
                                                                                                4.dp
                                                                                )
                                                                )
                                                                Text(
                                                                        text =
                                                                                "Get started to gain XP and earn rewards.",
                                                                        color =
                                                                                Color.White.copy(
                                                                                        alpha =
                                                                                                0.92f
                                                                                ),
                                                                        fontSize = 12.5.sp,
                                                                        fontFamily = Inter,
                                                                        modifier =
                                                                                Modifier.padding(
                                                                                        horizontal =
                                                                                                4.dp
                                                                                )
                                                                )

                                                                Spacer(
                                                                        modifier =
                                                                                Modifier.height(
                                                                                        10.dp
                                                                                )
                                                                )

                                                                StreakTrackerBar(
                                                                        currentStreak =
                                                                                stats.streak,
                                                                        modifier =
                                                                                Modifier.fillMaxWidth()
                                                                )
                                                        }

                                                        // DAILY TASK SECTION
                                                        val todayStr = remember {
                                                                LocalDate.now().toString()
                                                        }
                                                        val todayQuizzesCompleted =
                                                                remember(stats.quizzes) {
                                                                        stats.quizzes
                                                                                .count {
                                                                                        it.created_at
                                                                                                .startsWith(
                                                                                                        todayStr
                                                                                                )
                                                                                }
                                                                                .coerceAtMost(5)
                                                                }
                                                        val todayStudyMinutes =
                                                                (todayQuizzesCompleted * 6)
                                                                        .coerceAtMost(30)

                                                        Column(
                                                                verticalArrangement =
                                                                        Arrangement.spacedBy(10.dp),
                                                                modifier = Modifier.fillMaxWidth()
                                                        ) {
                                                                // Title & Subtext outside white
                                                                // card
                                                                Column(
                                                                        verticalArrangement =
                                                                                Arrangement
                                                                                        .spacedBy(
                                                                                                0.dp
                                                                                        ),
                                                                        modifier =
                                                                                Modifier.fillMaxWidth()
                                                                ) {
                                                                        Text(
                                                                                text = "Daily Tasks",
                                                                                color = Color.White,
                                                                                fontSize = 18.sp,
                                                                                fontWeight =
                                                                                        FontWeight
                                                                                                .Bold,
                                                                                fontFamily = Inter,
                                                                                modifier =
                                                                                        Modifier.padding(
                                                                                                horizontal =
                                                                                                        4.dp
                                                                                        )
                                                                        )
                                                                        Text(
                                                                                text =
                                                                                        "Complete daily tasks to earn rewards.",
                                                                                color =
                                                                                        Color.White
                                                                                                .copy(
                                                                                                        alpha =
                                                                                                                0.92f
                                                                                                ),
                                                                                fontSize = 12.5.sp,
                                                                                fontFamily = Inter,
                                                                                modifier =
                                                                                        Modifier.padding(
                                                                                                horizontal =
                                                                                                        4.dp
                                                                                        )
                                                                        )
                                                                }

                                                                // White Daily Task Card
                                                                Box(
                                                                        modifier =
                                                                                Modifier.fillMaxWidth()
                                                                                        .shadow(
                                                                                                elevation =
                                                                                                        4.dp,
                                                                                                shape =
                                                                                                        RoundedCornerShape(
                                                                                                                22.dp
                                                                                                        )
                                                                                        )
                                                                                        .background(
                                                                                                Color.White,
                                                                                                RoundedCornerShape(
                                                                                                        22.dp
                                                                                                )
                                                                                        )
                                                                                        .padding(
                                                                                                horizontal =
                                                                                                        16.dp,
                                                                                                vertical =
                                                                                                        14.dp
                                                                                        )
                                                                ) {
                                                                        Row(
                                                                                modifier =
                                                                                        Modifier.fillMaxWidth(),
                                                                                horizontalArrangement =
                                                                                        Arrangement
                                                                                                .SpaceBetween,
                                                                                verticalAlignment =
                                                                                        Alignment
                                                                                                .CenterVertically
                                                                        ) {
                                                                                Column(
                                                                                        modifier =
                                                                                                Modifier.weight(
                                                                                                        1.3f
                                                                                                ),
                                                                                        verticalArrangement =
                                                                                                Arrangement
                                                                                                        .spacedBy(
                                                                                                                8.dp
                                                                                                        )
                                                                                ) {
                                                                                        // Task 1:
                                                                                        // Take 5
                                                                                        // quizzes
                                                                                        Column(
                                                                                                verticalArrangement =
                                                                                                        Arrangement
                                                                                                                .spacedBy(
                                                                                                                        3.dp
                                                                                                                )
                                                                                        ) {
                                                                                                Text(
                                                                                                        text =
                                                                                                                "Take 5 quizzes",
                                                                                                        color =
                                                                                                                Color.Black,
                                                                                                        fontSize =
                                                                                                                13.sp,
                                                                                                        fontWeight =
                                                                                                                FontWeight
                                                                                                                        .Bold,
                                                                                                        fontFamily =
                                                                                                                Inter
                                                                                                )

                                                                                                val progressFraction =
                                                                                                        (todayQuizzesCompleted /
                                                                                                                        5f)
                                                                                                                .coerceIn(
                                                                                                                        0f,
                                                                                                                        1f
                                                                                                                )
                                                                                                Box(
                                                                                                        modifier =
                                                                                                                Modifier.fillMaxWidth()
                                                                                                                        .height(
                                                                                                                                20.dp
                                                                                                                        )
                                                                                                                        .background(
                                                                                                                                Color(
                                                                                                                                        0xFFE2E2E2
                                                                                                                                ),
                                                                                                                                CircleShape
                                                                                                                        )
                                                                                                                        .clip(
                                                                                                                                CircleShape
                                                                                                                        ),
                                                                                                        contentAlignment =
                                                                                                                Alignment
                                                                                                                        .Center
                                                                                                ) {
                                                                                                        if (progressFraction >
                                                                                                                        0f
                                                                                                        ) {
                                                                                                                Box(
                                                                                                                        modifier =
                                                                                                                                Modifier.fillMaxWidth(
                                                                                                                                                progressFraction
                                                                                                                                        )
                                                                                                                                        .fillMaxHeight()
                                                                                                                                        .align(
                                                                                                                                                Alignment
                                                                                                                                                        .CenterStart
                                                                                                                                        )
                                                                                                                                        .background(
                                                                                                                                                Color(
                                                                                                                                                        0xFF8052EC
                                                                                                                                                ),
                                                                                                                                                CircleShape
                                                                                                                                        )
                                                                                                                )
                                                                                                        }
                                                                                                        val textColor =
                                                                                                                if (progressFraction >=
                                                                                                                                0.5f
                                                                                                                )
                                                                                                                        Color.White
                                                                                                                else
                                                                                                                        Color(
                                                                                                                                0xFF6C757D
                                                                                                                        )
                                                                                                        Text(
                                                                                                                text =
                                                                                                                        "$todayQuizzesCompleted/5",
                                                                                                                color =
                                                                                                                        textColor,
                                                                                                                fontSize =
                                                                                                                        11.sp,
                                                                                                                fontWeight =
                                                                                                                        FontWeight
                                                                                                                                .Bold,
                                                                                                                fontFamily =
                                                                                                                        Inter,
                                                                                                                lineHeight =
                                                                                                                        20.sp,
                                                                                                                textAlign =
                                                                                                                        TextAlign
                                                                                                                                .Center,
                                                                                                                modifier =
                                                                                                                        Modifier.fillMaxWidth()
                                                                                                        )
                                                                                                }
                                                                                        }

                                                                                        // Task 2:
                                                                                        // Study for
                                                                                        // 30
                                                                                        // minutes
                                                                                        Column(
                                                                                                verticalArrangement =
                                                                                                        Arrangement
                                                                                                                .spacedBy(
                                                                                                                        3.dp
                                                                                                                )
                                                                                        ) {
                                                                                                Text(
                                                                                                        text =
                                                                                                                "Study for 30 minutes",
                                                                                                        color =
                                                                                                                Color.Black,
                                                                                                        fontSize =
                                                                                                                13.sp,
                                                                                                        fontWeight =
                                                                                                                FontWeight
                                                                                                                        .Bold,
                                                                                                        fontFamily =
                                                                                                                Inter
                                                                                                )

                                                                                                val progressFraction =
                                                                                                        (todayStudyMinutes /
                                                                                                                        30f)
                                                                                                                .coerceIn(
                                                                                                                        0f,
                                                                                                                        1f
                                                                                                                )
                                                                                                Box(
                                                                                                        modifier =
                                                                                                                Modifier.fillMaxWidth()
                                                                                                                        .height(
                                                                                                                                20.dp
                                                                                                                        )
                                                                                                                        .background(
                                                                                                                                Color(
                                                                                                                                        0xFFE2E2E2
                                                                                                                                ),
                                                                                                                                CircleShape
                                                                                                                        )
                                                                                                                        .clip(
                                                                                                                                CircleShape
                                                                                                                        ),
                                                                                                        contentAlignment =
                                                                                                                Alignment
                                                                                                                        .Center
                                                                                                ) {
                                                                                                        if (progressFraction >
                                                                                                                        0f
                                                                                                        ) {
                                                                                                                Box(
                                                                                                                        modifier =
                                                                                                                                Modifier.fillMaxWidth(
                                                                                                                                                progressFraction
                                                                                                                                        )
                                                                                                                                        .fillMaxHeight()
                                                                                                                                        .align(
                                                                                                                                                Alignment
                                                                                                                                                        .CenterStart
                                                                                                                                        )
                                                                                                                                        .background(
                                                                                                                                                Color(
                                                                                                                                                        0xFF8052EC
                                                                                                                                                ),
                                                                                                                                                CircleShape
                                                                                                                                        )
                                                                                                                )
                                                                                                        }
                                                                                                        val textColor =
                                                                                                                if (progressFraction >=
                                                                                                                                0.5f
                                                                                                                )
                                                                                                                        Color.White
                                                                                                                else
                                                                                                                        Color(
                                                                                                                                0xFF6C757D
                                                                                                                        )
                                                                                                        Row(
                                                                                                                modifier =
                                                                                                                        Modifier.fillMaxWidth(),
                                                                                                                horizontalArrangement =
                                                                                                                        Arrangement
                                                                                                                                .Center,
                                                                                                                verticalAlignment =
                                                                                                                        Alignment
                                                                                                                                .CenterVertically
                                                                                                        ) {
                                                                                                                Text(
                                                                                                                        text =
                                                                                                                                "${todayStudyMinutes}/30m",
                                                                                                                        color =
                                                                                                                                textColor,
                                                                                                                        fontSize =
                                                                                                                                11.sp,
                                                                                                                        fontWeight =
                                                                                                                                FontWeight
                                                                                                                                        .Bold,
                                                                                                                        fontFamily =
                                                                                                                                Inter,
                                                                                                                        lineHeight =
                                                                                                                                20.sp
                                                                                                                )
                                                                                                                Spacer(
                                                                                                                        modifier =
                                                                                                                                Modifier.width(
                                                                                                                                        2.dp
                                                                                                                                )
                                                                                                                )
                                                                                                        }
                                                                                                }
                                                                                        }
                                                                                }

                                                                                // Right
                                                                                // Illustration:
                                                                                // Chest Mascot
                                                                                // (Larger size)
                                                                                Box(
                                                                                        modifier =
                                                                                                Modifier.weight(
                                                                                                                0.85f
                                                                                                        )
                                                                                                        .height(
                                                                                                                105.dp
                                                                                                        ),
                                                                                        contentAlignment =
                                                                                                Alignment
                                                                                                        .CenterEnd
                                                                                ) {
                                                                                        Image(
                                                                                                painter =
                                                                                                        painterResource(
                                                                                                                id =
                                                                                                                        R.drawable
                                                                                                                                .ic_chest
                                                                                                        ),
                                                                                                contentDescription =
                                                                                                        "Treasure Chest",
                                                                                                contentScale =
                                                                                                        ContentScale
                                                                                                                .Fit,
                                                                                                modifier =
                                                                                                        Modifier.size(
                                                                                                                105.dp
                                                                                                        )
                                                                                        )
                                                                                }
                                                                        }
                                                                }
                                                        }
                                                }
                                        }

                                        // Spacer + Navigation bars padding to ensure complete
                                        // scrollability
                                        Spacer(modifier = Modifier.height(120.dp))
                                        Spacer(modifier = Modifier.navigationBarsPadding())
                                }
                        }
                }
        }
}

@Composable
fun QuickAccessItem(
        title: String,
        icon: ImageVector,
        onClick: () -> Unit,
        modifier: Modifier = Modifier
) {
        Column(
                modifier =
                        modifier.clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                        ) { onClick() },
                horizontalAlignment = Alignment.CenterHorizontally
        ) {
                Box(
                        modifier =
                                Modifier.size(56.dp)
                                        .background(Color(0xFFD2CAFF), RoundedCornerShape(18.dp)),
                        contentAlignment = Alignment.Center
                ) {
                        Icon(
                                imageVector = icon,
                                contentDescription = title,
                                tint = Color(0xFF5E27B6),
                                modifier = Modifier.size(26.dp)
                        )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                        text = title,
                        color = Color(0xFF1E152A),
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.Medium,
                        fontFamily = Inter,
                        textAlign = TextAlign.Center,
                        maxLines = 1
                )
        }
}

@Composable
private fun StreakTrackerBar(currentStreak: Int, modifier: Modifier = Modifier) {
        val days = listOf("Su", "Mo", "Tu", "We", "Th", "Fr", "Sa")
        val today = remember { java.time.LocalDate.now() }
        val endDayIndex =
                remember(today) { today.dayOfWeek.value % 7 } // Sunday = 0, Mon = 1 ... Sat = 6
        val clampedStreak = currentStreak.coerceIn(1, 7)
        val startDayIndex = (endDayIndex - (clampedStreak - 1)).coerceAtLeast(0)

        Column(
                modifier = modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
                // 1. Days of week header row (aligned with capsule inner width)
                Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                ) {
                        days.forEach { dayLabel ->
                                Box(
                                        modifier = Modifier.weight(1f),
                                        contentAlignment = Alignment.Center
                                ) {
                                        Text(
                                                text = dayLabel,
                                                color = Color.White,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                fontFamily = Inter
                                        )
                                }
                        }
                }

                // 2. White Capsule Container for progress track
                Box(
                        modifier =
                                Modifier.fillMaxWidth()
                                        .height(36.dp)
                                        .shadow(elevation = 2.dp, shape = CircleShape)
                                        .background(Color.White, CircleShape)
                                        .padding(horizontal = 14.dp),
                        contentAlignment = Alignment.CenterStart
                ) {
                        BoxWithConstraints(modifier = Modifier.fillMaxWidth().height(36.dp)) {
                                val containerWidth = maxWidth
                                val stepWidth = containerWidth / 7f

                                // If streak starts on Sunday (startDayIndex == 0), stretch all the
                                // way to left edge
                                // (0.dp)
                                val startX =
                                        if (startDayIndex == 0) 0.dp
                                        else stepWidth * (startDayIndex + 0.15f)
                                val endCenterX = stepWidth * (endDayIndex + 0.5f)

                                // Render Orange Connecting Track if streak > 1
                                if (clampedStreak > 1 && endCenterX > startX) {
                                        Box(
                                                modifier =
                                                        Modifier.offset(x = startX)
                                                                .width(endCenterX - startX)
                                                                .height(8.dp)
                                                                .align(Alignment.CenterStart)
                                                                .background(
                                                                        Color(0xFFFA6938),
                                                                        CircleShape
                                                                )
                                        )
                                }

                                // Render Flame Icon at endCenterX
                                val flameSize = 26.dp
                                Box(
                                        modifier =
                                                Modifier.offset(
                                                                x =
                                                                        (endCenterX -
                                                                                        (flameSize /
                                                                                                2))
                                                                                .coerceAtLeast(0.dp)
                                                        )
                                                        .size(flameSize)
                                                        .align(Alignment.CenterStart),
                                        contentAlignment = Alignment.Center
                                ) {
                                        Image(
                                                painter =
                                                        painterResource(id = R.drawable.ic_streak),
                                                contentDescription = "Streak Flame",
                                                modifier = Modifier.fillMaxSize()
                                        )
                                }
                        }
                }
        }
}
