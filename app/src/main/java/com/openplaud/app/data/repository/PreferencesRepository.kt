package com.openplaud.app.data.repository

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore by preferencesDataStore(name = "plaud_prefs")

@Singleton
class PreferencesRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private val KEY_API_KEY = stringPreferencesKey("api_key")
        private val KEY_BASE_URL = stringPreferencesKey("base_url")
        private val KEY_SESSION_COOKIE = stringPreferencesKey("session_cookie")
    }

    val apiKey: Flow<String?> = context.dataStore.data.map { it[KEY_API_KEY] }
    val baseUrl: Flow<String?> = context.dataStore.data.map { it[KEY_BASE_URL] }

    suspend fun setApiKey(key: String) {
        context.dataStore.edit { it[KEY_API_KEY] = key }
    }

    suspend fun setBaseUrl(url: String) {
        context.dataStore.edit { it[KEY_BASE_URL] = url }
    }

    suspend fun setSessionCookie(cookie: String) {
        context.dataStore.edit { it[KEY_SESSION_COOKIE] = cookie }
    }

    suspend fun getApiKey(): String? = context.dataStore.data.first()[KEY_API_KEY]

    suspend fun getBaseUrl(): String? = context.dataStore.data.first()[KEY_BASE_URL]

    suspend fun getSessionCookie(): String? = context.dataStore.data.first()[KEY_SESSION_COOKIE]

    suspend fun clearAll() {
        context.dataStore.edit { it.clear() }
    }
}
