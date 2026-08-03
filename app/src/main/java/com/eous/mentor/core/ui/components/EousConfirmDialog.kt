package com.eous.mentor.core.ui.components

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.eous.mentor.core.ui.theme.Inter

@Composable
fun EousConfirmDialog(
    title: String,
    message: String,
    confirmText: String = "Confirm",
    dismissText: String = "Cancel",
    isDestructive: Boolean = false,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(24.dp),
        containerColor = Color.White,
        title = {
            Text(
                text = title,
                color = Color(0xFF0F172A),
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                fontFamily = Inter
            )
        },
        text = {
            Text(
                text = message,
                color = Color(0xFF475569),
                fontSize = 14.sp,
                fontFamily = Inter,
                lineHeight = 20.sp
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isDestructive) Color(0xFFEF4444) else Color(0xFF5B29A2),
                    contentColor = Color.White
                )
            ) {
                Text(
                    text = confirmText,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    fontFamily = Inter
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = dismissText,
                    color = Color(0xFF64748B),
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    fontFamily = Inter
                )
            }
        }
    )
}
