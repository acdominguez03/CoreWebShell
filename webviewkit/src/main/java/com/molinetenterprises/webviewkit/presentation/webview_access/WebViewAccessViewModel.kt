package com.molinetenterprises.webviewkit.presentation.webview_access

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.ktor.client.HttpClient
import io.ktor.client.network.sockets.ConnectTimeoutException
import io.ktor.client.network.sockets.SocketTimeoutException
import io.ktor.client.plugins.ResponseException
import io.ktor.client.request.get
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.isSuccess
import io.ktor.util.network.UnresolvedAddressException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.net.UnknownHostException

enum class WebViewMode {
    NORMAL, MAINTENANCE, VERSION, ERROR
}

class WebViewAccessViewModel(
    private val client: HttpClient,
    private val maintenanceUrl: String,
    private val versionUrl: String,
    private val appVersion: String,
    private val serverUrl: String
): ViewModel() {

    data class WebViewAccessState(
        val startEpoch: Long = 0L,
        val endEpoch: Long = 0L,
        val webViewMode : WebViewMode = WebViewMode.NORMAL,
        val statusCodeError: Int = 500
    )

    private val _webViewAccessState = MutableStateFlow(WebViewAccessState())
    val webViewAccessState: StateFlow<WebViewAccessState> = _webViewAccessState

    private var shouldPoll = true

    init {
        startLoop()
    }

    private fun startLoop() {
        viewModelScope.launch(Dispatchers.IO) {
            while (isActive && shouldPoll) {
                val isReachable = checkServerStatus(serverUrl)
                if (isReachable) {
                    checkFlow()
                } else {
                    _webViewAccessState.update {
                        it.copy(
                            statusCodeError = 408,
                            webViewMode = WebViewMode.ERROR
                        )
                    }
                }
                delay(10_000)
            }
        }
    }

    private sealed class MaintenanceResult {
        data class InMaintenance(val startEpoch: Long, val endEpoch: Long) : MaintenanceResult()
        object NotInMaintenance : MaintenanceResult()
        data class Error(val errorCode: Int) : MaintenanceResult()
    }

    private sealed class VersionResult {
        object Correct : VersionResult()
        object Incorrect : VersionResult()
        data class Error(val errorCode: Int) : VersionResult()
    }

    private suspend fun checkFlow() {
        when (val versionResult = checkVersion()) {
            VersionResult.Correct -> {
                when (val maintenanceResult = checkMaintenance()) {
                    is MaintenanceResult.Error -> {
                        setError(maintenanceResult.errorCode)
                    }
                    is MaintenanceResult.InMaintenance -> {
                        _webViewAccessState.update {
                            it.copy(
                                startEpoch = maintenanceResult.startEpoch,
                                endEpoch = maintenanceResult.endEpoch,
                                webViewMode = WebViewMode.MAINTENANCE
                            )
                        }
                        shouldPoll = false
                    }
                    MaintenanceResult.NotInMaintenance -> {
                        _webViewAccessState.update {
                            it.copy(webViewMode = WebViewMode.NORMAL)
                        }
                    }
                }
            }
            is VersionResult.Error -> {
                setError(versionResult.errorCode)
            }
            VersionResult.Incorrect -> {
                _webViewAccessState.update {
                    it.copy(webViewMode = WebViewMode.VERSION)
                }
                shouldPoll = false
            }
        }
    }

    private suspend fun checkMaintenance(): MaintenanceResult {
        try {
            val response = client.get(maintenanceUrl)

            if (response.status.value != 200) {
                return MaintenanceResult.Error(errorCode = response.status.value)
            } else {
                val body = response.bodyAsText().trim()

                val parts = body.split("|")

                if (parts.size != 2) {
                    return MaintenanceResult.Error(errorCode = 422)
                } else {
                    val startEpoch = parts[0].toLongOrNull()
                    val endEpoch = parts[1].toLongOrNull()

                    if (startEpoch == null || endEpoch == null) {
                        return MaintenanceResult.Error(errorCode = 422)
                    } else {
                        val nowEpoch = System.currentTimeMillis() / 1000

                        return if (nowEpoch in startEpoch..endEpoch) {
                            MaintenanceResult.InMaintenance(startEpoch = startEpoch, endEpoch = endEpoch)
                        } else {
                            MaintenanceResult.NotInMaintenance
                        }
                    }
                }
            }
        } catch (exception: Exception) {
            return MaintenanceResult.Error(errorCode = mapException(exception))
        }
    }

    private suspend fun checkVersion(): VersionResult {
        try {
            val response = client.get(versionUrl)

            if (response.status.value != 200) {
                return VersionResult.Error(errorCode = response.status.value)
            }

            val remoteVersion = response.bodyAsText().trim()

            return if (compareVersions(appVersion, remoteVersion) >= 0) {
                VersionResult.Correct
            } else {
                VersionResult.Incorrect
            }
        } catch (exception: Exception) {
            return VersionResult.Error(errorCode = mapException(exception))
        }
    }

    private suspend fun checkServerStatus(url: String): Boolean {
        val client = HttpClient()

        return try {
            val response: HttpResponse = client.get(url)
            response.status.isSuccess()
        } catch (e: Exception) {
            Log.e("ServerCheck", "Error: ${e.message}")

            false
        } finally {
            client.close()
        }
    }

    //Helpers
    private fun setError(code: Int) {
        _webViewAccessState.update {
            it.copy(
                statusCodeError = code,
                webViewMode = WebViewMode.ERROR
            )
        }
    }

    private fun mapException(e: Exception): Int = when (e) {
        is ResponseException -> e.response.status.value
        is SocketTimeoutException,
        is ConnectTimeoutException -> 408
        is UnknownHostException -> 999
        is UnresolvedAddressException -> 408
        else -> 999
    }

    private fun compareVersions(a: String, b: String): Int {
        val aParts = a.split(".").map { it.toIntOrNull() ?: 0 }
        val bParts = b.split(".").map { it.toIntOrNull() ?: 0 }

        for (i in 0 until maxOf(aParts.size, bParts.size)) {
            val diff = (aParts.getOrElse(i) { 0 }) - (bParts.getOrElse(i) { 0 })
            if (diff != 0) return diff
        }
        return 0
    }
}