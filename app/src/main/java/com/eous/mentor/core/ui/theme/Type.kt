package com.eous.mentor.core.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.eous.mentor.R

val Inter = FontFamily(
    Font(R.font.inter_regular, FontWeight.Normal),
    Font(R.font.inter_bold, FontWeight.Bold)
)

val Typography = Typography(
    displayLarge = TextStyle(fontFamily = Inter),
    displayMedium = TextStyle(fontFamily = Inter),
    displaySmall = TextStyle(fontFamily = Inter),
    headlineLarge = TextStyle(fontFamily = Inter),
    headlineMedium = TextStyle(fontFamily = Inter),
    headlineSmall = TextStyle(fontFamily = Inter),
    titleLarge = TextStyle(fontFamily = Inter),
    titleMedium = TextStyle(fontFamily = Inter),
    titleSmall = TextStyle(fontFamily = Inter),
    bodyLarge = TextStyle(
        fontFamily = Inter,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    ),
    bodyMedium = TextStyle(fontFamily = Inter),
    bodySmall = TextStyle(fontFamily = Inter),
    labelLarge = TextStyle(fontFamily = Inter),
    labelMedium = TextStyle(fontFamily = Inter),
    labelSmall = TextStyle(fontFamily = Inter)
)
