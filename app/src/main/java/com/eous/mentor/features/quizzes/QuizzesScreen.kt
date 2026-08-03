package com.eous.mentor.features.quizzes

import androidx.compose.animation.*
import androidx.compose.animation.core.*
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import com.eous.mentor.core.ui.components.AnalyzingLoadingScreen
import com.eous.mentor.core.ui.theme.Inter
import com.eous.mentor.domain.model.Quiz
import com.eous.mentor.domain.model.QuizQuestion

private val HeaderPurple = Color(0xFF5B29A2)

@Composable
fun QuizzesScreen(userId: String, onBack: () -> Unit, viewModel: QuizzesViewModel = viewModel()) {
    val state by viewModel.uiState.collectAsState()
    var showCreateDialog by remember { mutableStateOf(false) }

    LaunchedEffect(userId) { viewModel.loadQuizzes(userId) }

    val filteredQuizzes =
            remember(state.quizzes, state.selectedFilter, state.searchQuery) {
                state.quizzes.filter { quiz ->
                    val matchesFilter =
                            when (state.selectedFilter) {
                                QuizFilter.ALL -> true
                                QuizFilter.IN_PROGRESS -> quiz.status == "in_progress"
                                QuizFilter.COMPLETED -> quiz.status == "completed"
                            }
                    val matchesQuery =
                            state.searchQuery.isBlank() ||
                                    quiz.title.contains(state.searchQuery, ignoreCase = true) ||
                                    quiz.topic.contains(state.searchQuery, ignoreCase = true)
                    matchesFilter && matchesQuery
                }
            }

    Box(modifier = Modifier.fillMaxSize().background(Color(0xFFF6F6F8))) {
        Column(
                modifier =
                        Modifier.fillMaxSize()
                                .statusBarsPadding()
                                .navigationBarsPadding()
                                .padding(horizontal = 20.dp)
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            // ---- 1. Top Bar Header (Standardized with ProgressScreen) ----
            Row(
                    modifier = Modifier.fillMaxWidth(),
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
                            text = "Your ",
                            color = Color.Black,
                            fontSize = 19.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = Inter
                    )
                    Text(
                            text = "Quizzes",
                            color = HeaderPurple,
                            fontSize = 19.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = Inter
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ---- 2. Search Bar ----
            Surface(
                    shape = CircleShape,
                    color = Color.White,
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
                    shadowElevation = 1.dp,
                    modifier = Modifier.fillMaxWidth().height(48.dp)
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
                                        "Search quizzes...",
                                        color = Color(0xFF94A3B8),
                                        fontSize = 14.sp,
                                        fontFamily = Inter
                                )
                            },
                            singleLine = true,
                            colors =
                                    OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = Color.Transparent,
                                            unfocusedBorderColor = Color.Transparent,
                                            focusedTextColor = Color(0xFF0F172A),
                                            unfocusedTextColor = Color(0xFF0F172A)
                                    ),
                            modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ---- 3. Filter Pills ----
            Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
            ) {
                QuizFilterPill(
                        label = "All",
                        isSelected = state.selectedFilter == QuizFilter.ALL,
                        onClick = { viewModel.setFilter(QuizFilter.ALL) }
                )
                QuizFilterPill(
                        label = "In Progress",
                        isSelected = state.selectedFilter == QuizFilter.IN_PROGRESS,
                        onClick = { viewModel.setFilter(QuizFilter.IN_PROGRESS) }
                )
                QuizFilterPill(
                        label = "Completed",
                        isSelected = state.selectedFilter == QuizFilter.COMPLETED,
                        onClick = { viewModel.setFilter(QuizFilter.COMPLETED) }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ---- 4. Quiz Cards List ----
            if (state.isLoading) {
                Box(
                        modifier = Modifier.fillMaxWidth().weight(1f),
                        contentAlignment = Alignment.Center
                ) { CircularProgressIndicator(color = HeaderPurple) }
            } else if (filteredQuizzes.isEmpty()) {
                Box(
                        modifier = Modifier.fillMaxWidth().weight(1f),
                        contentAlignment = Alignment.Center
                ) {
                    Text(
                            text = "No quizz available",
                            color = Color(0xFF94A3B8),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium,
                            fontFamily = Inter
                    )
                }
            } else {
                LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(14.dp),
                        contentPadding = PaddingValues(bottom = 100.dp),
                        modifier = Modifier.weight(1f)
                ) {
                    items(filteredQuizzes, key = { it.id }) { quiz ->
                        QuizCardItem(quiz = quiz, onClick = { viewModel.openQuiz(quiz) })
                    }
                }
            }
        }

        // ---- 5. Floating Bottom Button (Create Quizz) with 3D Solid Shadow & Hover effect ----
        val createInteractionSource = remember { MutableInteractionSource() }
        val isCreateHovered by createInteractionSource.collectIsHoveredAsState()
        val isCreatePressed by createInteractionSource.collectIsPressedAsState()
        val createScale by
                animateFloatAsState(
                        targetValue =
                                if (isCreatePressed) 0.96f
                                else if (isCreateHovered) 1.02f else 1.0f,
                        animationSpec = spring(stiffness = Spring.StiffnessLow),
                        label = "create_quiz_scale"
                )

        Box(
                modifier =
                        Modifier.align(Alignment.BottomCenter)
                                .fillMaxWidth()
                                .navigationBarsPadding()
                                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            Box(
                    modifier =
                            Modifier.fillMaxWidth().graphicsLayer {
                                scaleX = createScale
                                scaleY = createScale
                            }
            ) {
                // Solid dark shadow layer (styled like Log Out button)
                Box(
                        modifier =
                                Modifier.matchParentSize()
                                        .offset(x = 3.dp, y = 3.dp)
                                        .background(HeaderPurple, RoundedCornerShape(22.dp))
                )

                // Upper main button layer
                Box(
                        modifier =
                                Modifier.fillMaxWidth()
                                        .background(
                                                if (isCreatePressed || isCreateHovered)
                                                        Color(0xFFC084FC)
                                                else Color(0xFFDDD6FE),
                                                RoundedCornerShape(22.dp)
                                        )
                                        .clickable(
                                                interactionSource = createInteractionSource,
                                                indication = null
                                        ) { showCreateDialog = true }
                                        .padding(vertical = 14.dp),
                        contentAlignment = Alignment.Center
                ) {
                    Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Create Quizz",
                                tint = HeaderPurple,
                                modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                                text = "Create Quizz",
                                color = HeaderPurple,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 17.sp,
                                fontFamily = Inter
                        )
                    }
                }
            }
        }
    }

    // ---- Create Quiz Wizard Dialog ----
    if (showCreateDialog) {
        CreateQuizWizardDialog(
                subjects = state.userSubjects,
                isGenerating = state.isGeneratingQuiz,
                onDismiss = { if (!state.isGeneratingQuiz) showCreateDialog = false },
                onCreate = { topic, prompt, totalQuestions, difficulty ->
                    viewModel.createQuizWithAi(userId, topic, prompt, totalQuestions, difficulty)
                }
        )
    }

    LaunchedEffect(state.activeQuiz) {
        if (state.activeQuiz != null) {
            showCreateDialog = false
        }
    }

    // ---- Active Quiz Player & Results Dialog ----
    val activeQuiz = state.activeQuiz
    if (activeQuiz != null) {
        if (state.showResultModal) {
            QuizResultDialog(
                    quiz = activeQuiz,
                    questions = state.activeQuestions,
                    onRetake = { viewModel.retakeActiveQuiz(userId) },
                    onClose = { viewModel.closeActiveQuiz(userId) }
            )
        } else {
            QuizPlayerDialog(
                    quiz = activeQuiz,
                    questions = state.activeQuestions,
                    currentIndex = state.activeQuestionIndex,
                    onSelectAnswer = { qIdx, aIdx -> viewModel.selectAnswer(qIdx, aIdx) },
                    onNext = { viewModel.nextQuestion(userId) },
                    onPrev = { viewModel.prevQuestion() },
                    onSubmit = { viewModel.submitQuiz(userId) },
                    onClose = { viewModel.closeActiveQuiz(userId) }
            )
        }
    }
}

@Composable
private fun QuizFilterPill(label: String, isSelected: Boolean, onClick: () -> Unit) {
    Surface(
            onClick = onClick,
            shape = RoundedCornerShape(20.dp),
            color = if (isSelected) HeaderPurple else Color(0xFFE2E8F0),
            modifier = Modifier.height(36.dp)
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(horizontal = 18.dp)) {
            Text(
                    text = label,
                    color = if (isSelected) Color.White else Color(0xFF475569),
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    fontSize = 13.sp,
                    fontFamily = Inter
            )
        }
    }
}

@Composable
private fun QuizCardItem(quiz: Quiz, onClick: () -> Unit) {
    Card(
            onClick = onClick,
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            modifier = Modifier.fillMaxWidth()
    ) {
        Column(
                modifier = Modifier.fillMaxWidth().padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // ---- Top Header Row ----
            Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Subject Badge
                    Surface(shape = RoundedCornerShape(12.dp), color = HeaderPurple) {
                        Text(
                                text = quiz.topic.ifBlank { "General" },
                                color = Color.White,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 12.sp,
                                fontFamily = Inter,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }

                    // Difficulty Badge
                    val (diffBg, diffText) =
                            when (quiz.difficulty.lowercase()) {
                                "easy" -> Color(0xFFDCFCE7) to "🟢 Easy"
                                "hard" -> Color(0xFFFEE2E2) to "🔴 Hard"
                                else -> Color(0xFFFEF3C7) to "🟡 Medium"
                            }
                    val diffTextColor =
                            when (quiz.difficulty.lowercase()) {
                                "easy" -> Color(0xFF15803D)
                                "hard" -> Color(0xFFB91C1C)
                                else -> Color(0xFFB45309)
                            }
                    Surface(shape = RoundedCornerShape(12.dp), color = diffBg) {
                        Text(
                                text = diffText,
                                color = diffTextColor,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                fontFamily = Inter,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                // Relative Date / Timestamp
                val timeLabel =
                        when {
                            quiz.status == "not_started" -> "Not started"
                            quiz.created_at.contains("2026") -> "2 days ago"
                            else -> "Yesterday"
                        }
                Text(
                        text = timeLabel,
                        color = Color(0xFF64748B),
                        fontSize = 12.sp,
                        fontFamily = Inter
                )
            }

            // ---- Title ----
            Text(
                    text = quiz.title.ifBlank { "Intro to loops and array" },
                    color = Color(0xFF0F172A),
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    fontFamily = Inter
            )

            // ---- Status / Score / Progress Row ----
            when (quiz.status) {
                "completed" -> {
                    val scoreVal = quiz.score ?: 0
                    val totalVal = if (quiz.total_questions > 0) quiz.total_questions else 1
                    val pct = ((scoreVal.toFloat() / totalVal) * 100).toInt().coerceIn(0, 100)
                    val isHighScore = pct >= 50
                    val scoreColor = if (isHighScore) Color(0xFF16A34A) else Color(0xFFEA580C)

                    Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                                text = "${quiz.total_questions} questions",
                                color = Color(0xFF64748B),
                                fontSize = 13.sp,
                                fontFamily = Inter
                        )

                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(
                                    text = "$pct%",
                                    color = scoreColor,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 22.sp,
                                    fontFamily = Inter
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                    text = "score",
                                    color = scoreColor,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    fontFamily = Inter,
                                    modifier = Modifier.padding(bottom = 2.dp)
                            )
                        }
                    }
                }
                "in_progress" -> {
                    val currentIdx = quiz.current_question_index.coerceAtLeast(1)
                    val totalQ = if (quiz.total_questions > 0) quiz.total_questions else 12

                    Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                                text = "Question $currentIdx of $totalQ",
                                color = Color(0xFF64748B),
                                fontSize = 13.sp,
                                fontFamily = Inter
                        )
                        Text(
                                text = "In progress",
                                color = HeaderPurple,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                fontFamily = Inter
                        )
                    }

                    // Progress Bar
                    val progressFraction = (currentIdx.toFloat() / totalQ).coerceIn(0f, 1f)
                    LinearProgressIndicator(
                            progress = { progressFraction },
                            color = HeaderPurple,
                            trackColor = Color(0xFFDDD6FE),
                            strokeCap = StrokeCap.Round,
                            gapSize = 0.dp,
                            drawStopIndicator = {},
                            modifier = Modifier.fillMaxWidth().height(6.dp).clip(CircleShape)
                    )
                }
                else -> { // "not_started"
                    Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                                text = "${quiz.total_questions} questions",
                                color = Color(0xFF64748B),
                                fontSize = 13.sp,
                                fontFamily = Inter
                        )

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                    text = "Start quizz",
                                    color = HeaderPurple,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    fontFamily = Inter
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                    contentDescription = "Start",
                                    tint = HeaderPurple,
                                    modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

// ---- Interactive Quiz Player Dialog ----
@Composable
private fun QuizPlayerDialog(
        quiz: Quiz,
        questions: List<QuizQuestion>,
        currentIndex: Int,
        onSelectAnswer: (qIndex: Int, aIndex: Int) -> Unit,
        onNext: () -> Unit,
        onPrev: () -> Unit,
        onSubmit: () -> Unit,
        onClose: () -> Unit
) {
    if (questions.isEmpty()) {
        AlertDialog(
                onDismissRequest = onClose,
                shape = RoundedCornerShape(20.dp),
                containerColor = Color.White,
                title = {
                    Text(
                            "No Questions Available",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            fontFamily = Inter
                    )
                },
                text = {
                    Text(
                            "This quiz in the database currently contains no questions.",
                            color = Color(0xFF64748B),
                            fontSize = 14.sp,
                            fontFamily = Inter
                    )
                },
                confirmButton = {
                    Button(
                            onClick = onClose,
                            colors = ButtonDefaults.buttonColors(containerColor = HeaderPurple),
                            shape = RoundedCornerShape(12.dp)
                    ) { Text("Close", color = Color.White) }
                }
        )
        return
    }

    val currentQuestion = questions.getOrNull(currentIndex) ?: return
    val totalCount = questions.size

    Dialog(
            onDismissRequest = onClose,
            properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
                modifier = Modifier.fillMaxSize().background(Color(0xFFF8FAFC)),
                color = Color(0xFFF8FAFC)
        ) {
            Column(
                    modifier =
                            Modifier.fillMaxSize()
                                    .statusBarsPadding()
                                    .navigationBarsPadding()
                                    .padding(20.dp)
            ) {
                // Top Action Bar
                Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(shape = RoundedCornerShape(12.dp), color = HeaderPurple) {
                        Text(
                                text = quiz.topic.ifBlank { "General" },
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }

                    Text(
                            text = "Question ${currentIndex + 1} of $totalCount",
                            color = Color(0xFF475569),
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            fontFamily = Inter
                    )

                    IconButton(onClick = onClose) {
                        Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close",
                                tint = Color(0xFF64748B)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Question Progress Indicator
                val progressFrac = ((currentIndex + 1).toFloat() / totalCount).coerceIn(0f, 1f)
                LinearProgressIndicator(
                        progress = { progressFrac },
                        color = HeaderPurple,
                        trackColor = Color(0xFFE2E8F0),
                        strokeCap = StrokeCap.Round,
                        gapSize = 0.dp,
                        drawStopIndicator = {},
                        modifier = Modifier.fillMaxWidth().height(8.dp).clip(CircleShape)
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Scrollable Question & Options
                Column(
                        modifier = Modifier.weight(1f).verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Question Card
                    Card(
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            border =
                                    androidx.compose.foundation.BorderStroke(
                                            1.dp,
                                            Color(0xFFE2E8F0)
                                    ),
                            modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Text(
                                    text = quiz.title,
                                    color = HeaderPurple,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = Inter
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                    text = currentQuestion.question,
                                    color = Color(0xFF0F172A),
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = Inter,
                                    lineHeight = 26.sp
                            )
                        }
                    }

                    // Options List with Real-time Correct/Incorrect Feedback
                    val hasAnswered = currentQuestion.selectedAnswerIndex != null
                    val correctIdx = currentQuestion.correctAnswerIndex

                    currentQuestion.options.forEachIndexed { aIdx, optionText ->
                        val isSelected = currentQuestion.selectedAnswerIndex == aIdx
                        val isCorrectOption = aIdx == correctIdx

                        val optionBgColor =
                                when {
                                    hasAnswered && isCorrectOption ->
                                            Color(0xFFDCFCE7) // Light Green
                                    hasAnswered && isSelected -> Color(0xFFFEE2E2) // Light Red
                                    else -> Color.White
                                }
                        val optionBorderColor =
                                when {
                                    hasAnswered && isCorrectOption ->
                                            Color(0xFF16A34A) // Green Border
                                    hasAnswered && isSelected -> Color(0xFFDC2626) // Red Border
                                    isSelected -> HeaderPurple
                                    else -> Color(0xFFE2E8F0)
                                }
                        val circleBgColor =
                                when {
                                    hasAnswered && isCorrectOption -> Color(0xFF16A34A)
                                    hasAnswered && isSelected -> Color(0xFFDC2626)
                                    isSelected -> HeaderPurple
                                    else -> Color(0xFFF1F5F9)
                                }

                        Surface(
                                onClick = { onSelectAnswer(currentIndex, aIdx) },
                                shape = RoundedCornerShape(16.dp),
                                color = optionBgColor,
                                border =
                                        androidx.compose.foundation.BorderStroke(
                                                width =
                                                        if (isSelected ||
                                                                        (hasAnswered &&
                                                                                isCorrectOption)
                                                        )
                                                                2.dp
                                                        else 1.dp,
                                                color = optionBorderColor
                                        ),
                                modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                    modifier = Modifier.padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                        shape = CircleShape,
                                        color = circleBgColor,
                                        modifier = Modifier.size(32.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                                text = ('A' + aIdx).toString(),
                                                color =
                                                        if (isSelected ||
                                                                        (hasAnswered &&
                                                                                isCorrectOption)
                                                        )
                                                                Color.White
                                                        else Color(0xFF64748B),
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 14.sp
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.width(14.dp))

                                Text(
                                        text = optionText,
                                        color =
                                                when {
                                                    hasAnswered && isCorrectOption ->
                                                            Color(0xFF15803D)
                                                    hasAnswered && isSelected -> Color(0xFFB91C1C)
                                                    isSelected -> HeaderPurple
                                                    else -> Color(0xFF1E293B)
                                                },
                                        fontWeight =
                                                if (isSelected || (hasAnswered && isCorrectOption))
                                                        FontWeight.Bold
                                                else FontWeight.Medium,
                                        fontSize = 15.sp,
                                        fontFamily = Inter,
                                        modifier = Modifier.weight(1f)
                                )

                                if (hasAnswered && isCorrectOption) {
                                    Text(
                                            text = "✓",
                                            color = Color(0xFF16A34A),
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = 18.sp
                                    )
                                } else if (hasAnswered && isSelected && !isCorrectOption) {
                                    Text(
                                            text = "✕",
                                            color = Color(0xFFDC2626),
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = 18.sp
                                    )
                                }
                            }
                        }
                    }

                    // AI Explanation Card (shown once an answer is selected)
                    if (hasAnswered) {
                        val explanationText =
                                currentQuestion.explanation
                                        ?: if (currentQuestion.selectedAnswerIndex == correctIdx) {
                                            "Correct! Great job understanding this concept."
                                        } else {
                                            "The correct option is ('${('A' + correctIdx)}'): ${currentQuestion.options.getOrNull(correctIdx) ?: ""}."
                                        }

                        Card(
                                shape = RoundedCornerShape(16.dp),
                                colors =
                                        CardDefaults.cardColors(containerColor = Color(0xFFF0F9FF)),
                                border =
                                        androidx.compose.foundation.BorderStroke(
                                                1.dp,
                                                Color(0xFFBAE6FD)
                                        ),
                                modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                        text = "💡 AI Mentor Explanation",
                                        color = Color(0xFF0369A1),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        fontFamily = Inter
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                        text = explanationText,
                                        color = Color(0xFF0C4A6E),
                                        fontSize = 13.sp,
                                        fontFamily = Inter,
                                        lineHeight = 20.sp
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Bottom Action Buttons
                Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                            onClick = onPrev,
                            enabled = currentIndex > 0,
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.height(48.dp)
                    ) { Text("Previous") }

                    if (currentIndex < totalCount - 1) {
                        Button(
                                onClick = onNext,
                                colors = ButtonDefaults.buttonColors(containerColor = HeaderPurple),
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier.height(48.dp)
                        ) {
                            Text("Next Question", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    } else {
                        Button(
                                onClick = onSubmit,
                                colors =
                                        ButtonDefaults.buttonColors(
                                                containerColor = Color(0xFF16A34A)
                                        ),
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier.height(48.dp)
                        ) { Text("Finish Quiz", color = Color.White, fontWeight = FontWeight.Bold) }
                    }
                }
            }
        }
    }
}

// ---- Quiz Results / Review Dialog ----
@Composable
private fun QuizResultDialog(
        quiz: Quiz,
        questions: List<QuizQuestion>,
        onRetake: () -> Unit,
        onClose: () -> Unit
) {
    val score = quiz.score ?: 0
    val total =
            if (quiz.total_questions > 0) quiz.total_questions else questions.size.coerceAtLeast(1)
    val pct = ((score.toFloat() / total) * 100).toInt().coerceIn(0, 100)
    val isSuccess = pct >= 50

    Dialog(
            onDismissRequest = onClose,
            properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
                modifier = Modifier.fillMaxSize().background(Color(0xFFF8FAFC)),
                color = Color(0xFFF8FAFC)
        ) {
            Column(
                    modifier =
                            Modifier.fillMaxSize()
                                    .statusBarsPadding()
                                    .navigationBarsPadding()
                                    .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
            ) {
                Surface(
                        shape = CircleShape,
                        color = if (isSuccess) Color(0xFFDCFCE7) else Color(0xFFFFEDD5),
                        modifier = Modifier.size(100.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                                text = "$pct%",
                                color = if (isSuccess) Color(0xFF16A34A) else Color(0xFFEA580C),
                                fontSize = 32.sp,
                                fontWeight = FontWeight.ExtraBold,
                                fontFamily = Inter
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                        text = if (isSuccess) "Awesome job!" else "Keep practicing!",
                        color = Color(0xFF0F172A),
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = Inter
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                        text = "You scored $score out of $total questions on \"${quiz.title}\"",
                        color = Color(0xFF64748B),
                        fontSize = 15.sp,
                        fontFamily = Inter
                )

                Spacer(modifier = Modifier.height(32.dp))

                Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedButton(
                            onClick = onRetake,
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.weight(1f).height(50.dp)
                    ) {
                        Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Retake",
                                modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Retake")
                    }

                    Button(
                            onClick = onClose,
                            colors = ButtonDefaults.buttonColors(containerColor = HeaderPurple),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.weight(1f).height(50.dp)
                    ) { Text("Done", color = Color.White, fontWeight = FontWeight.Bold) }
                }
            }
        }
    }
}

@Composable
private fun CreateQuizWizardDialog(
        subjects: List<String>,
        isGenerating: Boolean,
        onDismiss: () -> Unit,
        onCreate: (topic: String, prompt: String, totalQuestions: Int, difficulty: String) -> Unit
) {
    var currentStep by remember { mutableIntStateOf(1) } // Step 1, 2, 3
    var topicText by remember { mutableStateOf("") }
    var selectedDifficulty by remember { mutableStateOf("medium") }
    var selectedQuestionCount by remember { mutableIntStateOf(5) }

    val suggestedChips =
            remember(subjects) {
                val defaults = listOf("Math", "Physics", "Geography", "Chemistry", "English")
                (subjects.filter { it.isNotBlank() } + defaults).distinct()
            }

    Dialog(
            onDismissRequest = { if (!isGenerating) onDismiss() },
            properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(modifier = Modifier.fillMaxSize(), color = Color(0xFFF8FAFC)) {
            if (isGenerating) {
                AnalyzingLoadingScreen()
            } else {
                Column(
                        modifier =
                                Modifier.fillMaxSize()
                                        .statusBarsPadding()
                                        .navigationBarsPadding()
                                        .padding(24.dp)
                ) {
                    // Header Nav
                    Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                    ) {
                        if (currentStep > 1) {
                            IconButton(onClick = { currentStep-- }) {
                                Icon(
                                        imageVector = Icons.Default.ChevronLeft,
                                        contentDescription = "Back",
                                        tint = Color(0xFF0F172A),
                                        modifier = Modifier.size(28.dp)
                                )
                            }
                        } else {
                            Spacer(modifier = Modifier.size(40.dp))
                        }

                        Surface(shape = RoundedCornerShape(12.dp), color = Color(0xFFF3E8FF)) {
                            Text(
                                    text = "Step $currentStep of 3",
                                    color = HeaderPurple,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    fontFamily = Inter,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }

                        IconButton(onClick = onDismiss) {
                            Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Close",
                                    tint = Color(0xFF64748B)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    val stepProgress = (currentStep / 3f).coerceIn(0f, 1f)
                    LinearProgressIndicator(
                            progress = { stepProgress },
                            color = HeaderPurple,
                            trackColor = Color(0xFFE2E8F0),
                            strokeCap = StrokeCap.Round,
                            gapSize = 0.dp,
                            drawStopIndicator = {},
                            modifier = Modifier.fillMaxWidth().height(6.dp).clip(CircleShape)
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    // Step Body
                    Box(modifier = Modifier.weight(1f)) {
                        when (currentStep) {
                            1 ->
                                    Step1TopicScreen(
                                            topicText = topicText,
                                            onTopicChange = { topicText = it },
                                            suggestedChips = suggestedChips
                                    )
                            2 ->
                                    Step2DifficultyScreen(
                                            selectedDifficulty = selectedDifficulty,
                                            onSelect = { selectedDifficulty = it }
                                    )
                            3 ->
                                    Step3QuestionCountScreen(
                                            selectedCount = selectedQuestionCount,
                                            onSelect = { selectedQuestionCount = it }
                                    )
                        }
                    }

                    // Footer Button
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                            onClick = {
                                if (currentStep < 3) {
                                    currentStep++
                                } else {
                                    onCreate(
                                            topicText.trim().ifBlank { "General Practice" },
                                            topicText.trim(),
                                            selectedQuestionCount,
                                            selectedDifficulty
                                    )
                                }
                            },
                            enabled = currentStep != 1 || topicText.isNotBlank(),
                            colors =
                                    ButtonDefaults.buttonColors(
                                            containerColor = HeaderPurple,
                                            disabledContainerColor = Color(0xFFCBD5E1)
                                    ),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth().height(54.dp)
                    ) {
                        Text(
                                text =
                                        if (currentStep < 3) "Continue ➔"
                                        else "✨ Generate Quiz with AI",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                fontFamily = Inter
                        )
                    }
                }
            }
        }
    }
}


@Composable
private fun Step1TopicScreen(
        topicText: String,
        onTopicChange: (String) -> Unit,
        suggestedChips: List<String>
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.fillMaxWidth()) {
        Text(
                text = "What subject or topic do you want to practice?",
                color = Color(0xFF0F172A),
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp,
                fontFamily = Inter,
                lineHeight = 28.sp
        )

        Text(
                text = "Enter a subject or a specific topic you want AI to quiz you on.",
                color = Color(0xFF64748B),
                fontSize = 14.sp,
                fontFamily = Inter
        )

        OutlinedTextField(
                value = topicText,
                onValueChange = onTopicChange,
                placeholder = { Text("e.g. Mathematics, Calculus, Organic Chemistry...") },
                singleLine = true,
                colors =
                        OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = HeaderPurple,
                                unfocusedBorderColor = Color(0xFFCBD5E1),
                                focusedContainerColor = Color.White,
                                unfocusedContainerColor = Color.White
                        ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
        )

        Text(
                text = "Or tap a quick suggestion:",
                color = Color(0xFF475569),
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.sp,
                fontFamily = Inter
        )

        Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState())
        ) {
            suggestedChips.forEach { chip ->
                val isSelected = topicText.equals(chip, ignoreCase = true)
                Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = if (isSelected) HeaderPurple else Color.White,
                        border =
                                androidx.compose.foundation.BorderStroke(
                                        1.dp,
                                        if (isSelected) HeaderPurple else Color(0xFFE2E8F0)
                                ),
                        modifier = Modifier.clickable { onTopicChange(chip) }
                ) {
                    Text(
                            text = chip,
                            color = if (isSelected) Color.White else Color(0xFF334155),
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp,
                            fontFamily = Inter,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun Step2DifficultyScreen(selectedDifficulty: String, onSelect: (String) -> Unit) {
    val items =
            listOf(
                    Triple("easy", "🟢 Easy", "Basic concepts & foundational practice"),
                    Triple(
                            "medium",
                            "🟡 Medium",
                            "Standard test questions with balanced challenge"
                    ),
                    Triple("hard", "🔴 Hard", "Advanced problem solving & tricky questions")
            )

    Column(verticalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.fillMaxWidth()) {
        Text(
                text = "Select Difficulty Level",
                color = Color(0xFF0F172A),
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp,
                fontFamily = Inter
        )

        Text(
                text = "Choose the challenge level that fits your learning goal.",
                color = Color(0xFF64748B),
                fontSize = 14.sp,
                fontFamily = Inter
        )

        items.forEach { (key, title, desc) ->
            val isSelected = selectedDifficulty == key
            Card(
                    shape = RoundedCornerShape(18.dp),
                    colors =
                            CardDefaults.cardColors(
                                    containerColor =
                                            if (isSelected) Color(0xFFF3E8FF) else Color.White
                            ),
                    border =
                            androidx.compose.foundation.BorderStroke(
                                    width = if (isSelected) 2.dp else 1.dp,
                                    color = if (isSelected) HeaderPurple else Color(0xFFE2E8F0)
                            ),
                    modifier = Modifier.fillMaxWidth().clickable { onSelect(key) }
            ) {
                Row(
                        modifier = Modifier.padding(18.dp),
                        verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                                text = title,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = Color(0xFF0F172A),
                                fontFamily = Inter
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                                text = desc,
                                fontSize = 13.sp,
                                color = Color(0xFF64748B),
                                fontFamily = Inter
                        )
                    }
                    RadioButton(
                            selected = isSelected,
                            onClick = { onSelect(key) },
                            colors = RadioButtonDefaults.colors(selectedColor = HeaderPurple)
                    )
                }
            }
        }
    }
}

@Composable
private fun Step3QuestionCountScreen(selectedCount: Int, onSelect: (Int) -> Unit) {
    val options =
            listOf(
                    Triple(3, "⚡ 3 Questions", "Quick Warmup (approx. 2 mins)"),
                    Triple(5, "🎯 5 Questions", "Standard Practice (approx. 5 mins)"),
                    Triple(10, "🔥 10 Questions", "Comprehensive Test (approx. 10 mins)")
            )

    Column(verticalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.fillMaxWidth()) {
        Text(
                text = "How many questions?",
                color = Color(0xFF0F172A),
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp,
                fontFamily = Inter
        )

        Text(
                text = "Pick the quiz length that suits your study session.",
                color = Color(0xFF64748B),
                fontSize = 14.sp,
                fontFamily = Inter
        )

        options.forEach { (count, title, desc) ->
            val isSelected = selectedCount == count
            Card(
                    shape = RoundedCornerShape(18.dp),
                    colors =
                            CardDefaults.cardColors(
                                    containerColor =
                                            if (isSelected) Color(0xFFF3E8FF) else Color.White
                            ),
                    border =
                            androidx.compose.foundation.BorderStroke(
                                    width = if (isSelected) 2.dp else 1.dp,
                                    color = if (isSelected) HeaderPurple else Color(0xFFE2E8F0)
                            ),
                    modifier = Modifier.fillMaxWidth().clickable { onSelect(count) }
            ) {
                Row(
                        modifier = Modifier.padding(18.dp),
                        verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                                text = title,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = Color(0xFF0F172A),
                                fontFamily = Inter
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                                text = desc,
                                fontSize = 13.sp,
                                color = Color(0xFF64748B),
                                fontFamily = Inter
                        )
                    }
                    RadioButton(
                            selected = isSelected,
                            onClick = { onSelect(count) },
                            colors = RadioButtonDefaults.colors(selectedColor = HeaderPurple)
                    )
                }
            }
        }
    }
}
