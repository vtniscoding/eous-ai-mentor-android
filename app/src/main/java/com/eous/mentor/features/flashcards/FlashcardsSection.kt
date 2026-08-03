package com.eous.mentor.features.flashcards

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
fun FlashcardsSection() {
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
                                text = "⚡ Active Recall Flashcards",
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                        )
                        Text(
                                text = "Automate flashcard sets from textbook screenshots or session notes. Review using spaced repetition.",
                                color = MutedText,
                                fontSize = 13.sp,
                                lineHeight = 18.sp
                        )
                }
        }
}
