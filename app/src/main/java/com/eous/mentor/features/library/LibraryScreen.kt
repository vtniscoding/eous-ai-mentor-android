package com.eous.mentor.features.library

import android.widget.Toast
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.eous.mentor.R
import com.eous.mentor.core.ui.components.EousConfirmDialog
import com.eous.mentor.core.ui.theme.*
import com.eous.mentor.domain.model.ChatMessage

private val SubjectHighlightColor = Color(0xFF5B29A2)

private data class SubjectItemData(
    val name: String,
    val count: Int,
    val isAll: Boolean = false,
    val isAdd: Boolean = false
)

@Composable
fun Library(
    onMenuClick: () -> Unit,
    userId: String,
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

    var showAddSubjectDialog by remember { mutableStateOf(false) }
    var showManageSubjectsDialog by remember { mutableStateOf(false) }
    var newSubjectName by remember { mutableStateOf("") }
    
    // State for unmark confirmation dialog
    var showUnmarkConfirmDialog by remember { mutableStateOf(false) }
    var messageToUnmark by remember { mutableStateOf<ChatMessage?>(null) }

    // State for delete subject confirmation dialog
    var showDeleteSubjectConfirmDialog by remember { mutableStateOf(false) }
    var subjectToDelete by remember { mutableStateOf<String?>(null) }

    // Subject items computed for the UI grid
    val subjectItems = remember(state.subjects, state.bookmarkedMessages, state.selectedSubject) {
        val list = mutableListOf<SubjectItemData>()
        
        // All subjects item
        val allCount = state.bookmarkedMessages.size
        list.add(SubjectItemData(name = "All subjects", count = allCount, isAll = true))
        
        // Custom/Default subjects
        state.subjects.forEach { subject ->
            val count = state.bookmarkedMessages.count { msg ->
                val folder = msg.bookmark_folder ?: "General"
                if (subject == "Foreign Languages" || subject == "Languages") {
                    folder == "Foreign Languages" || folder == "Languages"
                } else {
                    folder.equals(subject, ignoreCase = true)
                }
            }
            list.add(SubjectItemData(name = subject, count = count, isAll = false))
        }
        
        list
    }

    // Bookmarks list content filtered by selected subject
    val filteredMessages = remember(state.bookmarkedMessages, state.selectedSubject) {
        if (state.selectedSubject == null) {
            state.bookmarkedMessages
        } else {
            state.bookmarkedMessages.filter { msg ->
                val folder = msg.bookmark_folder ?: "General"
                val targetSubject = state.selectedSubject
                if (targetSubject == "Foreign Languages" || targetSubject == "Languages") {
                    folder == "Foreign Languages" || folder == "Languages"
                } else {
                    folder.equals(targetSubject, ignoreCase = true)
                }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Header Bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(elevation = 4.dp)
                    .zIndex(1f)
                    .background(Color.White)
                    .statusBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 18.dp)
            ) {
                Text(
                    text = "Personal Library",
                    color = Color.Black,
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = Inter
                )
            }

            // Scrollable Layout
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp),
                contentPadding = PaddingValues(top = 16.dp, bottom = 140.dp)
            ) {
                // Organized Subjects card
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFF1F5F9), RoundedCornerShape(24.dp))
                            .border(width = 1.dp, color = Color(0xFFE2E8F0), shape = RoundedCornerShape(24.dp))
                            .padding(horizontal = 20.dp, vertical = 16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(
                                modifier = Modifier.weight(1.3f),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = "Organized Subjects",
                                    color = SubjectHighlightColor,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = Inter
                                )
                                Text(
                                    text = "Bookmark important answers during conversation, select a subject, and organize them here for review",
                                    color = Color(0xFF475569),
                                    fontSize = 12.sp,
                                    lineHeight = 16.sp,
                                    fontFamily = Inter
                                )
                            }
                            Image(
                                painter = painterResource(id = R.drawable.ic_lib_1),
                                contentDescription = "Mascot Library",
                                modifier = Modifier
                                    .weight(0.7f)
                                    .height(90.dp)
                            )
                        }
                    }
                }

                // Your Subjects title with Manage Edit button
                item {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "Your Subjects",
                                color = Color.Black,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = Inter
                            )
                            Image(
                                painter = painterResource(id = R.drawable.ic_heart),
                                contentDescription = "Heart Icon",
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        // Manage Custom Subjects Button
                        TextButton(
                            onClick = { showManageSubjectsDialog = true }
                        ) {
                            Text(
                                text = "Manage",
                                color = SubjectHighlightColor,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = Inter
                            )
                        }
                    }
                }

                // Grid of Subjects
                item {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        val rows = subjectItems.chunked(3)
                        rows.forEach { rowItems ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                rowItems.forEach { item ->
                                    Box(modifier = Modifier.weight(1f)) {
                                        SubjectCard(
                                            item = item,
                                            selectedSubject = state.selectedSubject,
                                            onSelectSubject = { viewModel.selectSubject(it) }
                                        )
                                    }
                                }
                                // Pad row if it has fewer than 3 items
                                if (rowItems.size < 3) {
                                    repeat(3 - rowItems.size) {
                                        Spacer(modifier = Modifier.weight(1f))
                                    }
                                }
                            }
                        }
                    }
                }

                // Recent Bookmarks title
                item {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Recent Bookmarks",
                            color = Color.Black,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = Inter
                        )
                        Image(
                            painter = painterResource(id = R.drawable.ic_heart),
                            contentDescription = "Heart Icon",
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                if (filteredMessages.isEmpty()) {
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
                                    "No bookmarked answers in this subject yet.",
                                    color = Color(0xFF64748B),
                                    fontSize = 13.sp,
                                    textAlign = TextAlign.Center,
                                    fontFamily = Inter
                                )
                            }
                        }
                    }
                } else {
                    items(filteredMessages, key = { it.id ?: it.hashCode() }) { msg ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFE2E2E2)),
                            shape = RoundedCornerShape(20.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                // Tag & Delete Button
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .background(Color(0xFF252425), RoundedCornerShape(8.dp))
                                            .padding(horizontal = 10.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = msg.bookmark_folder ?: "General",
                                            color = Color.White,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = Inter
                                        )
                                    }

                                    IconButton(
                                        onClick = {
                                            messageToUnmark = msg
                                            showUnmarkConfirmDialog = true
                                        },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "Remove Bookmark",
                                            tint = Color.Black,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }

                                // Message content
                                Text(
                                    text = msg.content,
                                    color = Color.Black,
                                    fontSize = 13.sp,
                                    lineHeight = 18.sp,
                                    fontFamily = Inter
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Add Custom Subject Dialog
    if (showAddSubjectDialog) {
        AlertDialog(
            onDismissRequest = {
                showAddSubjectDialog = false
                newSubjectName = ""
            },
            title = {
                Text(
                    text = "Add Custom Subject",
                    color = Color.Black,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    fontFamily = Inter
                )
            },
            containerColor = Color.White,
            textContentColor = Color.Black,
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        "Create a custom subject to organize your bookmarked AI answers:",
                        color = Color(0xFF475569),
                        fontSize = 13.sp,
                        fontFamily = Inter
                    )
                    OutlinedTextField(
                        value = newSubjectName,
                        onValueChange = { newSubjectName = it },
                        placeholder = { Text("Subject name...", color = Color(0xFF94A3B8)) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.Black,
                            unfocusedTextColor = Color.Black,
                            focusedBorderColor = SubjectHighlightColor,
                            unfocusedBorderColor = Color.Black.copy(alpha = 0.1f)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val name = newSubjectName.trim()
                        if (name.isNotEmpty()) {
                            viewModel.addSubject(userId, name)
                        }
                        showAddSubjectDialog = false
                        newSubjectName = ""
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SubjectHighlightColor)
                ) {
                    Text("Add Subject", color = Color.White, fontFamily = Inter)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showAddSubjectDialog = false
                        newSubjectName = ""
                    }
                ) {
                    Text("Cancel", color = Color(0xFF64748B), fontFamily = Inter)
                }
            }
        )
    }

    // State for renaming a subject in Manage Subjects dialog
    var subjectToEdit by remember { mutableStateOf<String?>(null) }
    var editedSubjectName by remember { mutableStateOf("") }

    // Manage Subjects Dialog
    if (showManageSubjectsDialog) {
        AlertDialog(
            onDismissRequest = {
                showManageSubjectsDialog = false
                subjectToEdit = null
                editedSubjectName = ""
            },
            title = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = null,
                            tint = SubjectHighlightColor,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "Manage Subjects",
                            color = Color.Black,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            fontFamily = Inter
                        )
                    }
                    IconButton(
                        onClick = { showAddSubjectDialog = true },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add Subject",
                            tint = SubjectHighlightColor,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            },
            containerColor = Color.White,
            textContentColor = Color.Black,
            text = {
                if (state.subjects.isEmpty()) {
                    Text(
                        "No subjects found. Add a subject using the '+' button.",
                        color = Color(0xFF64748B),
                        fontSize = 13.sp,
                        fontFamily = Inter
                    )
                } else {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        state.subjects.forEach { topic ->
                            val isEditingThis = (subjectToEdit == topic)

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color(0xFFF1F5F9), RoundedCornerShape(12.dp))
                                    .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(12.dp))
                                    .padding(horizontal = 10.dp, vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (isEditingThis) {
                                    OutlinedTextField(
                                        value = editedSubjectName,
                                        onValueChange = { editedSubjectName = it },
                                        singleLine = true,
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedTextColor = Color.Black,
                                            unfocusedTextColor = Color.Black,
                                            focusedBorderColor = SubjectHighlightColor,
                                            unfocusedBorderColor = Color(0xFFCBD5E1)
                                        ),
                                        modifier = Modifier.weight(1f).height(48.dp)
                                    )
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        IconButton(
                                            onClick = {
                                                if (editedSubjectName.trim().isNotEmpty()) {
                                                    viewModel.renameSubject(userId, topic, editedSubjectName.trim())
                                                }
                                                subjectToEdit = null
                                                editedSubjectName = ""
                                            },
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = "Save",
                                                tint = Color(0xFF10B981),
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                        IconButton(
                                            onClick = {
                                                subjectToEdit = null
                                                editedSubjectName = ""
                                            },
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Close,
                                                contentDescription = "Cancel",
                                                tint = Color(0xFF64748B),
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }
                                } else {
                                    Text(
                                        text = topic,
                                        color = Color.Black,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 14.sp,
                                        fontFamily = Inter,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        IconButton(
                                            onClick = {
                                                subjectToEdit = topic
                                                editedSubjectName = topic
                                            },
                                            modifier = Modifier.size(28.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Edit,
                                                contentDescription = "Rename Subject",
                                                tint = SubjectHighlightColor,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                        IconButton(
                                            onClick = {
                                                subjectToDelete = topic
                                                showDeleteSubjectConfirmDialog = true
                                            },
                                            modifier = Modifier.size(28.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Delete,
                                                contentDescription = "Delete Subject",
                                                tint = Color.Red.copy(alpha = 0.8f),
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showManageSubjectsDialog = false
                        subjectToEdit = null
                        editedSubjectName = ""
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SubjectHighlightColor)
                ) {
                    Text("Done", color = Color.White, fontFamily = Inter)
                }
            }
        )
    }

    // Confirm Delete Subject Dialog
    if (showDeleteSubjectConfirmDialog && subjectToDelete != null) {
        EousConfirmDialog(
            title = "Delete Subject",
            message = "Are you sure you want to delete subject \"$subjectToDelete\"?",
            confirmText = "Delete",
            dismissText = "Cancel",
            isDestructive = true,
            onConfirm = {
                subjectToDelete?.let { topic ->
                    viewModel.removeSubject(userId, topic)
                }
                showDeleteSubjectConfirmDialog = false
                subjectToDelete = null
            },
            onDismiss = {
                showDeleteSubjectConfirmDialog = false
                subjectToDelete = null
            }
        )
    }

    // Confirm Unmark Dialog
    if (showUnmarkConfirmDialog) {
        EousConfirmDialog(
            title = "Remove Bookmark",
            message = "Are you sure you want to remove this answer from your library?",
            confirmText = "Remove",
            dismissText = "Cancel",
            isDestructive = true,
            onConfirm = {
                messageToUnmark?.let { msg ->
                    viewModel.removeBookmark(msg, userId)
                }
                showUnmarkConfirmDialog = false
                messageToUnmark = null
            },
            onDismiss = {
                showUnmarkConfirmDialog = false
                messageToUnmark = null
            }
        )
    }
}

@Composable
private fun SubjectCard(
    item: SubjectItemData,
    selectedSubject: String?,
    onSelectSubject: (String?) -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else if (isHovered) 1.03f else 1.0f,
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "card_scale"
    )

    val shadowOffsetX by animateDpAsState(
        targetValue = if (isPressed) 2.dp else if (isHovered) 6.dp else 4.dp,
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "shadow_offset_x"
    )

    val shadowOffsetY by animateDpAsState(
        targetValue = if (isPressed) 2.dp else if (isHovered) 6.dp else 4.dp,
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "shadow_offset_y"
    )

    val isSelected = if (item.isAll) selectedSubject == null else selectedSubject == item.name
    val shadowColor = if (isSelected) SubjectHighlightColor else Color(0xFF475569)
    val displayName = item.name

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
    ) {
        // Subject card
        Box(
            modifier = Modifier
                .fillMaxSize()
                .offset(x = shadowOffsetX, y = shadowOffsetY)
                .background(shadowColor, RoundedCornerShape(20.dp))
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White, RoundedCornerShape(20.dp))
                .border(
                    width = if (isSelected) 2.dp else 1.dp,
                    color = if (isSelected) SubjectHighlightColor else Color(0xFF475569).copy(alpha = 0.5f),
                    shape = RoundedCornerShape(20.dp)
                )
                .clickable(
                    interactionSource = interactionSource,
                    indication = null
                ) {
                    if (item.isAll) {
                        onSelectSubject(null)
                    } else {
                        onSelectSubject(item.name)
                    }
                }
                .padding(horizontal = 4.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = displayName,
                    color = if (isSelected) SubjectHighlightColor else Color.Black,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    fontFamily = Inter
                )
                Text(
                    text = "${item.count} item" + (if (item.count != 1) "s" else ""),
                    color = Color(0xFF64748B),
                    fontSize = 11.5.sp,
                    textAlign = TextAlign.Center,
                    fontFamily = Inter
                )
            }
        }
    }
}
