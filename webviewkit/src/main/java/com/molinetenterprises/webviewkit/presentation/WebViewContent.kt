package com.molinetenterprises.webviewkit.presentation

import android.Manifest
import android.app.Activity
import android.net.Uri
import android.os.Build
import android.view.View
import android.view.ViewGroup
import android.webkit.ValueCallback
import android.widget.FrameLayout
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.zIndex
import androidx.core.view.OnApplyWindowInsetsListener
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.molinetenterprises.webviewkit.core.Utils
import com.molinetenterprises.webviewkit.presentation.design_system.BackButton
import com.molinetenterprises.webviewkit.presentation.design_system.ConnectionBanner
import com.molinetenterprises.webviewkit.presentation.design_system.DonateButton
import com.molinetenterprises.webviewkit.presentation.design_system.ErrorPage
import com.molinetenterprises.webviewkit.presentation.design_system.rememberWebViewComponent

@Composable
fun WebViewContent(
    state: WebViewScreenViewModel.WebViewState = WebViewScreenViewModel.WebViewState(),
    uiEvent: (WebViewScreenViewModel.Event) -> Unit = {},
    backgroundColor: Color = Color.Red,
    url: String = "",
    enableProgressBar: Boolean = true,
    backButtonEnabled: Boolean = false,
    donateButtonEnabled: Boolean = false,
    popBackStack: () -> Unit = {},
    navigateToAnotherView: () -> Unit = {}
) {
    val window = LocalActivity.current?.window
    val context = LocalContext.current
    val activity = context as Activity
    var filePathCallback by remember { mutableStateOf<ValueCallback<Array<Uri>>?>(null) }

    val fileChooserLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri ->
            filePathCallback?.onReceiveValue(uri?.let { arrayOf(it) } ?: emptyArray())
            filePathCallback = null
        }
    )

    val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        arrayOf(
            Manifest.permission.READ_MEDIA_IMAGES,
        )
    } else {
        arrayOf(
            Manifest.permission.READ_EXTERNAL_STORAGE
        )
    }

    var launchPermissions by remember { mutableStateOf(false) }

    val permissionsLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
        onResult = { permissions -> })

    var customView by remember { mutableStateOf<View?>(null) }
    var fullScreenContainer by remember { mutableStateOf<FrameLayout?>(null) }

    val webView = rememberWebViewComponent(
        context = context,
        activity = activity,
        baseUrl = url,
        fileChooserLauncher = fileChooserLauncher,
        filePathCallback = filePathCallback,
        onFilePathCallbackChanged = { filePathCallback = it },
        customView = customView,
        onCustomViewChanged = { customView = it },
        fullScreenContainer = fullScreenContainer,
        onFullScreenContainerChanged = { fullScreenContainer = it },
        onPageFinished = { url ->
            uiEvent(WebViewScreenViewModel.Event.OnPageFinished(url = url, requestPermissions = {
                launchPermissions = true
            }))
        },
        uiEvent = uiEvent
    )

    val view = LocalView.current
    val density = LocalDensity.current
    var isKeyboardVisible by remember { mutableStateOf(false) }

    var keyboardHeight by remember { mutableStateOf(0.dp) }
    var inputFieldOffsetY by remember { mutableStateOf(0.dp) }

    val animatedOffsetY: Dp by animateDpAsState(targetValue = inputFieldOffsetY)

    DisposableEffect(view) {
        val listener = OnApplyWindowInsetsListener { v, insets ->
            val keyboardInsets = insets.getInsets(WindowInsetsCompat.Type.ime())
            val currentKeyboardHeight = with(density) { keyboardInsets.bottom.toDp() }

            if (currentKeyboardHeight > keyboardHeight) {
                isKeyboardVisible = true
                keyboardHeight = currentKeyboardHeight
                inputFieldOffsetY = -keyboardHeight
            } else if (currentKeyboardHeight == 0.dp) {
                isKeyboardVisible = false
                keyboardHeight = 0.dp
                inputFieldOffsetY = 0.dp
            }
            insets
        }

        ViewCompat.setOnApplyWindowInsetsListener(view, listener)

        onDispose {
            ViewCompat.setOnApplyWindowInsetsListener(view, null)
        }
    }

    LaunchedEffect(Unit) {
        if (window != null) {
            Utils.showSystemUI(window)
        }
        uiEvent(WebViewScreenViewModel.Event.OnWebViewStarted(webView = webView, url = url))
    }

    LaunchedEffect(state.isConnected) {
        uiEvent(WebViewScreenViewModel.Event.OnCheckCurrentConnectivity)
        val wasConnected = state.previousConnectedState
        val isNowConnected = state.isConnected

        uiEvent(WebViewScreenViewModel.Event.OnLaunchedEffect(value = isNowConnected))

        if (wasConnected == null) return@LaunchedEffect

        if (!state.isConnected) {
            uiEvent(WebViewScreenViewModel.Event.OnConnectionLost)
        } else {
            if (!wasConnected) {
                uiEvent(WebViewScreenViewModel.Event.OnConnectionRecovered(webView = webView))
                webView.reload()
            }
        }
    }

    LaunchedEffect(launchPermissions) {
        if (launchPermissions) {
            permissionsLauncher.launch(permissions)
            launchPermissions = false
        }
    }


    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Scaffold(modifier = Modifier.fillMaxSize()) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                    ) {
                        if (state.linearProgressIndicator < 1f && enableProgressBar) {
                            LinearProgressIndicator(
                                progress = { state.linearProgressIndicator },
                                color = Color(0xff53BDEB),
                                modifier = Modifier.fillMaxWidth().height(2.dp)
                            )
                        }

                        if (state.hasError) {
                            webView.visibility = View.INVISIBLE

                            AndroidView(
                                factory = { context ->
                                    SwipeRefreshLayout(context).apply {
                                        val composeView = ComposeView(context).apply {
                                            setContent {
                                                ErrorPage()
                                            }
                                        }
                                        addView(composeView)

                                        setOnRefreshListener {
                                            uiEvent(WebViewScreenViewModel.Event.OnRefresh(webView = webView))
                                            isRefreshing = false
                                        }
                                    }
                                },
                                modifier = Modifier.fillMaxSize().zIndex(9f)
                            )
                        } else {
                            AndroidView(
                                factory = { context ->
                                    SwipeRefreshLayout(context).apply {
                                        if (webView.parent != null) {
                                            (webView.parent as? ViewGroup)?.removeView(webView)
                                        }
                                        webView.visibility = View.VISIBLE
                                        addView(webView)
                                        setOnRefreshListener {
                                            uiEvent(WebViewScreenViewModel.Event.OnRefresh(webView = webView))
                                        }
                                    }
                                },
                                update = { swipeRefreshLayout ->
                                    swipeRefreshLayout.isRefreshing = state.refreshing
                                },
                                modifier = Modifier
                                    .offset(y = animatedOffsetY)
                            )
                        }

                    }

                    if (!state.isWebViewLoaded && state.isFirstTime) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(backgroundColor)
                                .zIndex(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                }

                if (backButtonEnabled) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.Black)
                            .padding(5.dp),
                        horizontalArrangement = if (donateButtonEnabled) Arrangement.SpaceBetween else Arrangement.Center
                    ) {
                        BackButton(
                            modifier = Modifier
                                .padding(start = if (donateButtonEnabled) 10.dp else 0.dp),
                            onClick = { popBackStack() }
                        )

                        if (donateButtonEnabled) {
                            DonateButton(
                                modifier = Modifier
                                    .padding(end = 10.dp),
                                onCLick = { navigateToAnotherView() }
                            )
                        }
                    }
                }
            }
        }

        AnimatedVisibility(
            visible = state.showBanner,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .zIndex(1f),
            enter = fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it })
        ) {
            ConnectionBanner(modifier = Modifier.offset(y = if (backButtonEnabled) (-50).dp else 0.dp), isError = state.isErrorBanner)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun WebViewScreenPreview() {
    WebViewContent()
}