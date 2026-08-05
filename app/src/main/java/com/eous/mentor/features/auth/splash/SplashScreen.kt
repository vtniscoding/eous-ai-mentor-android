package com.eous.mentor.features.auth.splash

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.eous.mentor.R
import com.eous.mentor.core.navigation.navigateSafe
import com.eous.mentor.core.ui.theme.Inter
import com.eous.mentor.core.ui.theme.EousPurple
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun SplashScreen(
    navController: NavController,
    targetDestination: String,
    isInitialized: Boolean
) {
    // Animation states
    val mascotScale = remember { Animatable(0f) }
    val mascotRotation = remember { Animatable(0f) }
    
    val heartScale = remember { Animatable(0f) }
    val heartAlpha = remember { Animatable(0f) }
    
    val textAlpha = remember { Animatable(0f) }
    val textOffsetY = remember { Animatable(20f) }
    
    // Infinite floating animations
    val infiniteTransition = rememberInfiniteTransition(label = "floating")
    
    val mascotFloatY by infiniteTransition.animateFloat(
        initialValue = -5f,
        targetValue = 5f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "mascot_float"
    )
    
    val heartFloatY by infiniteTransition.animateFloat(
        initialValue = -8f,
        targetValue = 8f,
        animationSpec = infiniteRepeatable(
            animation = tween(1600, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "heart_float"
    )

    // Trigger animations sequentially
    LaunchedEffect(Unit) {
        // 1. Mascot pop-in & tilt
        launch {
            mascotScale.animateTo(
                targetValue = 1f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                )
            )
            mascotRotation.animateTo(
                targetValue = -3f,
                animationSpec = tween(400, easing = EaseOutQuad)
            )
        }
        
        // 2. Hello! Text & Heart slide-up & fade-in together
        launch {
            delay(500)
            launch {
                textAlpha.animateTo(
                    targetValue = 1f,
                    animationSpec = tween(800, easing = EaseOutCubic)
                )
            }
            launch {
                textOffsetY.animateTo(
                    targetValue = 0f,
                    animationSpec = tween(800, easing = EaseOutCubic)
                )
            }
            launch {
                heartScale.animateTo(
                    targetValue = 1f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessLow
                    )
                )
            }
            launch {
                heartAlpha.animateTo(
                    targetValue = 1f,
                    animationSpec = tween(500)
                )
            }
        }
    }
    
    // Navigate when initialized and minimum animation time is met
    LaunchedEffect(isInitialized) {
        if (isInitialized) {
            // Keep splash screen visible for at least 2.5 seconds total
            delay(2500)
            navController.navigateSafe(targetDestination) {
                popUpTo("splash") { inclusive = true }
            }
        }
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFFFFFFF),
                        Color(0xFFF7F5FF)
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            // Hello Text + Heart
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier
                    .alpha(textAlpha.value)
                    .graphicsLayer {
                        translationY = textOffsetY.value
                    }
            ) {
                Text(
                    text = "Hello!",
                    color = Color.Black,
                    fontSize = 32.sp,
                    fontFamily = Inter,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.width(8.dp))
                
                Image(
                    painter = painterResource(id = R.drawable.ic_heart),
                    contentDescription = "Heart",
                    modifier = Modifier
                        .size(32.dp)
                        .graphicsLayer {
                            translationY = heartFloatY
                            scaleX = heartScale.value
                            scaleY = heartScale.value
                            alpha = heartAlpha.value
                        }
                )
            }
            
            Spacer(modifier = Modifier.height(28.dp))
            
            // Mascot Container (smaller size: 180.dp)
            Box(
                modifier = Modifier
                    .size(180.dp)
                    .graphicsLayer {
                        translationY = mascotFloatY
                        scaleX = mascotScale.value
                        scaleY = mascotScale.value
                        rotationZ = mascotRotation.value
                    },
                contentAlignment = Alignment.Center
            ) {
                // Eous Mascot (ic_greeting_eous_1)
                Image(
                    painter = painterResource(id = R.drawable.ic_greeting_eous_1),
                    contentDescription = "Eous Mascot",
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}
