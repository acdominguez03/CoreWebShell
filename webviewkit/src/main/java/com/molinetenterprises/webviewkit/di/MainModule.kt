package com.molinetenterprises.webviewkit.di

import android.app.Application
import android.content.Context
import com.molinetenterprises.webviewkit.data.DataStoreManager
import com.molinetenterprises.webviewkit.presentation.WebViewScreenViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

fun dataStoreModule(appContext: Context) = module {

    single { appContext }

    single { DataStoreManager(context = get()) }

    single { appContext.applicationContext as Application }

    viewModel { WebViewScreenViewModel(dataStoreManager = get(), application = get()) }
}