package com.molinetenterprises.corewebshell

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.ui.graphics.Color
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.molinetenterprises.corewebshell.ui.theme.CoreWebShellTheme
import com.molinetenterprises.webviewkit.presentation.WebViewScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CoreWebShellTheme {
                val navController = rememberNavController()

                NavHost(navController = navController, startDestination = "webView") {
                    composable("webView") {
                        WebViewScreen(
                            backgroundColor = Color(0xFFFFFFFF),
                            url = "https://player.twitch.tv/?channel=oliviermolinete&parent=localhost",
                            enableProgressBar = true,
                        )
                    }
                }
            }
        }
    }
}