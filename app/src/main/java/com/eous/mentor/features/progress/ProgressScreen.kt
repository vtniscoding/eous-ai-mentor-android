package com.eous.mentor.features.progress

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
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.eous.mentor.R
import com.eous.mentor.core.ui.theme.Inter
import com.eous.mentor.domain.model.Quiz
import com.eous.mentor.domain.model.SubjectStat

private val PrimaryPurple = Color(0xFF5B29A2)
private val HeaderPurple = Color(0xFF7F43D4)
private val LightPurpleCard = Color(0xFFDDE0FF)
private val LightGreenCard = Color(0xFFDCFCE7)

@Composable
fun ProgressScreen(
    onBack: () -> Unit,
    viewModel: ProgressViewModel
) {
    val state by viewModel.state.collectAsState()
    val stats = state.stats
    val scrollState = rememberScrollState()

    LaunchedEffect(Unit) {
        viewModel.loadStats()
    }

    val totalQueries = stats?.totalQueries ?: 0
    val streak = stats?.streak ?: 0
    val quizzes = stats?.quizzes ?: emptyList()

    // Calculated accuracy percentage
    val accuracyPct = remember(quizzes) {
        if (quizzes.isNotEmpty()) {
            val totalScore = quizzes.sumOf { it.score ?: 0 }
            val totalPossible = quizzes.sumOf { it.total_questions }.coerceAtLeast(1)
            ((totalScore.toFloat() / totalPossible) * 100).toInt().coerceIn(0, 100)
        } else {
            0
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF6F6F8))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp)
                .verticalScroll(scrollState)
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            // 1. TOP BAR (Back button + Progress Analytics title)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Back button without background or shadow
                androidx.compose.material3.IconButton(
                    onClick = onBack,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ChevronLeft,
                        contentDescription = "Back",
                        tint = Color.Black,
                        modifier = Modifier.size(28.dp)
                    )
                }

                // Title: Progress Analytics
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Progress ",
                        color = Color.Black,
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = Inter
                    )
                    Text(
                        text = "Analytics",
                        color = HeaderPurple,
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = Inter
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 2. QUIZ ACCURACY & WEEKLY PROGRESS CARDS (Row 1)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                QuizAccuracyCard(
                    modifier = Modifier.weight(1f),
                    accuracyPct = accuracyPct,
                    quizCount = quizzes.size
                )
                WeeklyProgressCard(
                    modifier = Modifier.weight(1f),
                    activeDays = streak
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            // 3. TOTAL QUESTION CARD
            TotalQuestionCard(totalQueries = totalQueries)

            Spacer(modifier = Modifier.height(22.dp))

            // 4. RECENT RESULT SECTION
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = "Recent result",
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

            Spacer(modifier = Modifier.height(12.dp))

            RecentResultsList(quizzes = quizzes)

            Spacer(modifier = Modifier.height(22.dp))

            // 5. STUDY TIME SECTION
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = "Study Time",
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

            Spacer(modifier = Modifier.height(12.dp))

            StudyTimeCard(
                thisWeekActivity = stats?.thisWeekActivity ?: emptyList(),
                lastWeekActivity = stats?.lastWeekActivity ?: emptyList(),
                thisWeekDateLabels = stats?.thisWeekDateLabels ?: emptyList(),
                lastWeekDateLabels = stats?.lastWeekDateLabels ?: emptyList()
            )

            Spacer(modifier = Modifier.height(22.dp))

            // 6. FAVOURITE SUBJECTS SECTION (Renamed from Your Subjects, placed at the bottom)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = "Favourite Subjects",
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

            Spacer(modifier = Modifier.height(12.dp))

            FavouriteSubjectsCard(
                subjectStats = stats?.subjectStats ?: emptyList()
            )

            Spacer(modifier = Modifier.height(120.dp))
        }
    }
}

@Composable
private fun QuizAccuracyCard(modifier: Modifier = Modifier, accuracyPct: Int, quizCount: Int) {
    Box(
        modifier = modifier
            .background(LightPurpleCard, RoundedCornerShape(20.dp))
            .padding(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "Quiz\nAccuracy",
                    color = Color.Black,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = Inter,
                    lineHeight = 16.sp
                )
                Text(
                    text = "$quizCount quizzes",
                    color = Color(0xFF64748B),
                    fontSize = 11.sp,
                    fontFamily = Inter
                )
            }

            Box(modifier = Modifier.size(54.dp), contentAlignment = Alignment.Center) {
                androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
                    drawArc(
                        color = Color(0xFFC7D2FE),
                        startAngle = 0f,
                        sweepAngle = 360f,
                        useCenter = false,
                        style = Stroke(width = 7.dp.toPx(), cap = StrokeCap.Round)
                    )
                    drawArc(
                        color = PrimaryPurple,
                        startAngle = -90f,
                        sweepAngle = (accuracyPct / 100f) * 360f,
                        useCenter = false,
                        style = Stroke(width = 7.dp.toPx(), cap = StrokeCap.Round)
                    )
                }
                Text(
                    text = "$accuracyPct%",
                    color = PrimaryPurple,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = Inter
                )
            }
        }
    }
}

@Composable
private fun WeeklyProgressCard(modifier: Modifier = Modifier, activeDays: Int) {
    Box(
        modifier = modifier
            .background(LightGreenCard, RoundedCornerShape(20.dp))
            .padding(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "Weekly\nProgress",
                    color = Color.Black,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = Inter,
                    lineHeight = 16.sp
                )
                Text(
                    text = "Keep it up!",
                    color = Color(0xFF166534),
                    fontSize = 11.sp,
                    fontFamily = Inter
                )
            }

            Box(modifier = Modifier.size(54.dp), contentAlignment = Alignment.Center) {
                androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
                    drawArc(
                        color = Color(0xFFBBF7D0),
                        startAngle = 0f,
                        sweepAngle = 360f,
                        useCenter = false,
                        style = Stroke(width = 7.dp.toPx(), cap = StrokeCap.Round)
                    )
                    drawArc(
                        color = Color(0xFF16A34A),
                        startAngle = -90f,
                        sweepAngle = (activeDays / 7f) * 360f,
                        useCenter = false,
                        style = Stroke(width = 7.dp.toPx(), cap = StrokeCap.Round)
                    )
                }
                Text(
                    text = "$activeDays days",
                    color = Color(0xFF166534),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = Inter
                )
            }
        }
    }
}

@Composable
private fun TotalQuestionCard(totalQueries: Int) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(130.dp)
            .background(Color(0xFFE2E2E6), RoundedCornerShape(24.dp))
            .padding(horizontal = 22.dp, vertical = 14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(verticalArrangement = Arrangement.Center) {
                Text(
                    text = "Total question",
                    color = Color(0xFF1E152A),
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = Inter
                )
                Text(
                    text = "$totalQueries",
                    color = PrimaryPurple,
                    fontSize = 38.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = Inter,
                    lineHeight = 42.sp
                )
                Text(
                    text = "Question",
                    color = Color(0xFF64748B),
                    fontSize = 12.5.sp,
                    fontFamily = Inter
                )
            }

            // Mascot Illustration using ic_total_question
            Image(
                painter = painterResource(id = R.drawable.ic_total_question),
                contentDescription = "Total Question Mascot",
                modifier = Modifier
                    .size(115.dp)
                    .offset(x = 10.dp, y = 4.dp)
            )
        }
    }
}

@Composable
private fun RecentResultsList(quizzes: List<Quiz>) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        if (quizzes.isNotEmpty()) {
            quizzes.take(3).forEach { quiz ->
                val s = quiz.score ?: 0
                val correctPct = if (quiz.total_questions > 0) {
                    ((s.toFloat() / quiz.total_questions) * 100).toInt()
                } else 0
                val wrongPct = 100 - correctPct

                RecentResultItemCard(
                    title = if (quiz.topic.isNotEmpty()) quiz.topic else "General Quiz",
                    subtitle = "$s/${quiz.total_questions} quiz",
                    correctPct = correctPct,
                    wrongPct = wrongPct
                )
            }
        } else {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(elevation = 2.dp, shape = RoundedCornerShape(20.dp), ambientColor = Color.Black.copy(alpha = 0.05f), spotColor = Color.Black.copy(alpha = 0.08f))
                    .background(Color.White, RoundedCornerShape(20.dp))
                    .padding(horizontal = 18.dp, vertical = 20.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No recent quiz results yet",
                    color = Color(0xFF94A3B8),
                    fontSize = 13.5.sp,
                    fontFamily = Inter,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
private fun RecentResultItemCard(
    title: String,
    subtitle: String,
    correctPct: Int,
    wrongPct: Int
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(elevation = 4.dp, shape = RoundedCornerShape(20.dp), ambientColor = Color.Black.copy(alpha = 0.05f), spotColor = Color.Black.copy(alpha = 0.08f))
            .background(Color.White, RoundedCornerShape(20.dp))
            .padding(horizontal = 18.dp, vertical = 14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = title,
                    color = Color.Black,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = Inter
                )
                Text(
                    text = subtitle,
                    color = Color(0xFF64748B),
                    fontSize = 11.5.sp,
                    fontFamily = Inter
                )
            }

            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Correct percentage green pill
                Box(
                    modifier = Modifier
                        .background(Color(0xFF5BA625), RoundedCornerShape(12.dp))
                        .padding(horizontal = 14.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = "$correctPct%",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = Inter
                    )
                }

                // Wrong percentage red pill
                Box(
                    modifier = Modifier
                        .background(Color(0xFFDC3545), RoundedCornerShape(12.dp))
                        .padding(horizontal = 14.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = "$wrongPct%",
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

@Composable
private fun StudyTimeCard(
    thisWeekActivity: List<Int>,
    lastWeekActivity: List<Int>,
    thisWeekDateLabels: List<String>,
    lastWeekDateLabels: List<String>
) {
    var isShowingLastWeek by remember { mutableStateOf(false) }

    val currentActivity = if (isShowingLastWeek) lastWeekActivity else thisWeekActivity
    val currentDateLabels = if (isShowingLastWeek) {
        if (lastWeekDateLabels.isNotEmpty()) lastWeekDateLabels else listOf("15/7", "16/7", "17/7", "18/7", "19/7", "20/7", "21/7")
    } else {
        if (thisWeekDateLabels.isNotEmpty()) thisWeekDateLabels else listOf("22/7", "23/7", "24/7", "25/7", "26/7", "27/7", "28/7")
    }

    val sumThisWeek = thisWeekActivity.sum()
    val sumLastWeek = lastWeekActivity.sum()
    val pctDiffText = remember(sumThisWeek, sumLastWeek) {
        if (sumLastWeek > 0) {
            val diff = ((sumThisWeek - sumLastWeek).toFloat() / sumLastWeek * 100).toInt()
            if (diff >= 0) "+$diff%" else "$diff%"
        } else if (sumThisWeek > 0) {
            "+100%"
        } else {
            "0%"
        }
    }

    // Clean white card with soft drop shadow (No dark underlayer box)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(elevation = 4.dp, shape = RoundedCornerShape(22.dp), ambientColor = Color.Black.copy(alpha = 0.05f), spotColor = Color.Black.copy(alpha = 0.08f))
            .background(Color.White, RoundedCornerShape(22.dp))
            .padding(16.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            // Header Row: +18% vs last week & arrows
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "$pctDiffText ",
                        color = Color.Black,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = Inter
                    )
                    Text(
                        text = if (isShowingLastWeek) "last week" else "vs last week",
                        color = Color(0xFF64748B),
                        fontSize = 11.5.sp,
                        fontFamily = Inter
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    // Previous Week Button
                    val prevInteractionSource = remember { MutableInteractionSource() }
                    val isPrevHovered by prevInteractionSource.collectIsHoveredAsState()
                    val isPrevPressed by prevInteractionSource.collectIsPressedAsState()

                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .background(
                                if (isPrevHovered || isPrevPressed) Color(0xFFE2E8F0) else Color(0xFFF1F5F9),
                                CircleShape
                            )
                            .clickable(interactionSource = prevInteractionSource, indication = null) {
                                isShowingLastWeek = true
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.ChevronLeft,
                            contentDescription = "Last week",
                            tint = Color(0xFF1E293B),
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    // Next Week Button
                    val nextInteractionSource = remember { MutableInteractionSource() }
                    val isNextHovered by nextInteractionSource.collectIsHoveredAsState()
                    val isNextPressed by nextInteractionSource.collectIsPressedAsState()

                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .background(
                                if (isNextHovered || isNextPressed) Color(0xFFE2E8F0) else Color(0xFFF1F5F9),
                                CircleShape
                            )
                            .clickable(interactionSource = nextInteractionSource, indication = null) {
                                isShowingLastWeek = false
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = "This week",
                            tint = Color(0xFF1E293B),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            // Weekly Activity Bar Chart (7 days Mon-Sun)
            val maxActivity = (currentActivity.maxOrNull() ?: 0)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(90.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                val defaultLabels = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")

                val count = 7
                for (i in 0 until count) {
                    val activityValue = currentActivity.getOrNull(i) ?: 0
                    val barHeight = if (maxActivity > 0 && activityValue > 0) {
                        val fraction = activityValue.toFloat() / maxActivity
                        (fraction * 65.dp.value).dp.coerceAtLeast(10.dp)
                    } else {
                        10.dp
                    }

                    val isHighlight = maxActivity > 0 && activityValue == maxActivity
                    val barColor = if (isHighlight) PrimaryPurple else Color(0xFFD9D9D9)
                    val labelText = currentDateLabels.getOrNull(i) ?: defaultLabels[i]

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Bottom,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .width(22.dp)
                                .height(barHeight)
                                .background(barColor, RoundedCornerShape(6.dp))
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = labelText,
                            color = Color(0xFF64748B),
                            fontSize = 9.sp,
                            fontFamily = Inter
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FavouriteSubjectsCard(
    subjectStats: List<SubjectStat>
) {
    // Clean white card with soft drop shadow
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(elevation = 4.dp, shape = RoundedCornerShape(22.dp), ambientColor = Color.Black.copy(alpha = 0.05f), spotColor = Color.Black.copy(alpha = 0.08f))
            .background(Color.White, RoundedCornerShape(22.dp))
            .padding(18.dp)
    ) {
        if (subjectStats.isEmpty()) {
            Text(
                text = "Start chatting to see your subject breakdown!",
                color = Color(0xFF94A3B8),
                fontSize = 13.sp,
                fontFamily = Inter
            )
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                subjectStats.forEach { stat ->
                    SubjectProgressRow(name = stat.name, percentage = stat.percentage)
                }
            }
        }
    }
}

@Composable
private fun SubjectProgressRow(name: String, percentage: Int) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = name,
                color = Color.Black,
                fontSize = 12.5.sp,
                fontWeight = FontWeight.SemiBold,
                fontFamily = Inter
            )
            Text(
                text = "$percentage%",
                color = Color(0xFF64748B),
                fontSize = 11.5.sp,
                fontFamily = Inter
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .background(Color(0xFFDDE0FF), CircleShape)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(fraction = (percentage / 100f).coerceIn(0f, 1f))
                    .fillMaxHeight()
                    .background(PrimaryPurple, CircleShape)
            )
        }
    }
}
