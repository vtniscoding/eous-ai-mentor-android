package com.eous.mentor.features.profile

import android.widget.Toast
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.outlined.RadioButtonUnchecked
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.eous.mentor.core.ui.theme.EousPurple
import com.eous.mentor.core.ui.theme.Inter
import com.eous.mentor.di.RepositoryProvider
import com.eous.mentor.domain.model.Profile
import kotlinx.coroutines.launch

private val PrimaryPurple = Color(0xFF5B29A2)

data class EducationOption(
    val id: String,
    val title: String,
    val description: String,
    val icon: String
)

private val educationOptions = listOf(
    EducationOption(
        id = "middle_school",
        title = "Middle School",
        description = "Clear explanations with basic fundamentals",
        icon = "📘"
    ),
    EducationOption(
        id = "high_school",
        title = "High School",
        description = "Structured concepts for academic prep & exams",
        icon = "🎒"
    ),
    EducationOption(
        id = "university",
        title = "University / College",
        description = "In-depth analysis, formulas & practical applications",
        icon = "🏛️"
    )
)

@Composable
fun ProfileScreen(
    userId: String,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val userRepository = RepositoryProvider.userRepository

    var userProfile by remember { mutableStateOf<Profile?>(null) }
    var selectedLevel by remember { mutableStateOf("high_school") }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(userId) {
        if (userId.isNotEmpty()) {
            isLoading = true
            userRepository.getProfile(userId).onSuccess { profile ->
                userProfile = profile
                profile?.education_level?.let { selectedLevel = it }
            }
            isLoading = false
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFFFAFAFA)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            // ---- Top Bar ----
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.Black
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Profile Settings",
                        color = Color.Black,
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = Inter
                    )
                }

                // Styled Done Button
                val doneInteractionSource = remember { MutableInteractionSource() }
                val isDoneHovered by doneInteractionSource.collectIsHoveredAsState()
                val isDonePressed by doneInteractionSource.collectIsPressedAsState()
                val doneScale by animateFloatAsState(
                    targetValue = if (isDonePressed) 0.94f else if (isDoneHovered) 1.04f else 1.0f,
                    animationSpec = spring(stiffness = Spring.StiffnessLow),
                    label = "done_scale"
                )

                Box(
                    modifier = Modifier.graphicsLayer {
                        scaleX = doneScale
                        scaleY = doneScale
                    }
                ) {
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .offset(x = 3.dp, y = 3.dp)
                            .background(Color(0xFF4C1D95), RoundedCornerShape(20.dp))
                    )
                    Box(
                        modifier = Modifier
                            .background(
                                if (isDonePressed || isDoneHovered) Color(0xFFC4B2FF) else Color(0xFFD6C7FF),
                                RoundedCornerShape(20.dp)
                            )
                            .clickable(
                                interactionSource = doneInteractionSource,
                                indication = null
                            ) { onBack() }
                            .padding(horizontal = 20.dp, vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Done",
                            color = Color(0xFF4C1D95),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = Inter
                        )
                    }
                }
            }

            HorizontalDivider(color = Color(0xFFE2E8F0), thickness = 1.dp)

            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = PrimaryPurple)
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 20.dp, vertical = 20.dp)
                ) {
                    // ---- User Info Card ----
                    val displayName = userProfile?.display_name
                        ?: userProfile?.email?.substringBefore("@")
                        ?: "Student"
                    val avatarUrl = userProfile?.avatar_url
                    val initials = displayName
                        .split(" ")
                        .filter { it.isNotEmpty() }
                        .map { it.first().uppercase() }
                        .joinToString("")
                        .take(2)
                        .ifEmpty { "U" }

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.5.dp, Color(0xFFE2E8F0), RoundedCornerShape(20.dp)),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (!avatarUrl.isNullOrEmpty()) {
                                AsyncImage(
                                    model = avatarUrl,
                                    contentDescription = "Avatar",
                                    modifier = Modifier
                                        .size(56.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFFEDE9FE))
                                )
                            } else {
                                Box(
                                    modifier = Modifier
                                        .size(56.dp)
                                        .clip(CircleShape)
                                        .background(EousPurple),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = initials,
                                        color = Color.White,
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(16.dp))

                            Column {
                                Text(
                                    text = displayName,
                                    color = Color(0xFF0F172A),
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = Inter
                                )
                                userProfile?.email?.let { email ->
                                    Text(
                                        text = email,
                                        color = Color(0xFF64748B),
                                        fontSize = 13.sp,
                                        fontFamily = Inter
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // ---- Education Level Section ----
                    Text(
                        text = "EDUCATION LEVEL",
                        color = Color(0xFF64748B),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = Inter,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Customize your academic level so AI Mentor adapts its terminology and explanation depth.",
                        color = Color(0xFF475569),
                        fontSize = 13.sp,
                        fontFamily = Inter
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.5.dp, Color(0xFF94A3B8), RoundedCornerShape(20.dp)),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Column {
                            educationOptions.forEachIndexed { index, option ->
                                val isSelected = selectedLevel == option.id
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(
                                            if (isSelected) Color(0xFFF3E8FF) else Color.White
                                        )
                                        .clickable {
                                            selectedLevel = option.id
                                            coroutineScope.launch {
                                                userRepository.updateEducationLevel(userId, option.id)
                                                    .onSuccess {
                                                        Toast.makeText(
                                                            context,
                                                            "Education level set to ${option.title}",
                                                            Toast.LENGTH_SHORT
                                                        ).show()
                                                    }
                                                    .onFailure {
                                                        Toast.makeText(
                                                            context,
                                                            "Failed to update education level",
                                                            Toast.LENGTH_SHORT
                                                        ).show()
                                                    }
                                            }
                                        }
                                        .padding(horizontal = 16.dp, vertical = 14.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = option.icon,
                                        fontSize = 22.sp
                                    )

                                    Spacer(modifier = Modifier.width(12.dp))

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = option.title,
                                            color = if (isSelected) PrimaryPurple else Color(0xFF0F172A),
                                            fontSize = 15.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                                            fontFamily = Inter
                                        )
                                        Text(
                                            text = option.description,
                                            color = Color(0xFF64748B),
                                            fontSize = 12.sp,
                                            fontFamily = Inter
                                        )
                                    }

                                    if (isSelected) {
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = "Selected",
                                            tint = PrimaryPurple,
                                            modifier = Modifier.size(22.dp)
                                        )
                                    } else {
                                        Icon(
                                            imageVector = Icons.Outlined.RadioButtonUnchecked,
                                            contentDescription = "Not selected",
                                            tint = Color(0xFFCBD5E1),
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }
                                }

                                if (index < educationOptions.size - 1) {
                                    HorizontalDivider(color = Color(0xFFF1F5F9), thickness = 1.dp)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(36.dp))
                }
            }
        }
    }
}
