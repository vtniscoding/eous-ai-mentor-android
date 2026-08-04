package com.eous.mentor.features.profile

import android.widget.Toast
import androidx.activity.compose.BackHandler
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronLeft
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.eous.mentor.R
import com.eous.mentor.core.ui.theme.Inter
import com.eous.mentor.di.RepositoryProvider
import com.eous.mentor.domain.model.Profile
import kotlinx.coroutines.launch

private val PrimaryPurple = Color(0xFF5B29A2)
private val DarkPurpleCardBg = Color(0xFF5821A6)
private val ButtonPurpleBg = Color(0xFFD6C7FF)
private val ButtonPurpleText = Color(0xFF4C1D95)

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
        description = "Grade 6-9: Core Foundations",
        icon = "📘"
    ),
    EducationOption(
        id = "high_school",
        title = "High School",
        description = "Grade 10-12: College Prep & SATs",
        icon = "🎒"
    ),
    EducationOption(
        id = "university",
        title = "University",
        description = "College & Beyond: In-depth analysis",
        icon = "🏛️"
    )
)

data class ExplanationStyleOption(
    val id: String,
    val title: String,
    val description: String,
    val icon: String
)

private val explanationOptions = listOf(
    ExplanationStyleOption(
        id = "short",
        title = "Short",
        description = "Direct answer, minimal text",
        icon = "⚡"
    ),
    ExplanationStyleOption(
        id = "detailed",
        title = "Detailed",
        description = "Comprehensive text and context",
        icon = "📖"
    ),
    ExplanationStyleOption(
        id = "step_by_step",
        title = "Step-by-step",
        description = "Breakdown concepts step-by-step",
        icon = "🪜"
    )
)

private val subjectOptions = listOf(
    "Math", "Physics", "Chemistry", "Biology", "English",
    "Geography", "History", "Programming", "Foreign Languages"
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ProfileScreen(
    userId: String,
    isForceOnboarding: Boolean = false,
    onBack: () -> Unit,
    onComplete: () -> Unit = {}
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val userRepository = RepositoryProvider.userRepository

    var userProfile by remember { mutableStateOf<Profile?>(null) }
    var selectedLevel by remember { mutableStateOf("high_school") }
    var selectedStyle by remember { mutableStateOf("detailed") }
    var selectedSubjects by remember { mutableStateOf<List<String>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var isSaving by remember { mutableStateOf(false) }

    // Intercept back press during forced onboarding
    if (isForceOnboarding) {
        BackHandler {
            // Do nothing, block back press
        }
    }

    LaunchedEffect(userId) {
        if (userId.isNotEmpty()) {
            isLoading = true
            userRepository.getProfile(userId).onSuccess { profile ->
                userProfile = profile
                profile?.education_level?.let { selectedLevel = it }
                profile?.explanation_style?.let { selectedStyle = it }
                profile?.subjects?.let { selectedSubjects = it }
            }
            isLoading = false
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color.White
    ) {
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
                    .navigationBarsPadding() // Avoid system navigation bar overlapping at bottom
            ) {
                // ---- Top Bar & Header ----
                if (!isForceOnboarding) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .statusBarsPadding()
                            .padding(horizontal = 16.dp, vertical = 6.dp)
                    ) {
                        IconButton(
                            onClick = onBack,
                            modifier = Modifier
                                .size(36.dp)
                                .align(Alignment.CenterStart)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ChevronLeft,
                                contentDescription = "Back",
                                tint = Color.Black
                            )
                        }
                    }
                } else {
                    Spacer(
                        modifier = Modifier
                            .statusBarsPadding()
                            .height(16.dp)
                    )
                }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = if (isForceOnboarding) "Set up your profile!" else "Update your profile!",
                        color = Color.Black,
                        fontSize = 28.sp,
                        lineHeight = 34.sp,
                        fontWeight = FontWeight.ExtraBold,
                        fontFamily = Inter,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Peak Mascot & Dark Purple Card Layout (Mascot behind card)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .wrapContentHeight()
                    ) {
                        // Robot Mascot (rendered first, placing it behind the card with downward offset)
                        Image(
                            painter = painterResource(id = R.drawable.ic_onboarding),
                            contentDescription = "Onboarding Mascot",
                            modifier = Modifier
                                .size(180.dp)
                                .offset(y = 18.dp)
                                .align(Alignment.TopCenter)
                        )

                        // Card Content (rendered on top of Mascot)
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 110.dp) // Covers lower body of robot mascot
                                .clip(RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp))
                                .background(DarkPurpleCardBg)
                                .padding(horizontal = 24.dp, vertical = 28.dp)
                        ) {
                            // ---- 1. Education Level Section ----
                            Text(
                                text = "What is your education level?",
                                color = Color.White,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = Inter
                            )
                            Text(
                                text = "This calibrates the difficulty level of your practice",
                                color = Color.White.copy(alpha = 0.7f),
                                fontSize = 13.sp,
                                fontFamily = Inter,
                                modifier = Modifier.padding(top = 2.dp, bottom = 14.dp)
                            )

                            educationOptions.forEachIndexed { index, option ->
                                val isSelected = selectedLevel == option.id
                                val optionInteractionSource = remember { MutableInteractionSource() }
                                val isOptionPressed by optionInteractionSource.collectIsPressedAsState()
                                val optionScale by animateFloatAsState(
                                    targetValue = if (isOptionPressed) 0.97f else 1.0f,
                                    animationSpec = spring(stiffness = Spring.StiffnessLow),
                                    label = "option_scale"
                                )

                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(bottom = 12.dp)
                                        .graphicsLayer {
                                            scaleX = optionScale
                                            scaleY = optionScale
                                        }
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(if (isSelected) ButtonPurpleBg else Color.White)
                                        .border(
                                            width = if (isSelected) 2.dp else 0.dp,
                                            color = if (isSelected) ButtonPurpleText else Color.Transparent,
                                            shape = RoundedCornerShape(16.dp)
                                        )
                                        .clickable(
                                            interactionSource = optionInteractionSource,
                                            indication = null
                                        ) {
                                            selectedLevel = option.id
                                        }
                                        .padding(horizontal = 16.dp, vertical = 10.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(34.dp)
                                                .clip(RoundedCornerShape(10.dp))
                                                .background(if (isSelected) Color(0xFFEDE9FE) else Color(0xFFF1F5F9)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(text = option.icon, fontSize = 18.sp)
                                        }

                                        Spacer(modifier = Modifier.width(12.dp))

                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = option.title,
                                                color = if (isSelected) ButtonPurpleText else Color(0xFF0F172A),
                                                fontSize = 15.sp,
                                                fontWeight = FontWeight.Bold,
                                                fontFamily = Inter
                                            )
                                            Text(
                                                text = option.description,
                                                color = if (isSelected) ButtonPurpleText.copy(alpha = 0.7f) else Color(0xFF64748B),
                                                fontSize = 12.sp,
                                                fontFamily = Inter
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(18.dp))

                            // ---- 2. Preferred Subjects Section ----
                            Text(
                                text = "Choose your prefer subjects",
                                color = Color.White,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = Inter
                            )
                            Text(
                                text = "Select one or multiple subjects you want to learn",
                                color = Color.White.copy(alpha = 0.7f),
                                fontSize = 13.sp,
                                fontFamily = Inter,
                                modifier = Modifier.padding(top = 2.dp, bottom = 14.dp)
                            )

                            FlowRow(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                subjectOptions.forEach { subject ->
                                    val isSelected = selectedSubjects.contains(subject)
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(20.dp))
                                            .background(if (isSelected) ButtonPurpleBg else Color.White)
                                            .border(
                                                width = if (isSelected) 1.5.dp else 0.dp,
                                                color = if (isSelected) ButtonPurpleText else Color.Transparent,
                                                shape = RoundedCornerShape(20.dp)
                                            )
                                            .clickable {
                                                selectedSubjects = if (isSelected) {
                                                    selectedSubjects.filter { it != subject }
                                                } else {
                                                    selectedSubjects + subject
                                                }
                                            }
                                            .padding(horizontal = 16.dp, vertical = 8.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = subject,
                                            color = if (isSelected) ButtonPurpleText else Color(0xFF0F172A),
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = Inter
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(28.dp))

                            // ---- 3. Preferred Explanation Style Section ----
                            Text(
                                text = "Preferred explanation style?",
                                color = Color.White,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = Inter
                            )
                            Text(
                                text = "How should AI mentor explain to you",
                                color = Color.White.copy(alpha = 0.7f),
                                fontSize = 13.sp,
                                fontFamily = Inter,
                                modifier = Modifier.padding(top = 2.dp, bottom = 14.dp)
                            )

                            explanationOptions.forEachIndexed { index, option ->
                                val isSelected = selectedStyle == option.id
                                val styleInteractionSource = remember { MutableInteractionSource() }
                                val isStylePressed by styleInteractionSource.collectIsPressedAsState()
                                val styleScale by animateFloatAsState(
                                    targetValue = if (isStylePressed) 0.97f else 1.0f,
                                    animationSpec = spring(stiffness = Spring.StiffnessLow),
                                    label = "style_scale"
                                )

                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(bottom = 12.dp)
                                        .graphicsLayer {
                                            scaleX = styleScale
                                            scaleY = styleScale
                                        }
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(if (isSelected) ButtonPurpleBg else Color.White)
                                        .border(
                                            width = if (isSelected) 2.dp else 0.dp,
                                            color = if (isSelected) ButtonPurpleText else Color.Transparent,
                                            shape = RoundedCornerShape(16.dp)
                                        )
                                        .clickable(
                                            interactionSource = styleInteractionSource,
                                            indication = null
                                        ) {
                                            selectedStyle = option.id
                                        }
                                        .padding(horizontal = 16.dp, vertical = 10.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(34.dp)
                                                .clip(RoundedCornerShape(10.dp))
                                                .background(if (isSelected) Color(0xFFEDE9FE) else Color(0xFFF1F5F9)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(text = option.icon, fontSize = 18.sp)
                                        }

                                        Spacer(modifier = Modifier.width(12.dp))

                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = option.title,
                                                color = if (isSelected) ButtonPurpleText else Color(0xFF0F172A),
                                                fontSize = 15.sp,
                                                fontWeight = FontWeight.Bold,
                                                fontFamily = Inter
                                            )
                                            Text(
                                                text = option.description,
                                                color = if (isSelected) ButtonPurpleText.copy(alpha = 0.7f) else Color(0xFF64748B),
                                                fontSize = 12.sp,
                                                fontFamily = Inter
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(28.dp))

                            // ---- Complete Button ----
                            val completeInteractionSource = remember { MutableInteractionSource() }
                            val isCompleteHovered by completeInteractionSource.collectIsHoveredAsState()
                            val isCompletePressed by completeInteractionSource.collectIsPressedAsState()
                            val completeScale by animateFloatAsState(
                                targetValue = if (isCompletePressed) 0.95f else if (isCompleteHovered) 1.03f else 1.0f,
                                animationSpec = spring(stiffness = Spring.StiffnessLow),
                                label = "complete_scale"
                            )

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(54.dp)
                                    .graphicsLayer {
                                        scaleX = completeScale
                                        scaleY = completeScale
                                    }
                            ) {
                                Button(
                                    onClick = {
                                        if (!isSaving) {
                                            isSaving = true
                                            coroutineScope.launch {
                                                userRepository.saveOnboardingProfile(
                                                    userId = userId,
                                                    educationLevel = selectedLevel,
                                                    explanationStyle = selectedStyle,
                                                    subjects = selectedSubjects
                                                ).onSuccess {
                                                    Toast.makeText(
                                                        context,
                                                        "Profile updated successfully!",
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                    onComplete()
                                                }.onFailure {
                                                    Toast.makeText(
                                                        context,
                                                        "Failed to update profile: ${it.message}",
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                }
                                                isSaving = false
                                            }
                                        }
                                    },
                                    interactionSource = completeInteractionSource,
                                    shape = RoundedCornerShape(20.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = ButtonPurpleBg),
                                    modifier = Modifier.fillMaxSize()
                                ) {
                                    if (isSaving) {
                                        CircularProgressIndicator(
                                            color = ButtonPurpleText,
                                            modifier = Modifier.size(20.dp),
                                            strokeWidth = 2.dp
                                        )
                                    } else {
                                        Text(
                                            text = "Complete",
                                            color = ButtonPurpleText,
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = Inter
                                        )
                                    }
                                }
                            }

                            // Substantial padding under the complete button (now set to 28.dp) to avoid system navigation overlapping
                            Spacer(modifier = Modifier.height(28.dp))
                        }
                    }
                }
            }
        }
    }
}
