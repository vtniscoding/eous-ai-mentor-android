package com.eous.mentor

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.eous.mentor.core.navigation.AuthRouter
import com.eous.mentor.core.ui.theme.EousTheme

import android.content.Intent
import com.eous.mentor.core.navigation.GlobalNavigationHelper

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        intent?.getStringExtra("navigate_to")?.let { route ->
            GlobalNavigationHelper.pendingRoute = route
        }

        enableEdgeToEdge()
        setContent {
            EousTheme {
                Surface(
                    modifier = Modifier.fillMaxSize()
                ) {
                    AuthRouter()
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        intent.getStringExtra("navigate_to")?.let { route ->
            GlobalNavigationHelper.pendingRoute = route
        }
    }
}

