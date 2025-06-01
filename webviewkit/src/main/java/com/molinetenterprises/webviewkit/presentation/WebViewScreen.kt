package com.molinetenterprises.webviewkit.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.graphics.Color
import org.koin.androidx.compose.koinViewModel

@Composable
fun WebViewScreen(
    backgroundColor: Color,
    url: String,
    haveBottomBar: Boolean = false,
    enableProgressBar: Boolean = true,
    backButtonEnabled: Boolean = false,
    donateButtonEnabled: Boolean = false,
    popBackStack: () -> Unit = {},
    navigateToAnotherView: () -> Unit = {}
) {
    val viewModel: WebViewScreenViewModel = koinViewModel()

    WebViewContent(
        state = viewModel.webViewState.collectAsState().value,
        uiEvent = { viewModel.handleEvent(it) },
        backgroundColor = backgroundColor,
        url = url,
        haveBottomBar = haveBottomBar,
        enableProgressBar = enableProgressBar,
        backButtonEnabled = backButtonEnabled,
        donateButtonEnabled = donateButtonEnabled,
        popBackStack = popBackStack,
        navigateToAnotherView = navigateToAnotherView
    )
}