package com.molinetenterprises.webviewkit.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.viewinterop.AndroidView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.molinetenterprises.webviewkit.presentation.design_system.ErrorPage
import com.molinetenterprises.webviewkit.presentation.error_screen.MaintenanceErrorScreen
import com.molinetenterprises.webviewkit.presentation.maintenance_screen.MaintenanceScreen
import com.molinetenterprises.webviewkit.presentation.version_screen.VersionScreen
import com.molinetenterprises.webviewkit.presentation.webview_access.WebViewAccessViewModel
import com.molinetenterprises.webviewkit.presentation.webview_access.WebViewMode
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun WebViewScreen(
    backgroundColor: Color,
    url: String,
    maintenanceUrl: String,
    versionUrl: String,
    appVersion: String,
    enableProgressBar: Boolean = true,
    backButtonEnabled: Boolean = false,
    donateButtonEnabled: Boolean = false,
    progressIndicatorColor: Color = Color.White,
    popBackStack: () -> Unit = {},
    navigateToAnotherView: () -> Unit = {}
) {
    val viewModel: WebViewScreenViewModel = koinViewModel()
    val webViewAccessViewModel: WebViewAccessViewModel = koinViewModel(
       parameters = {
           parametersOf(maintenanceUrl, versionUrl, appVersion, url)
       }
    )

    val maintenanceState = webViewAccessViewModel.webViewAccessState.collectAsState().value

    when (maintenanceState.webViewMode) {
        WebViewMode.NORMAL -> {
            WebViewContent(
                state = viewModel.webViewState.collectAsState().value,
                uiEvent = { viewModel.handleEvent(it) },
                backgroundColor = backgroundColor,
                url = url,
                enableProgressBar = enableProgressBar,
                backButtonEnabled = backButtonEnabled,
                donateButtonEnabled = donateButtonEnabled,
                popBackStack = popBackStack,
                navigateToAnotherView = navigateToAnotherView,
                progressIndicatorColor = progressIndicatorColor
            )
        }
        WebViewMode.MAINTENANCE -> {
            MaintenanceScreen(
                startEpoch = maintenanceState.startEpoch,
                endEpoch = maintenanceState.endEpoch
            )
        }
        WebViewMode.VERSION -> {
            VersionScreen()
        }
        WebViewMode.ERROR -> {
            if (maintenanceState.statusCodeError == 408) {
                ErrorPage(isWebView = false)
            } else if (maintenanceState.statusCodeError >= 0) {
                MaintenanceErrorScreen(
                    statusCode = maintenanceState.statusCodeError
                )
            }
        }
    }

}