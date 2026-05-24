package com.openplaud.app.ui.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.openplaud.app.data.model.V1Recording
import com.openplaud.app.data.repository.RecordingRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class RecordingListState(
    val recordings: List<V1Recording> = emptyList(),
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val isSyncing: Boolean = false,
    val error: String? = null,
    val searchQuery: String = "",
    val hasMore: Boolean = false
)

class RecordingListViewModel(
    private val repository: RecordingRepository
) : ViewModel() {

    private val _state = MutableStateFlow(RecordingListState())
    val state: StateFlow<RecordingListState> = _state.asStateFlow()

    private var nextCursor: String? = null
    private var allRecordings = mutableListOf<V1Recording>()

    init {
        loadRecordings(initial = true)
    }

    fun loadRecordings(initial: Boolean = false) {
        viewModelScope.launch {
            _state.update {
                if (initial) it.copy(isLoading = true, error = null)
                else it.copy(isRefreshing = true, error = null)
            }
            val result = repository.getRecordings(limit = 50, cursor = if (initial) null else nextCursor)
            result.onSuccess { page ->
                if (initial) allRecordings.clear()
                allRecordings.addAll(page.recordings)
                nextCursor = page.nextCursor
                applyFilter()
                _state.update { it.copy(isLoading = false, isRefreshing = false, hasMore = page.hasMore) }
            }.onFailure { e ->
                _state.update { it.copy(isLoading = false, isRefreshing = false, error = e.localizedMessage) }
            }
        }
    }

    fun loadMore() {
        if (_state.value.hasMore && !_state.value.isLoading) loadRecordings()
    }

    fun refresh() {
        nextCursor = null
        loadRecordings(initial = true)
    }

    fun syncPlaud() {
        viewModelScope.launch {
            _state.update { it.copy(isSyncing = true) }
            repository.syncRecordings().onSuccess { refresh() }.onFailure { e ->
                _state.update { it.copy(isSyncing = false, error = e.localizedMessage) }
            }
        }
    }

    fun onSearchQueryChange(query: String) {
        _state.update { it.copy(searchQuery = query) }
        applyFilter()
    }

    private fun applyFilter() {
        val q = _state.value.searchQuery.trim().lowercase()
        val filtered = if (q.isEmpty()) allRecordings.toList()
        else allRecordings.filter { it.title.lowercase().contains(q) }
        _state.update { it.copy(recordings = filtered) }
    }
}
