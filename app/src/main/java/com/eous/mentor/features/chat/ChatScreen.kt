package com.eous.mentor.features.chat

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.eous.mentor.R
import com.eous.mentor.core.ui.theme.EousPurple
import com.eous.mentor.domain.model.ChatMessage
import com.eous.mentor.domain.model.ChatSession
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

// --- CUSTOM MODIFIER FOR DASHED BORDER ---
fun Modifier.dashedBorder(
        width: Dp,
        color: Color,
        cornerRadius: Dp = 12.dp,
        on: Dp = 6.dp,
        off: Dp = 6.dp
) =
        this.drawBehind {
                val strokeWidth = width.toPx()
                val onPx = on.toPx()
                val offPx = off.toPx()
                val radiusPx = cornerRadius.toPx()

                val path =
                        Path().apply {
                                addRoundRect(
                                        RoundRect(
                                                rect =
                                                        androidx.compose.ui.geometry.Rect(
                                                                offset =
                                                                        Offset(
                                                                                strokeWidth / 2,
                                                                                strokeWidth / 2
                                                                        ),
                                                                size =
                                                                        Size(
                                                                                size.width -
                                                                                        strokeWidth,
                                                                                size.height -
                                                                                        strokeWidth
                                                                        )
                                                        ),
                                                cornerRadius = CornerRadius(radiusPx, radiusPx)
                                        )
                                )
                        }
                drawPath(
                        path = path,
                        color = color,
                        style =
                                Stroke(
                                        width = strokeWidth,
                                        pathEffect =
                                                PathEffect.dashPathEffect(
                                                        floatArrayOf(onPx, offPx),
                                                        0f
                                                )
                                )
                )
        }

@Composable
fun Chat(
        userId: String,
        onMenuClick: () -> Unit = {},
        initialQuestion: String = "",
        viewModel: ChatViewModel,
        onNavigateToSearch: () -> Unit = {},
        onNavigateToQuizzes: () -> Unit = {}
) {
        val state by viewModel.state.collectAsState()
        val context = LocalContext.current
        var showAttachmentMenu by remember { mutableStateOf(false) }
        var showHistorySheet by remember { mutableStateOf(false) }

        LaunchedEffect(state.errorMessage) {
                state.errorMessage?.let { error ->
                        Toast.makeText(context, error, Toast.LENGTH_LONG).show()
                        viewModel.clearError()
                }
        }

        // Image picker launcher
        val imagePickerLauncher =
                rememberLauncherForActivityResult(
                        contract = ActivityResultContracts.GetContent()
                ) { uri -> uri?.let { viewModel.onImagePicked(it, context) } }

        // Camera launcher
        val cameraLauncher =
                rememberLauncherForActivityResult(
                        contract = ActivityResultContracts.TakePicturePreview()
                ) { bitmap ->
                        bitmap?.let {
                                val cachePath = java.io.File(context.cacheDir, "camera_images")
                                cachePath.mkdirs()
                                val file =
                                        java.io.File(
                                                cachePath,
                                                "captured_${System.currentTimeMillis()}.jpg"
                                        )
                                try {
                                        val stream = java.io.FileOutputStream(file)
                                        it.compress(
                                                android.graphics.Bitmap.CompressFormat.JPEG,
                                                100,
                                                stream
                                        )
                                        stream.close()
                                        val uri = Uri.fromFile(file)
                                        viewModel.onImagePicked(uri, context)
                                } catch (e: Exception) {
                                        Toast.makeText(
                                                        context,
                                                        "Failed to capture image",
                                                        Toast.LENGTH_SHORT
                                                )
                                                .show()
                                }
                        }
                }

        // Process initial question if passed
        LaunchedEffect(initialQuestion) {
                if (initialQuestion.isNotBlank()) {
                        viewModel.onInputTextChanged(initialQuestion)
                }
        }

        // Handle system back press
        if (state.activeSession != null) {
                BackHandler { viewModel.startNewChat() }
        }

        Box(
                modifier =
                        Modifier.fillMaxSize()
                                .background(
                                        brush =
                                                androidx.compose.ui.graphics.Brush.verticalGradient(
                                                        colors =
                                                                listOf(
                                                                        Color(0xFFECE7FF),
                                                                        Color(0xFFFFFFFF)
                                                                )
                                                )
                                )
        ) {
                if (state.isLoadingSessions) {
                        com.eous.mentor.core.ui.components.PreparingLoadingScreen()
                } else {
                        if (state.activeSession == null) {
                                // --- LANDING SCREEN LAYOUT ---
                                ChatLandingContent(
                                        state = state,
                                        viewModel = viewModel,
                                        showAttachmentMenu = showAttachmentMenu,
                                        onToggleAttachment = { showAttachmentMenu = it },
                                        imagePickerLauncher = imagePickerLauncher,
                                        cameraLauncher = cameraLauncher,
                                        onShowHistory = { showHistorySheet = true }
                                )
                        } else {
                                // --- ANSWER OUTPUT SCREEN LAYOUT ---
                                AnswerOutputContent(
                                        state = state,
                                        viewModel = viewModel,
                                        onNavigateToQuizzes = onNavigateToQuizzes
                                )
                        }

                        // --- HISTORY BOTTOM SHEET ---
                        if (showHistorySheet) {
                                HistoryBottomSheet(
                                        state = state,
                                        viewModel = viewModel,
                                        onDismiss = { showHistorySheet = false }
                                )
                        }
                }
        }
}

@Composable
fun ChatLandingContent(
        state: ChatState,
        viewModel: ChatViewModel,
        showAttachmentMenu: Boolean,
        onToggleAttachment: (Boolean) -> Unit,
        imagePickerLauncher: androidx.activity.result.ActivityResultLauncher<String>,
        cameraLauncher: androidx.activity.result.ActivityResultLauncher<Void?>,
        onShowHistory: () -> Unit
) {
        Column(
                modifier =
                        Modifier.fillMaxSize()
                                .statusBarsPadding()
                                .padding(bottom = 80.dp) // Space for navigation bar
                                .navigationBarsPadding()
                                .imePadding()
                                .verticalScroll(rememberScrollState())
                                .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
        ) {
                Spacer(modifier = Modifier.height(36.dp))

                // Mascot
                Image(
                        painter = painterResource(id = R.drawable.ic_chat),
                        contentDescription = "Eous Mascot",
                        modifier = Modifier.size(90.dp)
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Greeting text (Smaller text, no bold text styling)
                Text(
                        text = "Hello there!, I'm Eous. What can I do for you today?",
                        color = Color(0xFF64748B),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Normal,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 8.dp)
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Central Input Box Card (No shadow)
                Box(
                        modifier =
                                Modifier.fillMaxWidth()
                                        .background(Color.White, RoundedCornerShape(22.dp))
                                        .border(
                                                width = 1.dp,
                                                color = Color(0xFFE2E8F0),
                                                shape = RoundedCornerShape(22.dp)
                                        )
                                        .padding(16.dp)
                ) {
                        Column(modifier = Modifier.fillMaxWidth()) {
                                if (state.pendingImageUri != null) {
                                        Box(
                                                modifier =
                                                        Modifier.padding(bottom = 8.dp)
                                                                .size(80.dp)
                                                                .clip(RoundedCornerShape(8.dp))
                                        ) {
                                                AsyncImage(
                                                        model = state.pendingImageUri,
                                                        contentDescription = "Preview",
                                                        contentScale = ContentScale.Crop,
                                                        modifier = Modifier.fillMaxSize()
                                                )
                                                IconButton(
                                                        onClick = { viewModel.clearPendingImage() },
                                                        modifier =
                                                                Modifier.align(Alignment.TopEnd)
                                                                        .size(24.dp)
                                                                        .background(
                                                                                Color.Black.copy(
                                                                                        alpha = 0.5f
                                                                                ),
                                                                                CircleShape
                                                                        )
                                                ) {
                                                        Icon(
                                                                imageVector = Icons.Default.Close,
                                                                contentDescription = "Remove",
                                                                tint = Color.White,
                                                                modifier = Modifier.size(14.dp)
                                                        )
                                                }
                                        }
                                }

                                BasicTextField(
                                        value = state.inputText,
                                        onValueChange = { viewModel.onInputTextChanged(it) },
                                        textStyle =
                                                TextStyle(
                                                        color = Color(0xFF1E293B),
                                                        fontSize = 15.sp
                                                ),
                                        cursorBrush = SolidColor(EousPurple),
                                        modifier = Modifier.fillMaxWidth().heightIn(min = 80.dp),
                                        decorationBox = { innerTextField ->
                                                if (state.inputText.isEmpty()) {
                                                        Text(
                                                                text =
                                                                        "Let me know your problems...",
                                                                color = Color(0xFF94A3B8),
                                                                fontSize = 15.sp
                                                        )
                                                }
                                                innerTextField()
                                        }
                                )

                                Spacer(modifier = Modifier.height(16.dp))

                                Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                        Box {
                                                Box(
                                                        modifier =
                                                                Modifier.size(32.dp)
                                                                        .background(
                                                                                color = Color(0xFFE2E8F0),
                                                                                shape = CircleShape
                                                                        )
                                                                        .clip(CircleShape)
                                                                        .clickable {
                                                                                onToggleAttachment(
                                                                                        true
                                                                                )
                                                                        },
                                                        contentAlignment = Alignment.Center
                                                ) {
                                                        Icon(
                                                                imageVector = Icons.Default.Add,
                                                                contentDescription =
                                                                        "Attachment Menu",
                                                                tint = Color(0xFF94A3B8),
                                                                modifier = Modifier.size(16.dp)
                                                        )
                                                }
                                                DropdownMenu(
                                                        expanded = showAttachmentMenu,
                                                        onDismissRequest = {
                                                                onToggleAttachment(false)
                                                        },
                                                        containerColor = Color.White
                                                ) {
                                                        DropdownMenuItem(
                                                                text = {
                                                                        Text("Choose from library")
                                                                },
                                                                onClick = {
                                                                        onToggleAttachment(false)
                                                                        imagePickerLauncher.launch(
                                                                                "image/*"
                                                                        )
                                                                }
                                                        )
                                                        DropdownMenuItem(
                                                                text = { Text("Take a photo") },
                                                                onClick = {
                                                                        onToggleAttachment(false)
                                                                        cameraLauncher.launch(null)
                                                                }
                                                        )
                                                }
                                        }

                                        val isSendEnabled =
                                                state.inputText.isNotEmpty() ||
                                                        state.pendingImageUri != null
                                        Box(
                                                modifier =
                                                        Modifier.size(32.dp)
                                                                .background(
                                                                        color =
                                                                                if (isSendEnabled)
                                                                                        Color(
                                                                                                0xFF7F43D4
                                                                                        )
                                                                                else
                                                                                        Color(
                                                                                                0xFFE2E8F0
                                                                                        ),
                                                                        shape = CircleShape
                                                                )
                                                                .clip(CircleShape)
                                                                .clickable(
                                                                        enabled =
                                                                                isSendEnabled &&
                                                                                        !state.isSending
                                                                ) {
                                                                        viewModel.sendMessage()
                                                                },
                                                contentAlignment = Alignment.Center
                                        ) {
                                                Icon(
                                                        imageVector = Icons.Default.ArrowUpward,
                                                        contentDescription = "Send",
                                                        tint = Color.White,
                                                        modifier = Modifier.size(16.dp)
                                                )
                                        }
                                }
                        }
                }

                Spacer(modifier = Modifier.height(28.dp))

                // Suggestion questions
                Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.Start
                ) {
                        Text(
                                text = "Suggestion questions",
                                color = Color(0xFF64748B),
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(start = 16.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                                SuggestionChip(
                                        "Solve for x equation",
                                        onClick = { viewModel.onInputTextChanged(it) }
                                )
                                SuggestionChip(
                                        "Explain Bubble Sort",
                                        onClick = { viewModel.onInputTextChanged(it) }
                                )
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        SuggestionChip(
                                "Grammar English: Past Simple",
                                onClick = { viewModel.onInputTextChanged(it) }
                        )
                }

                Spacer(modifier = Modifier.height(28.dp))

                // Recent questions Card (With see all button)
                Card(
                        modifier =
                                Modifier.fillMaxWidth()
                                        .padding(bottom = 24.dp)
                                        .border(
                                                width = 1.dp,
                                                color = Color(0xFFE2E8F0),
                                                shape = RoundedCornerShape(24.dp)
                                        ),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                ) {
                                        Text(
                                                text = "Recent questions",
                                                color = Color(0xFF64748B),
                                                fontSize = 15.sp,
                                                fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                                text = "See all",
                                                color = Color(0xFF7F43D4),
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.clickable { onShowHistory() }
                                        )
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                if (state.sessions.isEmpty()) {
                                        Box(
                                                modifier =
                                                        Modifier.fillMaxWidth()
                                                                .padding(vertical = 24.dp),
                                                contentAlignment = Alignment.Center
                                        ) {
                                                Text(
                                                        text = "No recent questions yet",
                                                        color = Color(0xFF94A3B8),
                                                        fontSize = 14.sp,
                                                        fontWeight = FontWeight.Medium
                                                )
                                        }
                                } else {
                                        Column(
                                                verticalArrangement = Arrangement.spacedBy(10.dp)
                                        ) {
                                                state.sessions.take(4).forEach { session ->
                                                        RecentQuestionRowItem(
                                                                session = session,
                                                                onClick = {
                                                                        viewModel.selectSession(
                                                                                session
                                                                        )
                                                                }
                                                        )
                                                }
                                        }
                                }
                        }
                }
        }
}

@Composable
fun AnswerOutputContent(
        state: ChatState,
        viewModel: ChatViewModel,
        onNavigateToQuizzes: () -> Unit
) {
        val qaPairs =
                remember(state.messages) {
                        val pairs = mutableListOf<Pair<ChatMessage, ChatMessage?>>()
                        var i = 0
                        while (i < state.messages.size) {
                                val msg = state.messages[i]
                                if (msg.role == "user") {
                                        val nextMsg = state.messages.getOrNull(i + 1)
                                        if (nextMsg != null && nextMsg.role == "ai") {
                                                pairs.add(Pair(msg, nextMsg))
                                                i += 2
                                        } else {
                                                pairs.add(Pair(msg, null))
                                                i += 1
                                        }
                                } else {
                                        i += 1
                                }
                        }
                        pairs
                }

        Column(
                modifier =
                        Modifier.fillMaxSize()
                                .statusBarsPadding()
                                .navigationBarsPadding()
                                .imePadding()
        ) {
                // Header Bar
                Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                ) {
                        IconButton(
                                onClick = { viewModel.startNewChat() },
                                modifier =
                                        Modifier.size(32.dp)
                                                .background(Color.White, CircleShape)
                                                .border(1.dp, Color(0xFFE2E8F0), CircleShape)
                        ) {
                                Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                        contentDescription = "Back",
                                        tint = Color(0xFF1E293B),
                                        modifier = Modifier.size(18.dp)
                                )
                        }

                        val category = state.activeSession?.subject ?: "General"
                        Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = Color(0xFF1E293B)
                        ) {
                                Text(
                                        text = category,
                                        color = Color.White,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier =
                                                Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                                )
                        }

                        val latestAiMsg = qaPairs.lastOrNull()?.second
                        val isBookmarked = latestAiMsg?.is_bookmarked == true

                        IconButton(
                                onClick = {
                                        if (latestAiMsg != null) {
                                                viewModel.toggleBookmark(latestAiMsg)
                                        }
                                },
                                modifier =
                                        Modifier.size(32.dp)
                                                .background(Color.White, CircleShape)
                                                .border(1.dp, Color(0xFFE2E8F0), CircleShape)
                        ) {
                                Icon(
                                        imageVector =
                                                if (isBookmarked) Icons.Default.Bookmark
                                                else Icons.Default.BookmarkBorder,
                                        contentDescription = "Bookmark",
                                        tint =
                                                if (isBookmarked) Color(0xFF7F43D4)
                                                else Color(0xFF64748B),
                                        modifier = Modifier.size(18.dp)
                                )
                        }
                }

                // Render the entire chat thread in a Single Native WebView
                RichChatThreadView(
                        qaPairs = qaPairs,
                        isThinking = state.isAiResponding,
                        inputText = state.inputText,
                        pendingImageUrl = state.pendingImageUrl,
                        modifier = Modifier.fillMaxSize(),
                        onSupportChipClicked = { action ->
                                viewModel.onInputTextChanged(action)
                                viewModel.sendMessage()
                        },
                        onNavigateToQuizzes = onNavigateToQuizzes
                )
        }
}

@Composable
fun YourQuestionCard(questionText: String, imageUrl: String? = null) {
        Box(modifier = Modifier.fillMaxWidth().padding(top = 32.dp)) {
                // Mascot sticking out behind card
                Image(
                        painter = painterResource(id = R.drawable.ic_aianswer),
                        contentDescription = "Mascot peeking",
                        modifier = Modifier.align(Alignment.TopEnd).offset(x = (-16).dp, y = (-38).dp).size(72.dp)
                )

                Card(
                        modifier =
                                Modifier.fillMaxWidth()
                                        .border(
                                                width = 1.dp,
                                                color = Color(0xFFDDD6FE),
                                                shape = RoundedCornerShape(22.dp)
                                        ),
                        shape = RoundedCornerShape(22.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F3FF)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                                Text(
                                        text = "Your question",
                                        color = Color(0xFF7F43D4),
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(8.dp))

                                if (!imageUrl.isNullOrEmpty()) {
                                        AsyncImage(
                                                model = imageUrl,
                                                contentDescription = "Question Image",
                                                modifier =
                                                        Modifier.fillMaxWidth()
                                                                .heightIn(max = 200.dp)
                                                                .clip(RoundedCornerShape(12.dp))
                                                                .padding(bottom = 8.dp),
                                                contentScale = ContentScale.Crop
                                        )
                                }

                                Text(
                                        text = questionText,
                                        color = Color(0xFF1E293B),
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Medium
                                )
                        }
                }
        }
}

@Composable
fun ThinkingIndicator() {
        Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
        ) {
                CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp,
                        color = Color(0xFF7F43D4)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                        text = "Eous is thinking...",
                        color = Color(0xFF64748B),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                )
        }
}

@Composable
fun AiAnswerContent(
        aiMsg: ChatMessage,
        showSupportChips: Boolean,
        viewModel: ChatViewModel,
        onNavigateToQuizzes: () -> Unit
) {
        val parsed = remember(aiMsg.content) { AnswerParser.parse(aiMsg.content, aiMsg.subject) }

        Column(modifier = Modifier.fillMaxWidth()) {
                when (parsed.type) {
                        AnswerType.REFUSAL -> {
                                Card(
                                        modifier =
                                                Modifier.fillMaxWidth()
                                                        .border(
                                                                width = 1.dp,
                                                                color = Color(0xFFF1F5F9),
                                                                shape = RoundedCornerShape(16.dp)
                                                        ),
                                        shape = RoundedCornerShape(16.dp),
                                        colors =
                                                CardDefaults.cardColors(
                                                        containerColor = Color(0xFFF8FAFC)
                                                )
                                ) {
                                        RichTextView(
                                                text = parsed.explanation,
                                                textColor = "#475569",
                                                fontSize = "14px",
                                                modifier = Modifier.padding(16.dp)
                                        )
                                }
                        }
                        AnswerType.CONCEPT -> {
                                Text(
                                        text = "Mentor explanation",
                                        color = Color(0xFF64748B),
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                RichTextView(
                                        text = parsed.explanation,
                                        textColor = "#1E293B",
                                        fontSize = "15px"
                                )

                                if (parsed.steps.isNotEmpty()) {
                                        Spacer(modifier = Modifier.height(16.dp))
                                        Column(
                                                verticalArrangement = Arrangement.spacedBy(12.dp)
                                        ) {
                                                parsed.steps.forEachIndexed { index, step ->
                                                        StepRow(index + 1, step)
                                                }
                                        }
                                }
                        }
                        AnswerType.EXERCISE -> {
                                Text(
                                        text = "Mentor explanation",
                                        color = Color(0xFF64748B),
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                RichTextView(
                                        text = parsed.explanation,
                                        textColor = "#1E293B",
                                        fontSize = "15px"
                                )

                                if (!parsed.formula.isNullOrEmpty()) {
                                        Spacer(modifier = Modifier.height(16.dp))
                                        Box(
                                                modifier =
                                                        Modifier.fillMaxWidth()
                                                                .dashedBorder(
                                                                        1.5.dp,
                                                                        Color(0xFF7F43D4),
                                                                        12.dp
                                                                )
                                                                .background(
                                                                        Color(0xFFFAF9FF),
                                                                        RoundedCornerShape(12.dp)
                                                                )
                                                                .padding(16.dp),
                                                contentAlignment = Alignment.Center
                                        ) {
                                                val wrappedFormula = if (parsed.formula.trim().startsWith("$$") || parsed.formula.trim().startsWith("$")) parsed.formula else "$$" + parsed.formula + "$$"
                                                RichTextView(
                                                        text = wrappedFormula,
                                                        textColor = "#7F43D4",
                                                        fontSize = "16px",
                                                        alignment = "center"
                                                )
                                        }
                                }

                                if (parsed.steps.isNotEmpty()) {
                                        Spacer(modifier = Modifier.height(16.dp))
                                        Column(
                                                verticalArrangement = Arrangement.spacedBy(12.dp)
                                        ) {
                                                parsed.steps.forEachIndexed { index, step ->
                                                        StepRow(index + 1, step)
                                                }
                                        }
                                }

                                if (!parsed.conclusion.isNullOrEmpty()) {
                                        Spacer(modifier = Modifier.height(16.dp))
                                        Box(
                                                modifier =
                                                        Modifier.fillMaxWidth()
                                                                .dashedBorder(
                                                                        1.5.dp,
                                                                        Color(0xFF10B981),
                                                                        12.dp
                                                                )
                                                                .background(
                                                                        Color(0xFFF0FDF4),
                                                                        RoundedCornerShape(12.dp)
                                                                )
                                                                .padding(16.dp)
                                        ) {
                                                Column {
                                                        Text(
                                                                text = "Conclusion",
                                                                color = Color(0xFF10B981),
                                                                fontSize = 12.sp,
                                                                fontWeight = FontWeight.Bold
                                                        )
                                                        Spacer(modifier = Modifier.height(4.dp))
                                                        RichTextView(
                                                                text = parsed.conclusion,
                                                                textColor = "#047857",
                                                                fontSize = "14px"
                                                        )
                                                }
                                        }
                                }
                        }
                }

                // Render quiz card if attached
                if (!aiMsg.quiz_id.isNullOrEmpty()) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Card(
                                modifier =
                                        Modifier.fillMaxWidth()
                                                .border(
                                                        1.dp,
                                                        Color(0xFFC084FC),
                                                        RoundedCornerShape(16.dp)
                                                ),
                                shape = RoundedCornerShape(16.dp),
                                colors =
                                        CardDefaults.cardColors(
                                                containerColor = Color(0xFFFAF5FF)
                                        )
                        ) {
                                Column(
                                        modifier = Modifier.padding(16.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                        Text(
                                                text = "Practice Quiz is ready!",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 15.sp,
                                                color = Color(0xFF7E22CE)
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                                text =
                                                        "Test your understanding with a quick practice quiz.",
                                                fontSize = 13.sp,
                                                color = Color(0xFF6B21A8),
                                                textAlign = TextAlign.Center
                                        )
                                        Spacer(modifier = Modifier.height(12.dp))
                                        Button(
                                                onClick = onNavigateToQuizzes,
                                                colors =
                                                        ButtonDefaults.buttonColors(
                                                                containerColor = Color(0xFF7F43D4)
                                                        ),
                                                shape = RoundedCornerShape(20.dp)
                                        ) {
                                                Text(
                                                        text = "Start Quiz",
                                                        color = Color.White,
                                                        fontWeight = FontWeight.Bold
                                                )
                                        }
                                }
                        }
                }

                // Render support chips if active and last
                if (showSupportChips) {
                        Spacer(modifier = Modifier.height(20.dp))
                        Row(
                                modifier =
                                        Modifier.fillMaxWidth()
                                                .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                                SupportChip("Simplify Explanation") {
                                        viewModel.onInputTextChanged("Simplify the explanation")
                                        viewModel.sendMessage()
                                }
                                SupportChip("Another Solution") {
                                        viewModel.onInputTextChanged("Give another solution")
                                        viewModel.sendMessage()
                                }
                                SupportChip("Practice with Quizzes") {
                                        viewModel.onInputTextChanged(
                                                "Generate a practice quiz on this topic"
                                        )
                                        viewModel.sendMessage()
                                }
                        }
                }
        }
}

@Composable
fun StepRow(number: Int, text: String) {
        Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
                Box(
                        modifier =
                                Modifier.size(24.dp)
                                        .background(Color(0xFFE2E8F0), CircleShape),
                        contentAlignment = Alignment.Center
                ) {
                        Text(
                                text = number.toString(),
                                color = Color(0xFF475569),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                        )
                }
                RichTextView(
                        text = text,
                        textColor = "#334155",
                        fontSize = "14px",
                        modifier = Modifier.weight(1f)
                )
        }
}

@Composable
fun SupportChip(text: String, onClick: () -> Unit) {
        Surface(
                modifier =
                        Modifier.clickable { onClick() }
                                .border(
                                        width = 1.dp,
                                        color = Color(0xFFDDD6FE),
                                        shape = RoundedCornerShape(20.dp)
                                ),
                shape = RoundedCornerShape(20.dp),
                color = Color(0xFFF5F3FF)
        ) {
                Text(
                        text = text,
                        color = Color(0xFF7F43D4),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                )
        }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryBottomSheet(state: ChatState, viewModel: ChatViewModel, onDismiss: () -> Unit) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

        ModalBottomSheet(
                onDismissRequest = onDismiss,
                sheetState = sheetState,
                containerColor = Color.White,
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
        ) {
                Column(
                        modifier =
                                Modifier.fillMaxWidth()
                                        .navigationBarsPadding()
                                        .padding(horizontal = 24.dp, vertical = 8.dp)
                ) {
                        Text(
                                text = "History",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1E293B)
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        Column(
                                modifier =
                                        Modifier.fillMaxWidth()
                                                .heightIn(max = 400.dp)
                                                .verticalScroll(rememberScrollState()),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                                if (state.sessions.isEmpty()) {
                                        Text(
                                                text = "No history available",
                                                color = Color(0xFF94A3B8),
                                                fontSize = 14.sp
                                        )
                                } else {
                                        state.sessions.forEach { session ->
                                                RecentQuestionRowItem(
                                                        session = session,
                                                        onClick = {
                                                                viewModel.selectSession(session)
                                                                onDismiss()
                                                        }
                                                )
                                        }
                                }
                        }
                        Spacer(modifier = Modifier.height(24.dp))
                }
        }
}

@Composable
fun SuggestionChip(text: String, onClick: (String) -> Unit) {
        Surface(
                modifier =
                        Modifier.clickable { onClick(text) }
                                .border(
                                        width = 1.dp,
                                        color = Color(0xFFDDD6FE),
                                        shape = RoundedCornerShape(20.dp)
                                ),
                shape = RoundedCornerShape(20.dp),
                color = Color(0xFFF5F3FF)
        ) {
                Text(
                        text = text,
                        color = Color(0xFF3B2A6B),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                )
        }
}

@Composable
fun RecentQuestionRowItem(session: ChatSession, onClick: (ChatSession) -> Unit) {
        val category = session.subject
        Row(
                modifier =
                        Modifier.fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color(0xFFF3F0FF))
                                .clickable { onClick(session) }
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
        ) {
                Surface(shape = RoundedCornerShape(8.dp), color = Color(0xFF1E293B)) {
                        Text(
                                text = category,
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                        text = session.title,
                        color = Color(0xFF1E293B),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Normal,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                        text = formatRelativeTime(session.created_at),
                        color = Color(0xFF94A3B8),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Normal
                )
        }
}

private fun formatRelativeTime(createdAt: String?): String {
        if (createdAt.isNullOrEmpty()) return "now"
        try {
                val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS", Locale.US)
                sdf.timeZone = TimeZone.getTimeZone("UTC")
                val sessionDate: Date = sdf.parse(createdAt) ?: return "now"
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

