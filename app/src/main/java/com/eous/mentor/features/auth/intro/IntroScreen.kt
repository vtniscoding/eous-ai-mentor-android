package com.eous.mentor.features.auth.intro

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.paint
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.eous.mentor.core.navigation.navigateSafe
import com.eous.mentor.R
import com.eous.mentor.core.ui.theme.*

@Composable
fun AuthIntroScreen(
    navController: NavController
) {
    val signUpInteractionSource = remember { MutableInteractionSource() }
    val isSignUpPressed by signUpInteractionSource.collectIsPressedAsState()
    val isSignUpHovered by signUpInteractionSource.collectIsHoveredAsState()
    val signUpScale by animateFloatAsState(
        targetValue = if (isSignUpHovered || isSignUpPressed) 0.95f else 1.0f,
        label = "signup_scale"
    )

    val loginInteractionSource = remember { MutableInteractionSource() }
    val isLoginPressed by loginInteractionSource.collectIsPressedAsState()
    val isLoginHovered by loginInteractionSource.collectIsHoveredAsState()
    val loginScale by animateFloatAsState(
        targetValue = if (isLoginHovered || isLoginPressed) 0.95f else 1.0f,
        label = "login_scale"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .paint(
                painter = painterResource(id = R.drawable.intro_background),
                contentScale = ContentScale.FillBounds
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Branding & Copy
            Column(
                horizontalAlignment = Alignment.Start,
                modifier = Modifier.fillMaxWidth(0.7f)
            ) {
                Text(
                    text = "Let's pop\nsome task!",
                    color = Color.Black,
                    fontSize = 36.sp,
                    fontFamily = Inter,
                    fontWeight = FontWeight.Bold,
                    lineHeight = 38.sp,
                    textAlign = TextAlign.Start
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Say goodbye to study stress. Welcome to Eous, your best AI Mentor!",
                    color = Color.Black,
                    fontSize = 13.5.sp,
                    fontFamily = Inter,
                    fontWeight = FontWeight.Normal,
                    lineHeight = 17.sp,
                    textAlign = TextAlign.Start
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Action Buttons
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 100.dp), // Pushes buttons up higher relative to the bottom mascot
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Sign Up capsule button
                Button(
                    onClick = { navController.navigateSafe("register") },
                    shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                    contentPadding = PaddingValues(),
                    interactionSource = signUpInteractionSource,
                    modifier = Modifier
                        .fillMaxWidth(0.7f)
                        .height(50.dp)
                        .graphicsLayer {
                            scaleX = signUpScale
                            scaleY = signUpScale
                        }
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.horizontalGradient(listOf(EousPurple, EousIndigo)),
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "Sign Up",
                            color = Color.White,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }

                // Log In Outline button
                OutlinedButton(
                    onClick = { navController.navigateSafe("login") },
                    border = BorderStroke(1.5.dp, EousPurple),
                    colors = ButtonDefaults.outlinedButtonColors(containerColor = Color.Transparent),
                    shape = CircleShape,
                    interactionSource = loginInteractionSource,
                    modifier = Modifier
                        .fillMaxWidth(0.7f)
                        .height(50.dp)
                        .graphicsLayer {
                            scaleX = loginScale
                            scaleY = loginScale
                        }
                ) {
                    Text(
                        "Log In",
                        color = EousPurple,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }
        }
    }
}

