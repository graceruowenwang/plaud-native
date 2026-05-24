package com.openplaud.app.ui.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.openplaud.app.data.repository.RecordingRepository

class RecordingListViewModelFactory(
    private val repo: RecordingRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return RecordingListViewModel(repo) as T
    }
}
