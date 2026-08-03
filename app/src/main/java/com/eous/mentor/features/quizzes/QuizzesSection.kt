package com.eous.mentor.features.quizzes

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.eous.mentor.core.ui.theme.*

@Composable
fun QuizzesSection() {
        Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                        containerColor = CardBackground.copy(alpha = 0.5f)
                ),
                modifier = Modifier
                        .fillMaxWidth()
                        .border(
                                1.dp,
                                BorderColor.copy(alpha = 0.5f),
                                RoundedCornerShape(20.dp)
                        )
        ) {
                Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                        Text(
                                text = "🧠 Smart Quizzes",
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                        )
                        Text(
                                text = "Generate custom quizzes based on your recent chats. Track scores, review mistakes, and earn experience points.",
                                color = MutedText,
                                fontSize = 13.sp,
                                lineHeight = 18.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                                onClick = {},
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(
                                        containerColor = EousPurple
                                )
                        ) {
                                Text(
                                        "Start Quiz",
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold
                                )
                        }
                }
        }
}
