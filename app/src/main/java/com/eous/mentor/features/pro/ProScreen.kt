package com.eous.mentor.features.pro

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.eous.mentor.core.ui.theme.*

@Composable
fun Pro(
    onMenuClick: () -> Unit,
    viewModel: ProViewModel = remember { ProViewModel() }
) {
    val state by viewModel.state.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // High-end vibrant glowing circles for Eous Pro screen
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .size(320.dp)
                .blur(80.dp)
                .background(Brush.radialGradient(listOf(EousPurple.copy(alpha = 0.2f), Color.Transparent)))
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 15.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Eous Pro",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .background(EousPurple.copy(alpha = 0.15f), RoundedCornerShape(10.dp))
                        .border(1.dp, EousPurple.copy(alpha = 0.25f), RoundedCornerShape(10.dp))
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "⭐ Premium Plan",
                        color = Purple80,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Text(
                    text = "Unlock Unlimited Learning Power",
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.ExtraBold,
                    lineHeight = 30.sp
                )

                Text(
                    text = "Gain access to state-of-the-art features to speed up your study prep by 10x.",
                    color = MutedText,
                    fontSize = 14.sp
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Premium Features checklist
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    state.features.forEach { feature ->
                        FeatureCheckRow(feature)
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = {},
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                    contentPadding = PaddingValues(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.horizontalGradient(listOf(EousPurple, EousIndigo)),
                                RoundedCornerShape(12.dp)
                              ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "Upgrade Now — ${state.price}",
                            color = Color.White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
