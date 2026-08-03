package com.eous.mentor.features.auth.register

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.eous.mentor.core.ui.theme.*

fun getPasswordStrength(password: String): Int {
    if (password.isEmpty()) return -1
    if (password.length < 6) return 0 // Very Weak

    var score = 1 // Weak
    val hasDigit = password.any { it.isDigit() }
    val hasUpper = password.any { it.isUpperCase() }
    val hasLower = password.any { it.isLowerCase() }
    val hasSpecial = password.any { !it.isLetterOrDigit() }

    if (hasDigit && (hasUpper || hasLower)) {
        score = 2 // Fair
    }
    if (hasDigit && hasUpper && hasLower) {
        score = 3 // Good (Meets requirement: min 6 chars, uppercase, lowercase, digit)
    }
    if (hasDigit && hasUpper && hasLower && hasSpecial && password.length >= 8) {
        score = 4 // Strong
    }
    return score
}

@Composable
fun PasswordStrengthMeter(password: String) {
    val score = remember(password) { getPasswordStrength(password) }
    
    val scoreText = when (score) {
        0 -> "Very Weak"
        1 -> "Weak"
        2 -> "Fair"
        3 -> "Good"
        4 -> "Strong"
        else -> "Enter password"
    }
    
    val scoreColor = when (score) {
        0, 1 -> EousRed
        2 -> EousOrange
        3 -> EousYellow
        4 -> EousGreen
        else -> Color.Gray.copy(alpha = 0.2f)
    }
    
    Column(modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Password Strength",
                color = MutedText,
                fontSize = 12.sp
            )
            Text(
                scoreText,
                color = scoreColor,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }
        
        Spacer(modifier = Modifier.height(6.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            for (i in 0..3) {
                val active = score >= 0 && i <= score
                val color = if (active) scoreColor else Color.Gray.copy(alpha = 0.2f)
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(6.dp)
                        .clip(CircleShape)
                        .background(color)
                )
            }
        }
    }
}
