package com.eous.mentor.core.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource

@Composable
fun rememberAssetVectorPainter(resName: String): Painter? {
    val context = LocalContext.current
    val cleanName = remember(resName) {
        resName.substringAfterLast("/").substringBeforeLast(".")
    }
    val resId = remember(cleanName) {
        context.resources.getIdentifier(cleanName, "drawable", context.packageName)
    }
    return if (resId != 0) {
        painterResource(id = resId)
    } else {
        null
    }
}
