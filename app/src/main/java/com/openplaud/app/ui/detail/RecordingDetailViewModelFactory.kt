package com.openplaud.app.ui.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.openplaud.app.data.repository.PreferencesRepository
import com.openplaud.app.data.repository.RecordingRepository

class RecordingDetailViewModelFactory(
    private val repo: RecordingRepository,
    private val prefs: PreferencesRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return RecordingDetailViewModel(repo, prefs) as T
    }
}
