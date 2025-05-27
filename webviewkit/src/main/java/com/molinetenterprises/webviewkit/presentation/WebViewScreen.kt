package com.molinetenterprises.webviewkit.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.graphics.Color
import org.koin.androidx.compose.koinViewModel

@Composable
fun WebViewScreen(
    backgroundColor: Color,
    url: String
) {
    val viewModel: WebViewScreenViewModel = koinViewModel()

    WebViewContent(
        state = viewModel.webViewState.collectAsState().value,
        uiEvent = { viewModel.handleEvent(it) },
        backgroundColor = backgroundColor,
        url = url
    )
}