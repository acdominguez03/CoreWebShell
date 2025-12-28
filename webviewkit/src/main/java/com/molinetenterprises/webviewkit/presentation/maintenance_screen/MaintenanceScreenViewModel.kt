package com.molinetenterprises.webviewkit.presentation.maintenance_screen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

enum class WebViewMode {
    NORMAL, MAINTENANCE, ERROR
}

class MaintenanceScreenViewModel(
    private val client: HttpClient,
    private val maintenanceUrl: String
): ViewModel() {

    data class MaintenanceState(
        val startEpoch: Long = 0L,
        val endEpoch: Long = 0L,
        val webViewMode : WebViewMode = WebViewMode.NORMAL,
        val error: String? = null
    )

    private val _maintenanceState = MutableStateFlow(MaintenanceState())
    val maintenanceState: StateFlow<MaintenanceState> = _maintenanceState

    private var job: Job? = null

    init {
        startLoop()
    }

    private fun startLoop() {
        job = viewModelScope.launch(Dispatchers.IO) {
            while (isActive) {
                checkMaintenance()
                delay(30_000)
            }
        }
    }

    private suspend fun checkMaintenance() {
        try {
            val response = client.get(maintenanceUrl)
            val body = response.bodyAsText().trim()

            val parts = body.split("|")
            if (parts.size != 2) return

            val startEpoch = parts[0].toLongOrNull() ?: return
            val endEpoch = parts[1].toLongOrNull() ?: return

            val nowEpoch = System.currentTimeMillis() / 1000

            if (nowEpoch in startEpoch..endEpoch) {
                _maintenanceState.tryEmit(
                    _maintenanceState.value.copy(
                        startEpoch = startEpoch,
                        endEpoch = endEpoch,
                        webViewMode = WebViewMode.MAINTENANCE
                    )
                )
                job?.cancel()
            } else {
                _maintenanceState.tryEmit(
                    _maintenanceState.value.copy(
                        startEpoch = 0L,
                        endEpoch = 0L,
                        webViewMode = WebViewMode.NORMAL
                    )
                )
            }
        } catch (exception: Exception) {
            _maintenanceState.tryEmit(
                _maintenanceState.value.copy(
                    error = exception.message,
                    webViewMode = WebViewMode.ERROR
                )
            )
        }
    }
}