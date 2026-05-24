package com.openplaud.app.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.openplaud.app.data.api.OpenPlaudApi
import com.openplaud.app.data.repository.PreferencesRepository

class SetupViewModelFactory(
    private val prefs: PreferencesRepository,
    private val api: OpenPlaudApi
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return SetupViewModel(prefs, api) as T
    }
}
