package com.molinetenterprises.webviewkit.presentation.design_system

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.webkit.CookieManager
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.FrameLayout
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.core.net.toUri
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.molinetenterprises.webviewkit.presentation.WebViewScreenViewModel
import java.net.HttpURLConnection
import java.net.URL


@Composable
fun rememberWebViewComponent(
    context: Context,
    activity: Activity,
    baseUrl: String = "",
    fileChooserLauncher: ManagedActivityResultLauncher<String, Uri?>,
    filePathCallback: ValueCallback<Array<Uri>>?,
    onFilePathCallbackChanged: (ValueCallback<Array<Uri>>?) -> Unit,
    onPageFinished: (String) -> Unit = {},
    uiEvent: (WebViewScreenViewModel.Event) -> Unit
): WebView {
    var customView by remember { mutableStateOf<View?>(null) }
    var fullScreenContainer by remember { mutableStateOf<FrameLayout?>(null) }
    var isFullScreen by remember { mutableStateOf(false) }

    return remember {
        WebView(context).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            setBackgroundColor(Color.White.toArgb())

            settings.domStorageEnabled = true
            settings.cacheMode = WebSettings.LOAD_DEFAULT
            settings.setGeolocationEnabled(true)
            settings.allowFileAccess = true
            settings.allowContentAccess = true
            settings.mediaPlaybackRequiresUserGesture = false

            CookieManager.getInstance().setAcceptThirdPartyCookies(this, true)
            val cookieManager = CookieManager.getInstance()
            cookieManager.setAcceptCookie(true)

            webViewClient = object : WebViewClient() {
                override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                    request?.url?.let { uri ->
                        val clickedUrl = request.url.toString()
                        return if (!clickedUrl.contains(baseUrl)) {
                            val intent = Intent(Intent.ACTION_VIEW, clickedUrl.toUri())
                            context.startActivity(intent)
                            true
                        } else {
                            false
                        }
                    }
                    return false
                }

                override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                    super.onPageStarted(view, url, favicon)
                    Log.d("WebView", "onPageStarted:")
                    uiEvent(WebViewScreenViewModel.Event.OnPageStarted)

                    val userAgent = view?.settings?.userAgentString ?: "Mozilla/5.0"

                    url?.let { pageUrl ->
                        Thread {
                            try {
                                val urlConnection = URL(pageUrl).openConnection() as HttpURLConnection
                                urlConnection.requestMethod = "HEAD"
                                urlConnection.connectTimeout = 5000
                                urlConnection.readTimeout = 5000
                                urlConnection.instanceFollowRedirects = true
                                urlConnection.setRequestProperty("User-Agent", userAgent) // Usar la variable local

                                val statusCode = urlConnection.responseCode

                                Log.d("WebView", "Status code $statusCode para: $pageUrl")

                                if (statusCode in 400..599) {
                                    Log.e("WebView", "Error HTTP $statusCode detectado")

                                    view?.post {
                                        uiEvent(
                                            WebViewScreenViewModel.Event.OnErrorReceived(
                                                statusCode = statusCode
                                            )
                                        )
                                    }
                                }

                                urlConnection.disconnect()
                            } catch (e: Exception) {
                                view?.post {
                                    uiEvent(
                                        WebViewScreenViewModel.Event.OnErrorReceived(
                                            statusCode = 408
                                        )
                                    )
                                }
                                Log.e("WebView", "Error verificando status: ${e.message}")
                            }
                        }.start()
                    }
                }

                override fun onPageFinished(view: WebView?, url: String?) {
                    super.onPageFinished(view, url)

                    view?.url?.let {
                        Log.d("WebView", "onPageFinished: $it")
                        CookieManager.getInstance().flush() 
                        onPageFinished(it)
                    }
                }

                override fun onReceivedError(
                    view: WebView?,
                    request: WebResourceRequest?,
                    error: WebResourceError?
                ) {
                    super.onReceivedError(view, request, error)

                    val requestUrl = request?.url.toString()

                    // Mismo filtro aquí
                    if (requestUrl.contains("favicon", ignoreCase = true)) {
                        return
                    }

                    Log.e("WebView", "App error: ${error?.errorCode}")

                    error?.errorCode?.let {
                        uiEvent(
                            WebViewScreenViewModel.Event.OnErrorReceived(
                                statusCode = it
                            )
                        )
                    }
                }

                override fun onReceivedHttpError(
                    view: WebView?,
                    request: WebResourceRequest?,
                    errorResponse: WebResourceResponse?
                ) {
                    super.onReceivedHttpError(view, request, errorResponse)

                    val statusCode = errorResponse?.statusCode ?: return
                    val requestUrl = request?.url.toString()

                    if (requestUrl.contains("favicon", ignoreCase = true)) {
                        Log.d("WebView", "Ignorando favicon")
                        return
                    }

                    if (statusCode in 400..599 && request?.isForMainFrame == true) {
                        Log.e("WebView", "Error HTTP $statusCode en página principal: ${request.url}")

                        uiEvent(
                            WebViewScreenViewModel.Event.OnErrorReceived(
                                statusCode = statusCode
                            )
                        )
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

                    fullScreenContainer = FrameLayout(context).apply {
                        layoutParams = ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )
                        addView(view)
                    }

                    activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                    WindowInsetsControllerCompat(activity.window, activity.window.decorView).apply {
                        hide(WindowInsetsCompat.Type.systemBars())
                        systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                    }

                    (activity.window.decorView as ViewGroup).addView(fullScreenContainer)
                }

                override fun onHideCustomView() {
                    customView?.let {
                        (activity.window.decorView as ViewGroup).removeView(fullScreenContainer)
                    }

                    activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

                    WindowInsetsControllerCompat(activity.window, activity.window.decorView).apply {
                        show(WindowInsetsCompat.Type.systemBars())
                        systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                    }
                }
            }
        }
    }
}