package com.molinetenterprises.webviewkit.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map

class DataStoreManager(private val context: Context) {

    val Context.dataStore by preferencesDataStore("web_view_info")

    companion object {
        val LAST_URL = stringPreferencesKey("last_url")
    }

    suspend fun saveUrl(url: String) {
        context.dataStore.edit { settings ->
            settings[LAST_URL] = url
        }
    }

    suspend fun getUrl(): String? {
        return context.dataStore.data
            .map { preferences -> preferences[LAST_URL] }
            .firstOrNull()
    }
}