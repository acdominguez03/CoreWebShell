package com.molinetenterprises.webviewkit.presentation.design_system

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.ActivityInfo
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.FrameLayout
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.molinetenterprises.webviewkit.presentation.WebViewScreenViewModel

@Composable
fun rememberWebViewComponent(
    context: Context,
    activity: Activity,
    fileChooserLauncher: ManagedActivityResultLauncher<String, Uri?>,
    filePathCallback: ValueCallback<Array<Uri>>?,
    onFilePathCallbackChanged: (ValueCallback<Array<Uri>>?) -> Unit,
    customView: View?,
    onCustomViewChanged: (View?) -> Unit,
    fullScreenContainer: FrameLayout?,
    onFullScreenContainerChanged: (FrameLayout?) -> Unit,
    onPageFinished: (String) -> Unit = {},
    uiEvent: (WebViewScreenViewModel.Event) -> Unit
): WebView {
    return remember {
        WebView(context).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            setBackgroundColor(Color.White.toArgb())
            settings.javaScriptEnabled = true
            settings.setGeolocationEnabled(true)
            settings.allowFileAccess = true
            settings.allowContentAccess = true
            settings.mediaPlaybackRequiresUserGesture = false

            webViewClient = object : WebViewClient() {
                override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                    super.onPageStarted(view, url, favicon)
                    Log.d("WebView", "onPageFinished:")
                    uiEvent(WebViewScreenViewModel.Event.OnPageStarted)
                }

                override fun onPageFinished(view: WebView?, url: String?) {
                    super.onPageFinished(view, url)
                    Log.d("WebView", "onPageFinished:")
                    view?.url?.let {
                        Log.d("WebView", "onPageFinished: $it")
                        onPageFinished(it)
                    }
                }

                override fun onReceivedError(
                    view: WebView?, request: WebResourceRequest?, error: WebResourceError?
                ) {
                    super.onReceivedError(view, request, error)
                    if (request?.isForMainFrame == true) {
                        uiEvent(WebViewScreenViewModel.Event.OnErrorReceived)
                    }
                }

                override fun onReceivedHttpError(
                    view: WebView?, request: WebResourceRequest?, errorResponse: WebResourceResponse?
                ) {
                    super.onReceivedHttpError(view, request, errorResponse)
                    if (request?.isForMainFrame == true) {
                        uiEvent(WebViewScreenViewModel.Event.OnErrorReceived)
                    }
                }
            }

            webChromeClient = object : WebChromeClient() {
                override fun onProgressChanged(view: WebView?, newProgress: Int) {
                    uiEvent(WebViewScreenViewModel.Event.OnProgressChanged(newProgress))
                }

                override fun onShowFileChooser(
                    webView: WebView?,
                    newFilePathCallback: ValueCallback<Array<Uri>>?,
                    fileChooserParams: FileChooserParams?
                ): Boolean {
                    filePathCallback?.onReceiveValue(null)
                    onFilePathCallbackChanged(newFilePathCallback)
                    fileChooserLauncher.launch("*/*")
                    return true
                }

                override fun onShowCustomView(view: View?, callback: CustomViewCallback?) {
                    if (customView != null) {
                        onHideCustomView()
                        return
                    }
                    onCustomViewChanged(view)

                    onFullScreenContainerChanged(FrameLayout(context).apply {
                        layoutParams = ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )
                        addView(view)
                    })

                    activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE

                    WindowInsetsControllerCompat(activity.window, activity.window.decorView).apply {
                        hide(WindowInsetsCompat.Type.systemBars())
                        systemBarsBehavior =
                            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                    }

                    (activity.window.decorView as ViewGroup).addView(fullScreenContainer)
                }

                override fun onHideCustomView() {
                    customView?.let {
                        (activity.window.decorView as ViewGroup).removeView(fullScreenContainer)
                        onCustomViewChanged(null)
                        onFullScreenContainerChanged(null)
                        uiEvent(WebViewScreenViewModel.Event.OnHideCustomView)
                    }
                    activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_FULL_USER

                    WindowInsetsControllerCompat(activity.window, activity.window.decorView).apply {
                        show(WindowInsetsCompat.Type.systemBars())
                        systemBarsBehavior =
                            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                    }
                }
            }
        }
    }
}