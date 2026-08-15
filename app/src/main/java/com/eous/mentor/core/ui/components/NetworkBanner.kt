package com.eous.mentor.core.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.eous.mentor.core.ui.theme.Inter
import kotlinx.coroutines.delay

@Composable
fun NetworkBanner(
    isConnected: Boolean,
    modifier: Modifier = Modifier
) {
    var showBackOnline by remember { mutableStateOf(false) }
    var wasDisconnected by remember { mutableStateOf(false) }

    LaunchedEffect(isConnected) {
        if (!isConnected) {
            wasDisconnected = true
            showBackOnline = false
        } else if (wasDisconnected) {
            showBackOnline = true
            delay(2500)
            showBackOnline = false
            wasDisconnected = false
        }
    }

    val isVisible = !isConnected || showBackOnline

    AnimatedVisibility(
        visible = isVisible,
        enter = slideInVertically(animationSpec = tween(400)) { -it } + fadeIn(animationSpec = tween(300)),
        exit = slideOutVertically(animationSpec = tween(400)) { -it } + fadeOut(animationSpec = tween(300)),
        modifier = modifier
    ) {
        val backgroundColor = if (!isConnected) Color(0xFFD97706) else Color(0xFF10B981) // Amber / Emerald Green
        val message = if (!isConnected) "No internet connection. Showing offline data." else "Back online!"
        val icon = if (!isConnected) Icons.Default.WifiOff else Icons.Default.Wifi

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(backgroundColor)
                .statusBarsPadding()
                .padding(vertical = 8.dp, horizontal = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = message,
                    color = Color.White,
                    fontSize = 13.sp,
                    fontFamily = Inter,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}
