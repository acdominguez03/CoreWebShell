package com.molinetenterprises.corewebshell

import android.app.Application
import com.molinetenterprises.webviewkit.di.dataStoreModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class CoreWebShellApp: Application() {
    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidContext(this@CoreWebShellApp)
            modules(
                listOf(
                    dataStoreModule(this@CoreWebShellApp)
                )
            )
        }
    }
}