package com.molinetenterprises.corewebshell

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.molinetenterprises.corewebshell.ui.theme.CoreWebShellTheme
import com.molinetenterprises.webviewkit.presentation.WebViewContent
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
                            url = "https://www.burbuja.info/inmobiliaria/forums/",
                            enableProgressBar = false
                        )
                    }
                }
            }
        }
    }
}