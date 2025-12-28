package com.molinetenterprises.webviewkit.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.graphics.Color
import com.molinetenterprises.webviewkit.presentation.design_system.ErrorPage
import com.molinetenterprises.webviewkit.presentation.error_screen.ErrorScreen
import com.molinetenterprises.webviewkit.presentation.maintenance_screen.MaintenanceScreen
import com.molinetenterprises.webviewkit.presentation.maintenance_screen.MaintenanceScreenViewModel
import com.molinetenterprises.webviewkit.presentation.maintenance_screen.WebViewMode
import org.koin.core.parameter.parametersOf
import org.koin.androidx.compose.koinViewModel

@Composable
fun WebViewScreen(
    backgroundColor: Color,
    url: String,
    maintenanceUrl: String,
    enableProgressBar: Boolean = true,
    backButtonEnabled: Boolean = false,
    donateButtonEnabled: Boolean = false,
    progressIndicatorColor: Color = Color.White,
    popBackStack: () -> Unit = {},
    navigateToAnotherView: () -> Unit = {}
) {
    val viewModel: WebViewScreenViewModel = koinViewModel()
    val maintenanceScreenViewModel: MaintenanceScreenViewModel = koinViewModel(
       parameters = {
           parametersOf(maintenanceUrl)
       }
    )

    val maintenanceState = maintenanceScreenViewModel.maintenanceState.collectAsState().value

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
        WebViewMode.ERROR -> {
            ErrorScreen(
                message = maintenanceState.error ?: "Unknown error"
            )
        }
    }

}