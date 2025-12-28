package com.molinetenterprises.webviewkit.di

import android.app.Application
import android.content.Context
import com.molinetenterprises.webviewkit.data.DataStoreManager
import com.molinetenterprises.webviewkit.presentation.WebViewScreenViewModel
import com.molinetenterprises.webviewkit.presentation.maintenance_screen.MaintenanceScreenViewModel
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

fun dataStoreModule(appContext: Context) = module {

    single { appContext }

    single { DataStoreManager(context = get()) }

    single { appContext.applicationContext as Application }

    viewModel { WebViewScreenViewModel(dataStoreManager = get(), application = get()) }
    viewModel {  (maintenanceUrl: String) ->
        MaintenanceScreenViewModel(client = get(), maintenanceUrl = maintenanceUrl)
    }

    single { providerKtorClient() }
}

fun providerKtorClient(): HttpClient =
    HttpClient(CIO) {
        expectSuccess = false
        engine {
            requestTimeout = 5000
        }
    }