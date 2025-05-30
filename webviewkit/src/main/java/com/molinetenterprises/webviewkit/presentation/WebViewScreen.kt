package com.molinetenterprises.webviewkit.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.graphics.Color
import org.koin.androidx.compose.koinViewModel

@Composable
fun WebViewScreen(
    backgroundColor: Color,
    url: String,
    enableProgressBar: Boolean = true,
    backButtonEnabled: Boolean = false,
    donateButtonEnabled: Boolean = false,
    haveBottomBar: Boolean = false,
    popBackStack: () -> Unit = {},
    navigateToAnotherView: () -> Unit = {}
) {
    val viewModel: WebViewScreenViewModel = koinViewModel()

    WebViewContent(
        state = viewModel.webViewState.collectAsState().value,
        uiEvent = { viewModel.handleEvent(it) },
        backgroundColor = backgroundColor,
        url = url,
        enableProgressBar = enableProgressBar,
        backButtonEnabled = backButtonEnabled,
        donateButtonEnabled = donateButtonEnabled,
        haveBottomBar = haveBottomBar,
        popBackStack = popBackStack,
        navigateToAnotherView = navigateToAnotherView
    )
}