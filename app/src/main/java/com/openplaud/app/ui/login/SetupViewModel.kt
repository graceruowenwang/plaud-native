package com.openplaud.app.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.openplaud.app.data.api.OpenPlaudApi
import com.openplaud.app.data.repository.PreferencesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SetupUiState(
    val serverUrl: String = "http://134.175.249.19",
    val apiKey: String = "",
    val isLoading: Boolean = false,
    val isReady: Boolean = false,
    val error: String? = null
)

class SetupViewModel(
    private val prefs: PreferencesRepository,
    private val api: OpenPlaudApi
) : ViewModel() {

    private val _uiState = MutableStateFlow(SetupUiState())
    val uiState: StateFlow<SetupUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val savedKey = prefs.getApiKey()
            if (!savedKey.isNullOrBlank()) {
                _uiState.update { it.copy(apiKey = savedKey) }
                verify()
            }
        }
    }

    fun onServerUrlChange(url: String) {
        _uiState.update { it.copy(serverUrl = url, error = null) }
    }

    fun onApiKeyChange(key: String) {
        _uiState.update { it.copy(apiKey = key, error = null) }
    }

    fun connect() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            prefs.setApiKey(_uiState.value.apiKey)
            verify()
        }
    }

    private suspend fun verify() {
        try {
            val response = api.getRecordings(limit = 1)
            if (response.isSuccessful) {
                _uiState.update { it.copy(isLoading = false, isReady = true) }
            } else {
                _uiState.update {
                    it.copy(isLoading = false, error = "Auth failed (${response.code()})")
                }
            }
        } catch (e: Exception) {
            _uiState.update {
                it.copy(isLoading = false, error = "Connection failed: ${e.localizedMessage}")
            }
        }
    }
}
