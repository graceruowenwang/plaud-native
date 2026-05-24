package com.openplaud.app.ui.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.openplaud.app.data.model.V1RecordingDetail
import com.openplaud.app.data.repository.PreferencesRepository
import com.openplaud.app.data.repository.RecordingRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class DetailUiState(
    val recording: V1RecordingDetail? = null,
    val isLoading: Boolean = true,
    val error: String? = null,
    val audioUrl: String = "",
    val isPlaying: Boolean = false,
    val playbackPosition: Long = 0L,
    val playbackDuration: Long = 0L
)

class RecordingDetailViewModel(
    private val repository: RecordingRepository,
    private val prefs: PreferencesRepository
) : ViewModel() {

    private val _state = MutableStateFlow(DetailUiState())
    val state: StateFlow<DetailUiState> = _state.asStateFlow()

    fun loadRecording(id: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            val baseUrl = prefs.getBaseUrl() ?: "http://134.175.249.19"
            repository.getRecordingDetail(id).onSuccess { detail ->
                _state.update {
                    it.copy(recording = detail, isLoading = false, audioUrl = "$baseUrl${detail.links.audio}")
                }
            }.onFailure { e ->
                _state.update { it.copy(isLoading = false, error = e.localizedMessage) }
            }
        }
    }

    fun onPlaybackStateChanged(isPlaying: Boolean, position: Long, duration: Long) {
        _state.update { it.copy(isPlaying = isPlaying, playbackPosition = position, playbackDuration = duration) }
    }
}
