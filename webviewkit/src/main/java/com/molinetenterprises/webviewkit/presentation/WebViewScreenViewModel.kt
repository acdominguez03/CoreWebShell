package com.molinetenterprises.webviewkit.presentation

import android.app.Application
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.util.Log
import android.webkit.WebView
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.molinetenterprises.webviewkit.data.DataStoreManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class WebViewScreenViewModel(
    private val dataStoreManager: DataStoreManager,
    application: Application
): ViewModel() {

    data class WebViewState(
        val isWebViewLoaded: Boolean = false,
        val refreshing: Boolean = false,
        val isFirstTime: Boolean = true,
        val linearProgressIndicator: Float = 0f,
        val hasError: Boolean = false,
        val isConnected: Boolean = true,
        val isFullScreen: Boolean = false,
        val showBanner : Boolean = false,
        val isErrorBanner: Boolean = false,
        val previousConnectedState: Boolean? = null,
    )

    private val connectivityManager = application.getSystemService(ConnectivityManager::class.java)

    private val networkCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            onConnectionChanged(isConnected = true)
        }

        override fun onLost(network: Network) {
            onConnectionChanged(isConnected = false)
        }
    }

    private val _webViewState = MutableStateFlow(WebViewState())
    val webViewState: StateFlow<WebViewState> = _webViewState

    // MEJORA 1: Rastrear errores solo del frame principal
    private var mainFrameHadError: Boolean = false
    private var currentLoadingUrl: String? = null

    init {
        val request = NetworkRequest.Builder().build()
        connectivityManager.registerNetworkCallback(request, networkCallback)
    }

    override fun onCleared() {
        super.onCleared()
        connectivityManager.unregisterNetworkCallback(networkCallback)
    }

    sealed class Event {
        data object OnErrorReceived : Event()
        data class OnProgressChanged(val progress: Int): Event()
        data object OnPageStarted: Event()
        data class OnPageFinished(val url: String, val requestPermissions: () -> Unit): Event()
        data object OnShowCustomView: Event()
        data object OnHideCustomView: Event()
        data class OnRefresh(val webView: WebView): Event()
        data class GetInitialUrl(val defaultUrl: String, val callback: (String) -> Unit): Event()
        data object OnCheckCurrentConnectivity: Event()
        data class OnLaunchedEffect(val value: Boolean): Event()
        data class OnConnectionRecovered(val webView: WebView): Event()
        data object OnConnectionLost: Event()
    }

    fun handleEvent(event: Event) {
        when (event) {
            is Event.OnErrorReceived -> onErrorReceived()
            is Event.OnProgressChanged -> onProgressChanged(progress = event.progress)
            is Event.OnPageStarted -> onPageStarted()
            is Event.OnPageFinished -> onPageFinished(url = event.url, requestPermissions = event.requestPermissions)
            is Event.OnShowCustomView -> onShowCustomView()
            is Event.OnHideCustomView -> onHideCustomView()
            is Event.OnRefresh -> onRefresh(webView = event.webView)
            is Event.GetInitialUrl -> getInitialUrl(defaultUrl = event.defaultUrl, callback = event.callback)
            is Event.OnCheckCurrentConnectivity -> checkCurrentConnectivity()
            is Event.OnLaunchedEffect -> onLaunchedEffect(value = event.value)
            is Event.OnConnectionLost -> onConnectionLost()
            is Event.OnConnectionRecovered -> onConnectionRecovered(webView = event.webView)
        }
    }

    fun onErrorReceived() {
        // MEJORA 2: Solo marcar error si es del frame principal
        mainFrameHadError = true
        Log.e("WebViewViewModel", "Main frame error detected for URL: $currentLoadingUrl")
    }

    fun onProgressChanged(progress: Int) {
        viewModelScope.launch {
            _webViewState.tryEmit(
                _webViewState.value.copy(
                    linearProgressIndicator = progress / 100f
                )
            )

            if (webViewState.value.linearProgressIndicator >= 1) {
                updateRefreshing()
            }
        }
    }

    fun onPageStarted() {
        viewModelScope.launch {
            mainFrameHadError = false

            _webViewState.tryEmit(
                _webViewState.value.copy(
                    isWebViewLoaded = false,
                    hasError = false
                )
            )
        }
    }

    fun onPageFinished(url: String, requestPermissions: () -> Unit) {
        viewModelScope.launch {
            currentLoadingUrl = url

            // Guardar URL solo si es válida y no hubo error
            if (url.isNotBlank() && url.startsWith("http") && !mainFrameHadError) {
                dataStoreManager.saveUrl(url)
            }

            _webViewState.tryEmit(
                _webViewState.value.copy(
                    isWebViewLoaded = true,
                    isFirstTime = false,
                    hasError = mainFrameHadError
                )
            )

            // Solo reiniciar después de actualizar el estado
            if (!mainFrameHadError) {
                requestPermissions()
            }
        }
    }

    private fun updateRefreshing() {
        viewModelScope.launch {
            _webViewState.emit(
                _webViewState.value.copy(
                    linearProgressIndicator = 0.8f
                )
            )
            delay(1000)
            _webViewState.emit(
                _webViewState.value.copy(
                    linearProgressIndicator = 0.99f
                )
            )
            delay(1000)
            _webViewState.emit(
                _webViewState.value.copy(
                    linearProgressIndicator = 1f,
                    refreshing = false
                )
            )
        }
    }

    fun onShowCustomView() {
        viewModelScope.launch {
            _webViewState.tryEmit(
                _webViewState.value.copy(
                    isFullScreen = true
                )
            )
        }
    }

    fun onHideCustomView() {
        viewModelScope.launch {
            _webViewState.tryEmit(
                _webViewState.value.copy(
                    isFullScreen = false
                )
            )
        }
    }

    fun onRefresh(webView: WebView) {
        viewModelScope.launch {
            // Limpiar estado de error antes de recargar
            mainFrameHadError = false

            _webViewState.tryEmit(
                _webViewState.value.copy(
                    refreshing = true,
                    hasError = false
                )
            )
            webView.reload()
        }
    }

    fun onConnectionLost() {
        viewModelScope.launch {
            _webViewState.tryEmit(
                _webViewState.value.copy(
                    isErrorBanner = true,
                    showBanner = true
                )
            )
        }
    }

    fun onConnectionRecovered(webView: WebView) {
        viewModelScope.launch {
            _webViewState.tryEmit(
                _webViewState.value.copy(
                    isErrorBanner = false,
                    showBanner = true
                )
            )

            delay(2000)

            _webViewState.tryEmit(
                _webViewState.value.copy(
                    showBanner = false
                )
            )
        }
    }

    fun onLaunchedEffect(value: Boolean) {
        _webViewState.tryEmit(
            _webViewState.value.copy(
                previousConnectedState = value
            )
        )
    }

    fun getInitialUrl(defaultUrl: String, callback: (String) -> Unit) {
        viewModelScope.launch {
            val stored = dataStoreManager.getUrl()

            val finalUrl = when {
                stored.isNullOrBlank() -> defaultUrl
                stored == "about:blank" -> defaultUrl
                !stored.startsWith("http") -> defaultUrl
                else -> stored
            }

            Log.d("WebViewViewModel", "Loading initial URL: $finalUrl")
            callback(finalUrl)
        }
    }

    fun checkCurrentConnectivity() {
        onConnectionChanged(isConnected = isConnected())
    }

    private fun onConnectionChanged(isConnected: Boolean) {
        viewModelScope.launch {
            _webViewState.emit(
                _webViewState.value.copy(
                    isConnected = isConnected
                )
            )
        }
    }

    fun isConnected(): Boolean {
        val activeNetwork = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(activeNetwork) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }
}


